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
    int[][] core;
    int offset1;
    int offset2;
    Random rand;
    ArrayList<int[]> p1code;
    ArrayList<int[]> p2code;
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
        core = new int[coreSize][5];
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
        core = new int[coreSize][3];
        //offset1 = rand.nextInt(coreSize);
        offset1 = 0;
        for(int i = 0; i != p1size; i++)
        {
            int[] line = p1code.get(i);
            int loc = (offset1 + i) & coreSizeM1;
            core[loc][0] = line[0];
            core[loc][1] = line[1];
            core[loc][2] = line[2];
        }
        offset2 = offset1 + p1size + deltaOffset;
        for(int i = 0; i != p2size; i++)
        {
            int[] line = p2code.get(i);
            int loc = (offset2 + i) & coreSizeM1;
            core[loc][0] = line[0];
            core[loc][1] = line[1];
            core[loc][2] = line[2];
        }
             
        int p1loc = offset1 & coreSizeM1;
        int p2loc = offset2 & coreSizeM1;
        int minValidOp = (1 << (Parser.modeBits*2));
        for(int time = 0; time != maxTime; time++)
        {
            if(debug != 0)
            {
                printCore(p1loc,p2loc);
                System.out.println("p1loc " + p1loc);
                System.out.println("offset " + offset1);
            }
            
            if(core[p1loc][0] < minValidOp)
            {
                return 0;
            }
            p1loc = execute(p1loc, offset1);
            
            if(debug != 0)
            {
                printCore(p1loc,p2loc);
                System.out.println("p2loc " + p2loc);
                System.out.println("offset " + offset2);
            }
            if(core[p2loc][0] < minValidOp)
            {
                return 2;
            }
            p2loc = execute(p2loc, offset2);
            
        }
        return 1;
    }
    public int execute(int ploc, int offset)
    {
        int[] curr = core[ploc];
        int op = curr[0], line1 = curr[1], line2 = curr[2];
        int opcode = op >> (Parser.modeBits*2), mode1 = (op >> Parser.modeBits), mode2 = op;
        mode1 &= (1 << Parser.modeBits)-1;
        mode2 &= (1 << Parser.modeBits)-1;

        switch(mode1)
        {
        case Parser.MODE_IMM:
            line1 += offset;
            break;
        case Parser.MODE_DIR:
            line1 += ploc;
            break;
        case Parser.MODE_IND:
            line1 += ploc;
            line1 += core[line1 & coreSizeM1][1];
            break;
        default:
            throw new IllegalStateException("invalid A addressing mode " + mode1 + " (decoded from " + op + ") on line " + ploc);
        }
        
        switch(mode2)
        {
        case Parser.MODE_IMM:
            line2 += offset;
            break;
        case Parser.MODE_DIR:
            line2 += ploc;
            break;
        case Parser.MODE_IND:
            line2 += ploc;
            line2 += core[line2 & coreSizeM1][2];
            break;
        default:
            throw new IllegalStateException("invalid B addressing mode " + mode2 + " (decoded from " + op + ") on line " + ploc);
        }
        
        line1 &= coreSizeM1;
        line2 &= coreSizeM1;
        int next = (ploc+1) & coreSizeM1;

        switch(opcode)
        {
        case 1: // MOV
            core[line2][0] = core[line1][0];
            core[line2][1] = core[line1][1];
            core[line2][2] = core[line1][2];
            return next;
        case 2: // ADD
            core[line2][1] += core[line1][1];
            core[line2][2] += core[line1][2];
            return next;
        case 3: // SUB
            core[line2][1] -= core[line1][1];
            core[line2][2] -= core[line1][2];
            return next;
        case 4: // JMP
            return line1;
        case 5: // JMZ
            return (core[line2][1] == 0 && core[line2][2] == 0 ? line1 : next);
        case 6: // CMP
            return next + (core[line1][1] != core[line2][1] || core[line1][2] != core[line2][2] ? 1 : 0);
        default:
            throw new IllegalStateException("invalid opcode " + opcode + " (decoded from " + op + ") on line " + ploc);
        }
    }
    public void printCore(int p1loc, int p2loc)
    {
        int dupCount = 0;
        int[] dupLine = new int[]{0,0,0};
        for(int i = 0; i < core.length; i++)
        {
            int[] line = core[i];
            if(Arrays.equals(line, dupLine) && i != p1loc && i != p2loc)
            {
                if(dupCount == 0)
                {
                    for(int val : line)
                    {
                        System.out.printf("%5d ",val);
                    }
                    System.out.println();
                }
                dupCount++;
            }
            else
            {
                if(dupCount == 2)
                {
                    for(int val : dupLine)
                    {
                        System.out.printf("%5d ",val);
                    }
                    System.out.println();
                }
                else if(dupCount > 2)
                {
                    System.out.println("    " + (dupCount - 1) + " lines skipped.");
                }
                for(int val : line)
                {
                    System.out.printf("%5d ",val);
                }
                if(i == p1loc)
                {
                    System.out.print(" <- 1");
                }
                if(i == p2loc)
                {
                    System.out.print(" <- 2");
                }
                System.out.println();
                dupLine = line;
                dupCount = 1;
            }
        }
        if(dupCount == 2)
        {
            for(int val : dupLine)
            {
                System.out.printf("%5d ",val);
            }
            System.out.println();
        }
        else if(dupCount > 2)
        {
            System.out.println("    " + (dupCount - 1) + " lines skipped.");
        }
    }
}
