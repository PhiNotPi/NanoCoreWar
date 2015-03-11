import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
/**
 * This runs a game of Core Wars between two players.  It can be called mutiple times.
 * 
 * @author PhiNotPi 
 * @version 3/08/15
 */
public class Game
{
    final Player p1;
    final Player p2;
    final int coreSize;
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
        this.coreSize = coreSize;
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
        core = new int[coreSize][5];
        //offset1 = rand.nextInt(coreSize);
        offset1 = 0;
        for(int i = 0; i != p1size; i++)
        {
            //System.arraycopy(p1.getCode().get(i), 0, core[(offset1 + i) % coreSize], 0, 5 );
            int[] line = p1code.get(i);
            int loc = (offset1 + i) % coreSize;
            core[loc][0] = line[0];
            core[loc][1] = line[1];
            core[loc][2] = line[2];
            core[loc][3] = line[3];
            core[loc][4] = line[4];
        }
        offset2 = offset1 + p1size + deltaOffset;
        for(int i = 0; i != p2size; i++)
        {
            //System.arraycopy(p2.getCode().get(i), 0, core[(offset2 + i) % coreSize], 0, 5 );
            int[] line = p2code.get(i);
            int loc = (offset2 + i) % coreSize;
            core[loc][0] = line[0];
            core[loc][1] = line[1];
            core[loc][2] = line[2];
            core[loc][3] = line[3];
            core[loc][4] = line[4];
        }
             
        int p1loc = offset1 % coreSize;
        int p2loc = offset2 % coreSize;
        for(int time = 0; time != maxTime; time++)
        {
            if(debug != 0)
            {
                printCore(p1loc,p2loc);
                System.out.println("p1loc " + p1loc);
                System.out.println("offset " + offset1);
            }
            
            if(core[p1loc][0] == 0)
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
            if(core[p2loc][0] == 0)
            {
                return 2;
            }
            p2loc = execute(p2loc, offset2);
            
        }
        return 1;
    }
    public int execute(int ploc, int offset)
    {
        int line1 = offset + core[ploc][3];
        if(core[ploc][1] != 0)
        {
            line1 += ploc - offset;
        }
        if(core[ploc][1] == 2)
        {
            line1 += core[((line1 % coreSize) + coreSize) % coreSize][3];
        }
        int line2 = offset + core[ploc][4];
        if(core[ploc][2] != 0)
        {
            line2 += ploc - offset;
        }
        if(core[ploc][2] == 2)
        {
            line2 += core[((line2 % coreSize) + coreSize) % coreSize][4];
        }
        line1 = ((line1 % coreSize) + coreSize) % coreSize;
        line2 = ((line2 % coreSize) + coreSize) % coreSize;
        int opcode = core[ploc][0];
        ploc = (ploc + 1) % coreSize;
        //String opDescription = "";
        if(opcode == 1)
        {
            core[line2][0] = core[line1][0];
            core[line2][1] = core[line1][1];
            core[line2][2] = core[line1][2];
            core[line2][3] = core[line1][3];
            core[line2][4] = core[line1][4];
            return ploc;
            //opDescription = "Moved from " + line1 + " to " + line2;
        }
        if(opcode == 2)
        {
                core[line2][3] += core[line1][3];
                core[line2][4] += core[line1][4];
                return ploc;
                //opDescription = "Added " + line1 + " to " + line2;
        }
        if(opcode == 3)
        {
                core[line2][3] -= core[line1][3];
                core[line2][4] -= core[line1][4];
                return ploc;
                //opDescription = "Subtracted " + line1 + " to " + line2;
        }
        if(opcode == 4)
        {
                ploc = line1;
                return ploc;
                //opDescription = "Jumped to " + line1;
        }
        if(opcode == 5)
        {
                if(core[line2][3] == 0 && core[line2][4] == 0)
                {
                    ploc = line1;
                    //opDescription = "Jumped to " + line1;
                }
                else
                {
                    //opDescription = "Did not jump to " + line1;
                }
                return ploc;
        }
        if(opcode == 6)
        {
            if(core[line1][3] == core[line2][3] && core[line1][4] == core[line2][4])
            {
                //opDescription = "Did not skip because " + line1 + " and " + line2 + " were equal.";
            }
            else
            {
                ploc = (ploc + 1) % coreSize;
                //opDescription = "Skipped because " + line1 + " and " + line2 + " were not equal.";
            }
            return ploc;
        }
        if(debug != 0)
        {
            //System.out.println(opDescription);
        }
        return ploc;
    }
    public void printCore(int p1loc, int p2loc)
    {
        int dupCount = 0;
        int[] dupLine = new int[]{0,0,0,0,0};
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
