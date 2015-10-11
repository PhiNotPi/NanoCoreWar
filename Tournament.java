import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.FileWriter;
/**
 * This is the main controller for the Nano Core War KOTH
 * 
 * @author PhiNotPi, Ilmari Karonen
 * @version 3/17/15
 */
public class Tournament
{
    static int coreSize = 8192;
    static int maxTime = coreSize * 8; //measured in ply
    static int repeats = 0; //number of times a battle is played between each pair of contestants
    static int debug = 0;
    static int verbose = 0;
    static int cache = 2;
    
    static final String settingsFile = "settings.txt";
    static final String playerFile = "playerlist.txt";
    static final String saveFile = "savedresults.txt";
    static final String leaderboardFile = "leaderboard.txt";
    
    public static void main(String [] args)
    {
        long startTime = System.nanoTime();
        
        
        try{
            Scanner settings = new Scanner(new File(settingsFile));
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
                if(varName.equals("debug"))
                {
                    debug = val;
                }
                if(varName.equals("verbose"))
                {
                    verbose = val;
                }
                if(varName.equals("cache"))
                {
                    cache = val;
                }
            }
        }
        catch(FileNotFoundException e)
        {
            System.err.println(settingsFile + " not found, using default settings.");
        }
        
        ArrayList<Player> players = new ArrayList<Player>();
        
        try{
            Scanner playerlist = new Scanner(new File(playerFile));
            while(playerlist.hasNextLine())
            {
                String line = playerlist.nextLine().trim();
                if(line.length() > 0)
                {
                    Scanner entry = new Scanner(line);
                    String botName = entry.next().trim();
                    String botSource = entry.next().trim();
                    System.err.println("Loading " + botName + " from " + botSource);
                    Player bot = Parser.parseFile(botName, botSource, verbose > 0);
                    if(bot == null)
                    {
                        System.err.println("Aborting due to parse errors.");
                        return;
                    }
                    players.add(bot);
                }
            }
        }
        catch(FileNotFoundException e)
        {
            System.err.println(playerFile + " not found, aborting.");
            return;
        }
        
        
        final Map<String,Integer> score = new HashMap<String,Integer>();
        
        for(Player p : players)
        {
            score.put(p.getName(), 0);
        }
        
        Map<String,Integer> matchResults = new HashMap<String,Integer>();
        
        if(cache > 0)
        {
            try{
                Scanner loadResults = new Scanner(new File(saveFile));
                while(loadResults.hasNextLine())
                {
                    String[] fields = loadResults.nextLine().split("\\s+");
                    if(fields.length >= 2)
                    {
                        matchResults.put(fields[0], Integer.decode(fields[1]));
                    }
                }
                System.err.println(matchResults.size() + " saved results loaded from " + saveFile);
                loadResults.close();
            }
            catch(FileNotFoundException e)
            {
            	System.err.println(saveFile + " not found, re-running all matches.");
            }
        }

        try{
            PrintWriter saveResults = null;
            if(cache > 1)
            {
                saveResults = new PrintWriter(new FileWriter(saveFile, true));  // append mode
            }
            System.out.println("Pairwise Results:");
            for(int i = 0; i < players.size() - 1; i++)
            {
                Player p1 = players.get(i);
                String hash1 = p1.getUniqueHash();
                for(int j = i + 1; j < players.size(); j++)
                {
                    Player p2 = players.get(j);
                    String hash2 = p2.getUniqueHash();
    
                    int hashOrder = Integer.signum(hash1.compareTo(hash2));
                    String matchHash = repeats + ":" +
                        (hashOrder < 0
                        ? hash1 + ":" + hash2
                        : hash2 + ":" + hash1);
                    
                    int result = 0;
                    String newMarker = (cache > 0 ? " (new)" : "");
                    if(matchResults.containsKey(matchHash))
                    {
                        result = -hashOrder * matchResults.get(matchHash).intValue();
                        newMarker = "";
                    }
                    else
                    {
                        Game g = new Game(p1, p2, coreSize, maxTime, debug);
                        for(int r = 0; r < repeats; r++)
                        {
                            result += g.run();
                        }
                        if(repeats == 0)
                        {
                            Game g2 = new Game(p2, p1, coreSize, maxTime, debug);
                            result = g.runAll() - g2.runAll();
                        }
                        if(saveResults != null)
                        {
                            String name1 = (hashOrder < 0 ? p1 : p2).getName();
                            String name2 = (hashOrder < 0 ? p2 : p1).getName();
                            saveResults.println(matchHash + " " + (-hashOrder * result) + " (" + name1 + " vs. " + name2 + ")");
                        }
                        matchResults.put(matchHash, Integer.valueOf(-hashOrder * result));
                    }
                    if(result > repeats)
                    {
                        score.put(p1.getName(), score.get(p1.getName()) + 2);
                        System.out.println(p1.getName() +" > "+ p2.getName() + newMarker);
                    }
                    else if(result < repeats)
                    {
                        score.put(p2.getName(), score.get(p2.getName()) + 2);
                        System.out.println(p2.getName() +" > "+ p1.getName() + newMarker);
                    }
                    else
                    {
                        score.put(p1.getName(), score.get(p1.getName()) + 1);
                        score.put(p2.getName(), score.get(p2.getName()) + 1);
                        System.out.println(p2.getName() +" = "+ p1.getName() + newMarker);
                    }
                }
            }
            if(saveResults != null)
            {
                saveResults.close();
            }
        }
        catch(IOException e)
        {
            System.err.println("Cannot append new results to " + saveFile + ": " + e);
            return;
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
        
        long endTime = System.nanoTime();
        System.out.println("The tournament took " + ((endTime-startTime)/1000000000.0) + " seconds, or " + ((endTime-startTime)/60000000000.0) + " minutes.");

        try{
            PrintWriter saveLeaderboard = null;
            saveLeaderboard = new PrintWriter(new FileWriter(leaderboardFile));
            saveLeaderboard.println("Leaderboard:");
            for(String p : playerNames)
            {
                saveLeaderboard.printf("%5d - %-40s%n",score.get(p),p);
            }
            saveLeaderboard.println("The tournament took " + ((endTime-startTime)/1000000000.0) + " seconds, or " + ((endTime-startTime)/60000000000.0) + " minutes.");
            saveLeaderboard.close();
        }
        catch(IOException e)
        {
            System.err.println("Cannot write leaderboard to " + saveFile + ": " + e);
        }

        
    }
}
