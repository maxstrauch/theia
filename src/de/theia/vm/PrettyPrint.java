package de.theia.vm;

/**
 * Pretty prints (decompiles) the bytecode to a readable format
 * 
 * @author maximilianstrauch
 */
public class PrettyPrint {
    
    /**
     * Formats an immedate or register
     * 
     * @param x The VAR or NUM to format
     * @return The string representation of x
     */
    private static String formatNumReg(int x) {
        if ((x >> 31) != 0) {
            // Register
            return "x" + (x & 0x7fffffff);
        } else {
            // Immediate value
            return String.valueOf(x & 0x7fffffff);
        }
    }
    
    /**
     * Pretty prints the bytecode
     * 
     * @param bytecode The bytecode to pretty print
     * @return The string representation
     */
    public static String print(int[] bytecode) {
        StringBuilder buf = new StringBuilder();
        buf.append("Code:\n");
        
        int ln = 0, tmp;
        for (int i = 0; i < bytecode.length; i++) {
            
            buf.append(String.format("%3d", i));
            buf.append(": ");
            
            switch (bytecode[i]) {
                
                case 0x2a:
                case 0x2b:
                case 0x2c:
                    switch (bytecode[i] - 0x2a) {
                        case 0: buf.append("add "); break;
                        case 1: buf.append("sub "); break;
                        case 2: buf.append("mul "); break;
                    }
                    
                    buf.append(formatNumReg(bytecode[++i])); // arg1
                    buf.append(", ");
                    buf.append(formatNumReg(bytecode[++i])); // arg2
                    buf.append(", ");
                    buf.append(formatNumReg(bytecode[++i])); // dst
                    break;
                
                case 0x2d:
                    buf.append("mov ");
                    buf.append(formatNumReg(bytecode[++i])); // src
                    buf.append(", ");
                    buf.append(formatNumReg(bytecode[++i])); // dst
                    break;
                    
                case 0x10:
                    buf.append(String.format("push x%d", bytecode[++i] & 0x7fffffff));
                    break;
                    
                case 0x11:
                    buf.append("pop");
                    break;
                
                case 0x12:
                    buf.append("dec");
                    break;
                    
                case 0x13:
                    buf.append(String.format("bz #%d", bytecode[++i]));
                    break;
                    
                case 0x99:
                    buf.append("nop");
                    break;
                    
                case 0x21:
                    buf.append(String.format("goto #%d", bytecode[++i]));
                    break;
                    
                case 0x42:
                    buf.append("ifneq ");
                    buf.append(formatNumReg(bytecode[++i])); // arg1
                    buf.append(", ");
                    buf.append(formatNumReg(bytecode[++i])); // arg2
                    buf.append(", #");
                    buf.append(bytecode[++i]); // addr
                    break;
                    
                case 0x43:
                    buf.append("ifgt ");
                    buf.append(formatNumReg(bytecode[++i])); // arg1
                    buf.append(", ");
                    buf.append(formatNumReg(bytecode[++i])); // arg2
                    buf.append(", #");
                    buf.append(bytecode[++i]); // addr
                    break;
                    
                case 0x44:
                    buf.append("ifeq ");
                    buf.append(formatNumReg(bytecode[++i])); // arg1
                    buf.append(", ");
                    buf.append(formatNumReg(bytecode[++i])); // arg2
                    buf.append(", #");
                    buf.append(bytecode[++i]); // addr
                    break;
                    
                    
                default:
                    buf.append("<Unkown opcode>");
                    break;
            }
            
            
            buf.append("\n");
        }

        return buf.toString();
    }
    
}
