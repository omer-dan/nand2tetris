import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Parser {
    private Scanner scanner;
    private String currentInstruction;

    // Define InstructionTypes
    public enum commandType {
            C_ARITHMETIC, C_PUSH, C_POP
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
        currentInstruction = scanner.nextLine();
        while((currentInstruction.contains("//") || currentInstruction.isBlank()) && hasMoreLines())
        {
            currentInstruction = scanner.nextLine();
        }
        currentInstruction = currentInstruction.trim();
    }

    public commandType getCommandType() {
        switch (currentInstruction.split(" ")[0]) {
            case "push":
                return commandType.C_PUSH;
            case "pop":
                return commandType.C_POP;
        
            default:
                return commandType.C_ARITHMETIC;
        }
    }

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

