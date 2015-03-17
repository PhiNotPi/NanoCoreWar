import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
/**
 * This has the tools to parse files into warriors
 * 
 * @author PhiNotPi
 * @version 3/08/15
 */
public class Parser
{
    private static HashMap<String, Integer> opMap = new HashMap<String, Integer>(){{
        put("DAT",  Instruction.OP_DAT);
        put("DATA", Instruction.OP_DAT);
        put("MOV",  Instruction.OP_MOV);
        put("MOVE", Instruction.OP_MOV);
        put("CPY",  Instruction.OP_MOV);
        put("COPY", Instruction.OP_MOV);
        put("ADD",  Instruction.OP_ADD);
        put("SUB",  Instruction.OP_SUB);
        put("SUBTRACT", Instruction.OP_SUB);
        put("JMP",  Instruction.OP_JMP);
        put("JUMP", Instruction.OP_JMP);
        put("JMZ",  Instruction.OP_JMZ);
        put("CMP",  Instruction.OP_CMP);
        put("COMPARE",  Instruction.OP_CMP);
        put("SEQ",  Instruction.OP_CMP);
    }};
    public static int opEncode(String opcode)
    {
        if(opMap.containsKey(opcode))
        {
            return opMap.get(opcode);
        }
        return 0;
    }
    public static Player parseFile(String name, String fileName, boolean verbose)
    {
        Player bot = new Player(name);
        String data = "";
        try{
            data = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
        }
        catch(Exception e){
            System.err.println("Problem reading file " + fileName);
            bot.addLine("dat","0","0");
            return bot;
        }
        ArrayList<String> lines = new ArrayList<String>(Arrays.asList(data.split(String.format("%n"))));
        ArrayList< ArrayList<String>> commands = new ArrayList<ArrayList<String>>();
        
        if(verbose) System.err.println("Original source of " + name);
        for(int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            if(verbose) System.err.println(line);
            line = line.trim().replaceAll(","," ").replaceAll("\\s+", " ").toUpperCase();
            String[] fields = line.split(" ");
            commands.add(new ArrayList<String>(Arrays.asList(line.split(" "))));
            //System.err.println("Checkpoint 1");
        }
        //System.err.println("Checkpoint 2");
        for(int i = 0; i < commands.size(); i++)
        {
            ArrayList<String> command = commands.get(i);
            //System.err.println("Checkpoint 3");
            if(command.size() == 4)
            {
                String lineLabel = command.get(0).toLowerCase();
                command.remove(0);
                //System.err.println("Checkpoint 4");
                for(int j = 0; j < commands.size(); j++)
                {
                    ArrayList<String> command2 = commands.get(j);
                    String field = command2.get(command2.size()-2).toLowerCase();
                    //System.err.println("Checkpoint 5");
                    if(field.equals(lineLabel))
                    {
                        command2.set(command2.size()-2, String.valueOf(i - j));
                        //System.err.println("Checkpoint 6");
                    }
                    if(field.equals("#" + lineLabel))
                    {
                        command2.set(command2.size()-2, "#" + String.valueOf(i));
                    }
                    if(field.equals("@" + lineLabel))
                    {
                        command2.set(command2.size()-2, "@" + String.valueOf(i - j));
                    }
                    
                    
                    field = command2.get(command2.size()-1).toLowerCase();
                    //System.err.println("Checkpoint 5");
                    if(field.equals(lineLabel))
                    {
                        command2.set(command2.size()-1, String.valueOf(i - j));
                        //System.err.println("Checkpoint 6");
                    }
                    if(field.equals("#" + lineLabel))
                    {
                        command2.set(command2.size()-1, "#" + String.valueOf(i));
                    }
                    if(field.equals("@" + lineLabel))
                    {
                        command2.set(command2.size()-1, "@" + String.valueOf(i - j));
                    }
                }
            }
        }
        
        if(verbose) System.err.println("Preprocessed source of " + name);
        for(ArrayList<String> command : commands)
        {
            bot.addLine(command.get(0),command.get(1),command.get(2));
            if(verbose) System.err.println(command.get(0) + " " + command.get(1) + " " + command.get(2));
        }
        if(verbose) System.err.println("Compiled code of " + name);
        for(Instruction command : bot.getCode())
        {
            if(verbose) System.err.println(command.getOpcode() + " " + command.getMode1() + " " + command.getMode2() + " " + command.field1 + " " + command.field2);
        }
        if(verbose) System.err.println("Hash of " + name + ": " + bot.getUniqueHash());

        return bot;
    }
}
