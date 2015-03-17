import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.PrintWriter;
/**
 * This is the main controller for the Nano Core War KOTH
 * 
 * @author PhiNotPi
 * @version 3/08/15
 */
public class Tournament
{
    static int coreSize = 8192;
    static int maxTime = coreSize * 8; //measured in ply
    static int repeats = 0; //number of times a battle is played between each pair of contestants
    static int overwriteLeaderboardFile = 0;
    static int debug = 0;
    public static void main(String [] args)
    {
        long startTime = System.nanoTime();
        
        
        try{
            Scanner settings = new Scanner(new File("settings.txt"));
            while(settings.hasNextLine())
            {
                Scanner setting = new Scanner(settings.nextLine());
                String varName = setting.next().trim().toLowerCase();
                int val = setting.nextInt();
                if(varName.equals("coresize"))
                {
                    coreSize = val;
                }
                if(varName.equals("maxtime"))
                {
                    maxTime = coreSize * val;
                }
                if(varName.equals("repeats"))
                {
                    repeats = val;
                }
                if(varName.equals("overwrite"))
                {
                    overwriteLeaderboardFile = val;
                }
                if(varName.equals("debug"))
                {
                    debug = val;
                }
            }
        }
        catch(FileNotFoundException e)
        {
            
        }
        
        ArrayList<Player> players = new ArrayList<Player>();
        
        try{
            Scanner playerlist = new Scanner(new File("playerlist.txt"));
            while(playerlist.hasNextLine())
            {
                Scanner entry = new Scanner(playerlist.nextLine());
                String botName = entry.next().trim();
                String botSource = entry.next().trim();
                players.add(Parser.parseFile(botName, botSource));
            }
        }
        catch(FileNotFoundException e)
        {
            
        }
        
        
        final Map<String,Integer>  score = new HashMap<String,Integer>();
        
        for(Player p : players)
        {
            score.put(p.getName(), 0);
        }
        
        try{
            Scanner oldBoard = new Scanner(new File("leaderboard.txt"));
            while(oldBoard.hasNextLine())
            {
                int oldScore = oldBoard.nextInt();
                oldBoard.next();  //should capture the "-"
                String name = oldBoard.next().trim();
                for(Player p : players)
                {
                    if(p.getName().toUpperCase().equals(name.toUpperCase()))
                    {
                        score.put(p.getName(), oldScore);
                        p.isChallenger = false;
                    }
                }
            }
        }
        catch(FileNotFoundException e) { }
        catch(java.util.NoSuchElementException e) { }
        
        System.out.println("Pairwise Results:");
        for(int i = 0; i < players.size() - 1; i++)
        {
            Player p1 = players.get(i);
            for(int j = i + 1; j < players.size(); j++)
            {
                Player p2 = players.get(j);
                if(p1.isChallenger || p2.isChallenger)
                {
                    Game g = new Game(p1, p2, coreSize, maxTime, debug);
                    int result = 0;
                    for(int r = 0; r < repeats; r++)
                    {
                        result += g.run();
                    }
                    if(repeats == 0)
                    {
                        Game g2 = new Game(p2, p1, coreSize, maxTime, debug);
                        result = g.runAll() - g2.runAll();
                    }
                    if(result > repeats)
                    {
                        score.put(p1.getName(), score.get(p1.getName()) + 2);
                        System.out.println(p1.getName() +" "+ p2.getName());
                    }
                    else if(result < repeats)
                    {
                        score.put(p2.getName(), score.get(p2.getName()) + 2);
                        System.out.println(p2.getName() +" "+ p1.getName());
                    }
                    else
                    {
                        score.put(p1.getName(), score.get(p1.getName()) + 1);
                        score.put(p2.getName(), score.get(p2.getName()) + 1);
                        System.out.println(p2.getName() +" = "+ p1.getName());
                    }
                }
            }
        }
        String[] playerNames = score.keySet().toArray(new String[score.keySet().size()]);
        Arrays.sort(playerNames, new Comparator<String>() {
            public int compare(String a, String b) {
                return score.get(b).compareTo(score.get(a));
            }
        });
        
        System.out.println("Leaderboard:");
        for(String p : playerNames)
        {
            System.out.printf("%5d - %-40s%n",score.get(p),p);
        }
        if(overwriteLeaderboardFile > 0)
        {
            try
            {
                PrintWriter newBoard = new PrintWriter(new File("leaderboard.txt"));
                for(String p : playerNames)
                {
                    newBoard.printf("%5d - %-40s%n",score.get(p),p);
                }
                newBoard.close();
            }
            catch(FileNotFoundException e)
            {
                
            }
        }
        
        long endTime = System.nanoTime();
        System.out.println("The tournament took " + ((endTime-startTime)/1000000000.0) + " seconds, or " + ((endTime-startTime)/60000000000.0) + " minutes.");
        
    }
}
