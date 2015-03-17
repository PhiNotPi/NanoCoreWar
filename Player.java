import java.util.ArrayList;
import java.util.HashMap;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
/**
 * This contains the compiled code for each warror and a method for adding a line with three arguments.
 * 
 * @author PhiNotPi, Ilmari Karonen
 * @version 3/17/15
 */
public class Player
{
    private String name, hash;
    private ArrayList<int []> codeList;
    
    private static final int opBits = 3, modeBits = 2;
    
    public Player(String name)
    {
        this.name = name;
        codeList = new ArrayList<int []>();
    }
    
    public void addLine(String opcode, String fieldA, String fieldB)
    {
        int[] command = new int[3];
        int modeA = Parser.MODE_DIR;
        int valA = 0;
        fieldA = fieldA.trim();
        if(fieldA.charAt(0) == '#')
        {
            modeA = Parser.MODE_IMM;
            fieldA = fieldA.substring(1);
        }
        else if(fieldA.charAt(0) == '@')
        {
            modeA = Parser.MODE_IND;
            fieldA = fieldA.substring(1);
        }
        try
        {
            valA = Integer.parseInt(fieldA);
        }
        catch(Exception e)
        {
            System.out.println("Player " + name + " had a problem at line " + codeList.size() + ", field A.");
        }
        int modeB = Parser.MODE_DIR;
        int valB = 0;
        fieldB = fieldB.trim();
        if(fieldB.charAt(0) == '#')
        {
            modeB = Parser.MODE_IMM;
            fieldB = fieldB.substring(1);
        }
        else if(fieldB.charAt(0) == '@')
        {
            modeB = Parser.MODE_IND;
            fieldB = fieldB.substring(1);
        }
        try
        {
            valB = Integer.parseInt(fieldB);
        }
        catch(Exception e)
        {
            System.out.println("Player " + name + " had a problem at line " + codeList.size() + ", field B");
        }
        command[0] = Parser.opEncode(opcode.trim().toUpperCase(), modeA, modeB);
        command[1] = valA;
        command[2] = valB;
        codeList.add(command);
        hash = null;  // invalidate old hash
    }
    public ArrayList<int[]> getCode()
    {
        return codeList;
    }
    public String getName()
    {
        return name;
    }
    public String getUniqueHash()
    {
        if (hash != null) return hash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
	        for (int [] command : codeList)
	        {
                ByteBuffer byteBuf = ByteBuffer.allocate(command.length * 4);
                IntBuffer intBuf = byteBuf.asIntBuffer();
                intBuf.put(command);
                md.update(byteBuf.array());
            }
            byte[] digest = md.digest();
        
       	    char[] hexDigest = new char[digest.length * 2];
            char[] hexChars = "0123456789abcdef".toCharArray();
            for(int i = 0; i < digest.length; i++)
            {
                hexDigest[2*i] = hexChars[(digest[i] >> 4) & 0xF];
                hexDigest[2*i+1] = hexChars[digest[i] & 0xF];
            }
            hash = new String(hexDigest);
            return hash;
        }
        catch (java.security.NoSuchAlgorithmException e)
        {
            return null;  // should never happen
        }
    }
}
