/**
 * This class represents a single instruction line.
 * 
 * @author Ilmari Karonen
 * @version 3/17/15
 */
public final class Instruction
{
    public final int packedOp, field1, field2;
    
    public static final int OP_DAT = 0, OP_MOV = 1, OP_ADD = 2, OP_SUB = 3, OP_JMP = 4, OP_JMZ = 5, OP_CMP = 6;
    public static final int MODE_IMM = 0, MODE_DIR = 1, MODE_IND = 2;

    private static final String[] opNames = {"DAT", "MOV", "ADD", "SUB", "JMP", "JMZ", "CMP"};
    private static final String[] modeNames = {"\t#", "\t", "\t@"};

    public static final int modeBits = 2;
    public static final int modeMask = (1 << modeBits)-1;
    public static final int minValidOp = (1 << (2*modeBits));

    public static final Instruction DAT00 = new Instruction(0, 0, 0);
    
    public Instruction(int packedOp, int field1, int field2)
    {
        this.packedOp = packedOp;
        this.field1 = field1;
        this.field2 = field2;
    }
    public Instruction(Instruction other)
    {
        this.packedOp = other.packedOp;
        this.field1 = other.field1;
        this.field2 = other.field2;
    }
    public Instruction(int opcode, int mode1, int mode2, int field1, int field2)
    {
        mode1 &= modeMask;
        mode2 &= modeMask;
        this.packedOp = (opcode << (2*modeBits)) + (mode1 << modeBits) + mode2;
        this.field1 = field1;
        this.field2 = field2;
    }

    public final int getOpcode()
    {
        return packedOp >> (2*modeBits);
    }
    public final int getMode1()
    {
        return (packedOp >> modeBits) & modeMask;
    }
    public final int getMode2()
    {
        return packedOp & modeMask;
    }
    
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Instruction)) return false;
        Instruction other = (Instruction)obj;
        return (packedOp == other.packedOp && field1 == other.field1 && field2 == other.field2);
    }
    public int hashCode()
    {
        return 31 * packedOp + 17033 * field1 + 99901 * field2;  // random primes
    }
    public String toString()
    {
        return opNames[getOpcode()] + modeNames[getMode1()] + field2 + modeNames[getMode2()] + field2;
    }
}
