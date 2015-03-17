import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.IllegalStateException;
/**
 * This runs a game of Core Wars between two players.  It can be called mutiple times.
 * 
 * @author PhiNotPi, Ilmari Karonen
 * @version 3/17/15
 */
public class Game
{
    final Player p1;
    final Player p2;
    final int coreSize;
    final int coreSizeM1;
    final int maxTime;
    final int debug;
    Instruction[] core;
    int offset1;
    int offset2;
    Random rand;
    ArrayList<Instruction> p1code;
    ArrayList<Instruction> p2code;
    int p1size;
    int p2size;
    public Game(Player A, Player B, int coreSize, int maxTime, int debug)
    {
        p1 = A;
        p2 = B;
        
        coreSize--;
        coreSize |= coreSize >> 1;
        coreSize |= coreSize >> 2;
        coreSize |= coreSize >> 4;
        coreSize |= coreSize >> 8;
        coreSize |= coreSize >> 16;
        coreSize++;
        
        this.coreSize = coreSize;
        this.coreSizeM1 = coreSize - 1;
        this.maxTime = maxTime / 2;
        this.debug = debug;
        rand = new Random();
        p1code =  p1.getCode();
        p1size = p1code.size();
        p2code =  p2.getCode();
        p2size = p2code.size();
    }
    
    public int runAll()
    {
        int sum = 0;
        for(int i = 0; i < coreSize - p1size - p2size; i++)
        {
            sum += run(i) - 1;
        }
        if(sum > 0)
        {
            return 1;
        }
        if(sum < 0)
        {
            return -1;
        }
        return 0;
    }
    
    public int run()
    {
        return run(rand.nextInt(coreSize - p1size - p2size + 1));
    }
    
    public int run(int deltaOffset)
    {
        if(debug != 0)
        {
            System.out.println("New game between " + p1.getName() + " and " + p2.getName() + " with offset " + deltaOffset + ":");
        }
        //offset1 = rand.nextInt(coreSize);
        offset1 = 0;
        core = new Instruction[coreSize];
        for(int i = 0; i != p1size; i++)
        {
            Instruction line = p1code.get(i);
            int loc = (offset1 + i) & coreSizeM1;
            core[loc] = new Instruction(line);
        }
        offset2 = offset1 + p1size + deltaOffset;
        for(int i = 0; i != p2size; i++)
        {
            Instruction line = p2code.get(i);
            int loc = (offset2 + i) & coreSizeM1;
            core[loc] = new Instruction(line);
        }
        for(int i = 0; i < coreSize; i++)
        {
            if(core[i] == null) core[i] = new Instruction();
        }
             
        int p1loc = offset1 & coreSizeM1;
        int p2loc = offset2 & coreSizeM1;
        for(int time = 0; time != maxTime; time++)
        {
            if(debug != 0)
            {
                printCore(p1loc,p2loc);
                System.out.println("p1loc " + p1loc);
                System.out.println("offset " + offset1);
            }
            
            Instruction p1instr = core[p1loc];
            if(p1instr.packedOp < Instruction.minValidOp)
            {
                return 0;
            }
            p1loc = execute(p1instr, p1loc, offset1);
            
            if(debug != 0)
            {
                printCore(p1loc,p2loc);
                System.out.println("p2loc " + p2loc);
                System.out.println("offset " + offset2);
            }
            Instruction p2instr = core[p2loc];
            if(p2instr.packedOp < Instruction.minValidOp)
            {
                return 2;
            }
            p2loc = execute(p2instr, p2loc, offset2);
            
        }
        return 1;
    }
    public int execute(Instruction curr, int ploc, int offset)
    {
        int op = curr.packedOp, line1 = curr.field1, line2 = curr.field2;
        int opcode = op >> (Instruction.modeBits*2), mode1 = (op >> Instruction.modeBits), mode2 = op;
        mode1 &= Instruction.modeMask;
        mode2 &= Instruction.modeMask;

        switch(mode1)
        {
        case Instruction.MODE_IMM:
            line1 += offset;
            break;
        case Instruction.MODE_DIR:
            line1 += ploc;
            break;
        case Instruction.MODE_IND:
            line1 += ploc;
            line1 += core[line1 & coreSizeM1].field1;
            break;
        default:
            throw new IllegalStateException("invalid A addressing mode " + mode1 + " (decoded from " + op + ") on line " + ploc);
        }
        
        switch(mode2)
        {
        case Instruction.MODE_IMM:
            line2 += offset;
            break;
        case Instruction.MODE_DIR:
            line2 += ploc;
            break;
        case Instruction.MODE_IND:
            line2 += ploc;
            line2 += core[line2 & coreSizeM1].field2;
            break;
        default:
            throw new IllegalStateException("invalid B addressing mode " + mode2 + " (decoded from " + op + ") on line " + ploc);
        }
        
        line1 &= coreSizeM1;
        line2 &= coreSizeM1;
        int next = (ploc+1) & coreSizeM1;
        Instruction i1, i2;

        switch(opcode)
        {
        case Instruction.OP_MOV:
            i1 = core[line1]; i2 = core[line2];
            i2.packedOp = i1.packedOp;
            i2.field1 = i1.field1;
            i2.field2 = i1.field2;
            return next;
        case Instruction.OP_ADD:
            i1 = core[line1]; i2 = core[line2];
            i2.field1 += i1.field1;
            i2.field2 += i1.field2;
            return next;
        case Instruction.OP_SUB:
            i1 = core[line1]; i2 = core[line2];
            i2.field1 -= i1.field1;
            i2.field2 -= i1.field2;
            return next;
        case Instruction.OP_JMP:
            return line1;
        case Instruction.OP_JMZ:
            i2 = core[line2];
            return (i2.field1 == 0 && i2.field2 == 0 ? line1 : next);
        case Instruction.OP_CMP:
            i1 = core[line1]; i2 = core[line2];
            return next + (i1.field1 != i2.field1 || i1.field2 != i2.field2 ? 1 : 0);
        default:
            throw new IllegalStateException("invalid opcode " + opcode + " (decoded from " + op + ") on line " + ploc);
        }
    }
    public void printCore(int p1loc, int p2loc)
    {
        int dupCount = 0;
        Instruction dupLine = new Instruction();
        for(int i = 0; i < core.length; i++)
        {
            Instruction line = core[i];
            if(line.equals(dupLine) && i != p1loc && i != p2loc)
            {
                if(dupCount == 0)
                {
                    System.out.println(line);
                }
                dupCount++;
            }
            else
            {
                if(dupCount == 2)
                {
                    System.out.println(dupLine);
                }
                else if(dupCount > 2)
                {
                    System.out.println("    " + (dupCount - 1) + " lines skipped.");
                }
                System.out.print(line);
                if(i == p1loc)
                {
                    System.out.print("\t<- 1");
                }
                if(i == p2loc)
                {
                    System.out.print("\t<- 2");
                }
                System.out.println();
                dupLine = line;
                dupCount = 1;
            }
        }
        if(dupCount == 2)
        {
            System.out.println(dupLine);
        }
        else if(dupCount > 2)
        {
            System.out.println("    " + (dupCount - 1) + " lines skipped.");
        }
    }
}
