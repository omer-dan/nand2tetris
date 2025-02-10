public class Code {

    // According to C instruction table

    public static String dest(String dest) {
        StringBuilder str = new StringBuilder();
        switch (dest) {
            // No destination, the value is not stored anywhere
            case "0":
                return "000";

            // Store the computed value in RAM[A] (memory)
            case "M":
                return "001";

            // Store the computed value in the D register
            case "D":
                return "010";

            // Store the computed value in both RAM[A] and the D register
            case "DM":
                return "011";

            case "MD":
                return "011";

            // Store the computed value in the A register
            case "A":
                return "100";

            // Store the computed value in both the A register and RAM[A]
            case "AM":
                return "101";

            // Store the computed value in both the A register and the D register
            case "AD":
                return "110";

            // Store the computed value in the A register, D register, and RAM[A]
            case "ADM":
                return "111";

            // Invalid input
            default:
                str.delete(0, str.length());
                str.append("ERROR in Code.dest : ");
                str.append(dest + "\n");
                return str.toString();
    }
}


    public static String comp(String comp) {
        StringBuilder str = new StringBuilder();
        switch (comp) {
            case "0":
                return "0101010";

            case "1":
                return "0111111";

            case "-1":
                return "0111010";

            case "D":
                return "0001100";

            case "A":
                return "0110000";

            case "M":
                return "1110000"; // a == 1

            case "!D":
                return "0001101";

            case "!A":
                return "0110001";

            case "!M":
                return "1110001"; // a == 1

            case "-D":
                return "0001111";

            case "-A":
                return "0110011";

            case "-M":
                return "1110011"; // a == 1

            case "D+1":
                return "0011111";

            case "A+1":
                return "0110111";

            case "M+1":
                return "1110111"; // a == 1

            case "D-1":
                return "0001110";

            case "A-1":
                return "0110010";

            case "M-1":
                return "1110010"; // a == 1

            case "D+A":
                return "0000010";

            case "D+M":
                return "1000010"; // a == 1

            case "D-A":
                return "0010011";

            case "D-M":
                return "1010011"; // a == 1

            case "A-D":
                return "0000111";

            case "M-D":
                return "1000111"; // a == 1

            case "D&A":
                return "0000000";

            case "D&M":
                return "1000000"; // a == 1

            case "D|A":
                return "0010101";

            case "D|M":
                return "1010101"; // a == 1

            default:
                str.delete(0, str.length());
                str.append("ERROR in Code.comp : ");
                str.append(comp);
                return str.toString();
        }
    }

    public static String jump(String jump) {
        switch (jump) {
            // No jump, continue executing the next instruction
            case "":
                return "000";
    
            // Jump if the comp > 0
            case "JGT":
                return "001";
    
            // Jump if the comp == 0
            case "JEQ":
                return "010";
    
            // Jump if the comp >= 0
            case "JGE":
                return "011";
    
            // Jump if the comp < 0
            case "JLT":
                return "100";
    
            // Jump if the comp != 0
            case "JNE":
                return "101";
    
            // Jump if the comp <= 0
            case "JLE":
                return "110";
    
            // Unconditional jump
            case "JMP":
                return "111";
    
            // Invalid input
            default:
                return "ERROR in Code.jump";
        }
    }
}
