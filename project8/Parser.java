import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Parser {
    private Scanner scanner;
    private String currentInstruction;

    // Define InstructionTypes
    public enum commandType {
            C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
    }

    // Opens the file/stream and gets ready to parse it
    public Parser(File inFile) throws IOException{
        scanner = new Scanner(inFile);
    }

    // Return True if thare are more lines in the file
    public boolean hasMoreLines() {
        if (scanner.hasNextLine())
            return true;
        return false;
    }

    // Skips over whitespace and comments, if necessary.
    // Reads the next instruction from the input, and makes it the current instruction.
    // This method should be called only if hasMoreLines is true.
    // Initially there is no current instruction.
    public void advance() {
        currentInstruction = scanner.nextLine();  // Read the next line
        currentInstruction = currentInstruction.split("//")[0].trim(); // Remove comments and trim
        while ((currentInstruction.isBlank() || currentInstruction.startsWith("//")) && hasMoreLines()) {
            currentInstruction = scanner.nextLine();
            currentInstruction = currentInstruction.split("//")[0].trim(); // Remove comments and trim again
        }
    }

    public commandType getCommandType() {
        String command = currentInstruction.split(" ")[0];
        switch (command) {
            case "push":
                return commandType.C_PUSH;
            
            case "pop":
                return commandType.C_POP;
        
            case "label":
                return commandType.C_LABEL;
        
            case "if-goto":
                return commandType.C_IF;
        
            case "goto":
                return commandType.C_GOTO;
        
            case "function":
                return commandType.C_FUNCTION;
        
            case "return":
                return commandType.C_RETURN;
        
            case "call":
                return commandType.C_CALL;
        
            default:
                return commandType.C_ARITHMETIC;
        }
    }
    

    // Should not be called if the current command is C_RETURN.
    public String arg1() {
        if (this.getCommandType() == commandType.C_ARITHMETIC) {
            return currentInstruction;
        } else {
            return currentInstruction.split(" ")[1];
        }
    }

    public int arg2() {
        String[] instruction = currentInstruction.split(" ");
        return Integer.parseInt(instruction[instruction.length - 1]);
    }
}

