import java.util.Random;
import java.util.ArrayList;
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
    public Game(Player A, Player B, int coreSize, int maxTime, int debug)
    {
        p1 = A;
        p2 = B;
        this.coreSize = coreSize;
        this.maxTime = maxTime;
        this.debug = debug;
        core = new int[coreSize][5];
        rand = new Random();
    }
    
    public int runAll()
    {
        int sum = 0;
        for(int i = 0; i < coreSize - p1.getCode().size() - p2.getCode().size(); i++)
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
        return run(rand.nextInt(coreSize - p1.getCode().size() - p2.getCode().size()));
    }
    
    public int run(int deltaOffset)
    {
        core = new int[coreSize][5];
        offset1 = rand.nextInt(coreSize);
        for(int i = 0; i < p1.getCode().size(); i++)
        {
            System.arraycopy(p1.getCode().get(i), 0, core[(offset1 + i) % coreSize], 0, p1.getCode().get(i).length );
        }
        offset2 = offset1 + p1.getCode().size() + deltaOffset;
        for(int i = 0; i < p2.getCode().size(); i++)
        {
            System.arraycopy(p2.getCode().get(i), 0, core[(offset2 + i) % coreSize], 0, p2.getCode().get(i).length );
        }
             
        int p1loc = offset1 % coreSize;
        int p2loc = offset2 % coreSize;
        int time = 0;
        while(time < maxTime)
        {
            if(debug > 0)
            {
                printCore();
                System.out.println("p1loc " + p1loc);
            }
            
            if(core[p1loc][0] < 1 || core[p1loc][0] > 6)
            {
                return 0;
            }
            else
            {
                p1loc = execute(p1loc, offset1);
            }
            time++;
            if(debug > 0)
            {
                printCore();
                System.out.println("p2loc " + p2loc);
            }
            if(core[p2loc][0] < 1 || core[p2loc][0] > 6)
            {
                return 2;
            }
            else
            {
                p2loc = execute(p2loc, offset2);
            }
            time++;
        }
        return 1;
    }
    public int execute(int ploc, int offset)
    {
        int line1 = offset + core[ploc][3];
        if(debug > 0){
            System.out.println("offset " + offset);
        }
        if(core[ploc % coreSize][1] > 0)
        {
            line1 += ploc - offset;
        }
        if(core[ploc][1] == 2)
        {
            line1 += core[((line1 % coreSize) + coreSize) % coreSize][3];
        }
        int line2 = offset + core[ploc][4];
        if(core[ploc][2] > 0)
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
        String opDescription = "";
        if(opcode == 1)
        {
            System.arraycopy( core[line1], 0, core[line2], 0, core[line1].length );
            opDescription = "Moved from " + line1 + " to " + line2;
        }
        if(opcode == 2)
        {
                core[line2][3] += core[line1][3];
                core[line2][4] += core[line1][4];
                opDescription = "Added " + line1 + " to " + line2;
        }
        if(opcode == 3)
        {
                core[line2][3] -= core[line1][3];
                core[line2][4] -= core[line1][4];
                opDescription = "Subtracted " + line1 + " to " + line2;
        }
        if(opcode == 4)
        {
                ploc = line1;
                opDescription = "Jumped to " + line1;
        }
        if(opcode == 5)
        {
                if(core[line2][3] == 0 && core[line2][4] == 0)
                {
                    ploc = line1;
                    opDescription = "Jumped to " + line1;
                }
                else
                {
                    opDescription = "Did not jump to " + line1;
                }
        }
        if(opcode == 6)
        {
            if(core[line1][3] == core[line2][3] && core[line1][4] == core[line2][4])
            {
                opDescription = "Did not skip because " + line1 + " and " + line2 + " were equal.";
            }
            else
            {
                ploc = (ploc + 1) % coreSize;
                opDescription = "Skipped because " + line1 + " and " + line2 + " were not equal.";
            }
        }
        if(debug > 0)
        {
            System.out.println(opDescription);
        }
        return ploc;
    }
    public void printCore()
    {
        for(int[] line : core)
        {
            for(int val : line)
            {
                System.out.printf("%5d ",val);
            }
           System.out.println();
        }
    }
}
