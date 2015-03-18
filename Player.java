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
        hash = null;
    }
    
    public void addLine(Instruction command)
    {
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
