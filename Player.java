import java.util.ArrayList;
import java.util.HashMap;
/**
 * This contains the compiled code for each warror and a method for adding a line with three arguments.
 * 
 * @author PhiNotPi
 * @version 3/08/15
 */
public class Player
{
    private String name;
    private ArrayList<int []> code;
    
    boolean isChallenger;
    
    public Player(String name)
    {
        this.name = name;
        code = new ArrayList<int []>();
        isChallenger = true; //will be set to false by the controller if its name is already on the leaderboard
    }
    
    public void addLine(String opcode, String fieldA, String fieldB)
    {
        int[] command = new int[5];
        command[0] = Parser.opEncode(opcode.trim().toUpperCase());
        int modeA = 1;
        int valA = 0;
        fieldA = fieldA.trim();
        if(fieldA.charAt(0) == '#')
        {
            modeA = 0;
            fieldA = fieldA.substring(1);
        }
        else if(fieldA.charAt(0) == '@')
        {
            modeA = 2;
            fieldA = fieldA.substring(1);
        }
        try
        {
            valA = Integer.parseInt(fieldA);
        }
        catch(Exception e)
        {
            System.out.println("Player " + name + " had a problem at line " + code.size() + ", field A.");
        }
        int modeB = 1;
        int valB = 0;
        fieldB = fieldB.trim();
        if(fieldB.charAt(0) == '#')
        {
            modeB = 0;
            fieldB = fieldB.substring(1);
        }
        else if(fieldB.charAt(0) == '@')
        {
            modeB = 2;
            fieldB = fieldB.substring(1);
        }
        try
        {
            valB = Integer.parseInt(fieldB);
        }
        catch(Exception e)
        {
            System.out.println("Player " + name + " had a problem at line " + code.size() + ", field B");
        }
        command[1] = modeA;
        command[2] = modeB;
        command[3] = valA;
        command[4] = valB;
        code.add(command);
    }
    public ArrayList<int[]> getCode()
    {
        return code;
    }
    public String getName()
    {
        return name;
    }
}
