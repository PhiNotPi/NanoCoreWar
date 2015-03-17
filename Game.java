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

        int coreSize = this.coreSize, coreSizeM1 = coreSize-1;

        Instruction[] core = new Instruction[coreSize];
        for(int i = 0; i < coreSize; i++)
        {
            core[i] = Instruction.DAT00;
        }

        int offset1 = 0;
        for(int i = 0; i != p1size; i++)
        {
            int loc = (offset1 + i) & coreSizeM1;
            core[loc] = p1code.get(i);
        }

        int offset2 = offset1 + p1size + deltaOffset;
        for(int i = 0; i != p2size; i++)
        {
            int loc = (offset2 + i) & coreSizeM1;
            core[loc] = p2code.get(i);
        }
             
        int poffset = offset1 & coreSizeM1, ploc = poffset;
        int xoffset = offset2 & coreSizeM1, xloc = xoffset;

        int maxSteps = maxTime * 2;
        for(int step = 0; step != maxSteps; step++)
        {
            if(debug != 0)
            {
                printCore(core, ploc, xloc, step);
            }
            
            Instruction curr = core[ploc];
            int op = curr.packedOp;
            if(op < Instruction.minValidOp)
            {
                return ((step & 1) == 0 ? 0 : 2);
            }
            
            int line1 = curr.field1, line2 = curr.field2;
            int opcode = op >> (Instruction.modeBits*2), mode1 = (op >> Instruction.modeBits), mode2 = op;
            mode1 &= Instruction.modeMask;
            mode2 &= Instruction.modeMask;

            switch(mode1)
            {
            case Instruction.MODE_IMM:
                line1 += poffset;
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
                line2 += poffset;
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
            Instruction i1, i2;
     
            switch(opcode)
            {
            case Instruction.OP_MOV:
                core[line2] = core[line1];
                break;
            case Instruction.OP_ADD:
                i1 = core[line1]; i2 = core[line2];
                core[line2] = new Instruction(i2.packedOp, i2.field1 + i1.field1, i2.field2 + i1.field2);
                break;
            case Instruction.OP_SUB:
                i1 = core[line1]; i2 = core[line2];
                core[line2] = new Instruction(i2.packedOp, i2.field1 - i1.field1, i2.field2 - i1.field2);
                break;
            case Instruction.OP_JMP:
                ploc = line1 - 1;  // will be incremented by one below
                break;
            case Instruction.OP_JMZ:
                i2 = core[line2];
                if(i2.field1 == 0 && i2.field2 == 0)
                {
                    ploc = line1 - 1;
                }
                break;
            case Instruction.OP_CMP:
                i1 = core[line1]; i2 = core[line2];
                if(i1.field1 != i2.field1 || i1.field2 != i2.field2)
                {
                    ploc++;
                }
                break;
            default:
                throw new IllegalStateException("invalid opcode " + opcode + " (decoded from " + op + ") on line " + ploc);
            }
            
            ploc++;
            ploc &= coreSizeM1;
           
            int tmpLoc = ploc; ploc = xloc; xloc = tmpLoc;            
            int tmpOffset = poffset; poffset = xoffset; xoffset = tmpOffset;            
        }
        return 1;
    }
    public void printCore(Instruction[] core, int ploc, int xloc, int step)
    {
        int time = (step >> 1), turn = (step & 1);
        
        int dupCount = 0;
        Instruction dupLine = Instruction.DAT00;
        for(int i = 0; i < core.length; i++)
        {
            Instruction line = core[i];
            if(line.equals(dupLine) && i != ploc && i != xloc)
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
                if(i == ploc)
                {
                    System.out.print("\t<- " + (turn == 0 ? 1 : 2));
                }
                if(i == xloc)
                {
                    System.out.print("\t<- " + (turn == 0 ? 2 : 1));
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
