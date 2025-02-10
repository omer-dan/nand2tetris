import java.io.File;
import java.io.IOException;
import java.util.Scanner;


public class Parser {
    private static Scanner scanner;
    private static String currentInstruction;

    // Define InstructionTypes
    public enum InstructionTypes {
            A_INSTRUCTION, C_INSTRUCTION, L_INSTRUCTION
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

    // Returns the type of the current instruction:
    // A_INSTRUCTION for @xxx, where xxx is either a decimal number or a symbol.
    // C_INSTRUCTION for dest=comp;jump
    // L_INSTRUCTION for (xxx), where xxx is a symbol.
    public InstructionTypes instrcutionType() {
        switch (currentInstruction.charAt(0)) {
            case '@':
                return InstructionTypes.A_INSTRUCTION;
            case '(':
                return InstructionTypes.L_INSTRUCTION;
        
            default:
                return InstructionTypes.C_INSTRUCTION;
        }
    }

    // If the current instruction is (xxx), returns the symbol xxx.
    // If the current instruction is @xxx, returns the symbol or decimal xxx (as a string).
    // Should be called only if instructionType is A_INSTRUCTION or L_INSTRUCTION.
    public String symbol() {
        switch (currentInstruction.charAt(0)) {
            case '@':
                return currentInstruction.substring(1);
        
            default:
                return currentInstruction.replace("(", "").replace(")", "");
        }
    }

    // Returns the symbolic dest part of the current C-instruction (8 possibilities).
    // Should be called only if instructionType is C_INSTRUCTION.
    public String dest() {
        return currentInstruction.contains("=") ?
                currentInstruction.split("=")[0].split(";")[0] : "0";
    }

    // Returns the symbolic comp part of the current C-instruction (28 possibilities).
    // Should be called only if instructionType is C_INSTRUCTION.
    public String comp() {
        return currentInstruction.contains("=") ?
                currentInstruction.split("=")[1].split(";")[0] :
                currentInstruction.split(";")[0];
    }

    // Returns the symbolic jump part of the current C-instruction (8 possibilities).
    // Should be called only if instructionType is C_INSTRUCTION.
    public String jump() {
        return currentInstruction.split(";").length > 1 ? currentInstruction.split(";")[1] : "";
    }
    
}