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
    private ArrayList<Instruction> codeList;
        
    public Player(String name)
    {
        this.name = name;
        codeList = new ArrayList<Instruction>();
    }
    
    public void addLine(String opcode, String fieldA, String fieldB)
    {
        int modeA = Instruction.MODE_DIR;
        int valA = 0;
        fieldA = fieldA.trim();
        if(fieldA.charAt(0) == '#')
        {
            modeA = Instruction.MODE_IMM;
            fieldA = fieldA.substring(1);
        }
        else if(fieldA.charAt(0) == '@')
        {
            modeA = Instruction.MODE_IND;
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
        int modeB = Instruction.MODE_DIR;
        int valB = 0;
        fieldB = fieldB.trim();
        if(fieldB.charAt(0) == '#')
        {
            modeB = Instruction.MODE_IMM;
            fieldB = fieldB.substring(1);
        }
        else if(fieldB.charAt(0) == '@')
        {
            modeB = Instruction.MODE_IND;
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
        int op = Parser.opEncode(opcode.trim().toUpperCase());
        Instruction command = new Instruction(op, modeA, modeB, valA, valB);
        codeList.add(command);
        hash = null;  // invalidate old hash
    }
    public ArrayList<Instruction> getCode()
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
	        for (Instruction command : codeList)
	        {
                ByteBuffer byteBuf = ByteBuffer.allocate(3 * 4);
                IntBuffer intBuf = byteBuf.asIntBuffer();
                intBuf.put(command.packedOp);
                intBuf.put(command.field1);
                intBuf.put(command.field2);
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
