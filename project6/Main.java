import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static int currentFreeAddress = 16;


    public static void main(String[] args) throws IOException {
        
        // Initialize - open file and construct symbol table (with predefined)
        File input = new File(args[0]);
        Parser parser = new Parser(input);

        StringBuilder outputFile = new StringBuilder();
        outputFile.append(args[0].split("\\.")[0]);
        outputFile.append(".hack");

        FileWriter fileWriter = new FileWriter(outputFile.toString());

        SymbolTable symbolTable = InitSymbolTable();

        FirstPass(symbolTable, parser);
        
        // Begin reading again from start
        parser = new Parser(input);
        SecondPass(symbolTable, parser, fileWriter);

        fileWriter.close();
        
    }

    // Construct a symbol table and add all the predefined HACK symbols
    public static SymbolTable InitSymbolTable() {
        SymbolTable symbolTable = new SymbolTable();
        StringBuilder str = new StringBuilder();
        str.append("R");

        for (int i = 0; i < 16; i++) {
            str.append(i);
            symbolTable.addEntry(str.toString(), i);
            str.delete(1, str.length());
        }

        symbolTable.addEntry("SCREEN", 16384);
        symbolTable.addEntry("KBD", 24576);
        symbolTable.addEntry("SP", 0);
        symbolTable.addEntry("LCL", 1);
        symbolTable.addEntry("ARG", 2);
        symbolTable.addEntry("THIS", 3);
        symbolTable.addEntry("THAT", 4);

        return symbolTable;
    }

    // Reads the program lines,
    // one by one focusing only on (label) declarations.
    // Adds the found labels to the symbol table.
    public static void FirstPass(SymbolTable symbolTable, Parser parser) {
        StringBuilder currentSymbol = new StringBuilder();
        int countLines = 0;

        while (parser.hasMoreLines()) {
            parser.advance();

            if (parser.instrcutionType() == Parser.InstructionTypes.L_INSTRUCTION) {
                currentSymbol.append(parser.symbol());
                symbolTable.addEntry(currentSymbol.toString(), countLines);
                currentSymbol.delete(0, currentSymbol.length());
                countLines--;
            }

            countLines++;
        }
    }

    // While there are more lines to process:
    //   Gets the next instruction, and parses it
    //   If the instruction is @symbol
    //     If symbol is not in the symbol table, adds it to the table
    //     Translates the symbol into its binary value
    //   If the instruction is dest=comp;jump
    //     Translates each of the three fields into its binary value
    //     Assembles the binary values into a string of sixteen 0's and 1's
    //     Writes the string to the output file.
    public static void SecondPass(SymbolTable symbolTable, Parser parser, FileWriter fileWriter) throws IOException{
        StringBuilder currentStr = new StringBuilder();
        StringBuilder instruction = new StringBuilder();

        while (parser.hasMoreLines()) {

            parser.advance();
            
            if (parser.instrcutionType() == Parser.InstructionTypes.A_INSTRUCTION) {
                // Check if symbol is an int or text
                try {
                    instruction.append(AInstructionToBinary(Integer.parseInt(parser.symbol())));

                } catch (NumberFormatException e) {
                    currentStr.append(parser.symbol());

                    if (!symbolTable.contains(currentStr.toString())) {
                        symbolTable.addEntry(currentStr.toString(), currentFreeAddress);
                        instruction.append(AInstructionToBinary(currentFreeAddress++));
                    } else {
                        instruction.append(AInstructionToBinary(symbolTable.getAddress(currentStr.toString())));
                    }

                    currentStr.delete(0, currentStr.length());
                }
                
            } else if (parser.instrcutionType() == Parser.InstructionTypes.C_INSTRUCTION) {
                instruction.append("111");
                instruction.append(Code.comp(parser.comp()));
                instruction.append(Code.dest(parser.dest()));
                instruction.append(Code.jump(parser.jump()));

            } else {
                continue;
            }
            
            fileWriter.write(instruction.toString());
            instruction.delete(0, instruction.length());
            instruction.append("\n");
        }
    }


    public static String AInstructionToBinary(int symbol) {
        return String.format("%16s", Integer.toBinaryString(symbol)).replace(' ', '0');
    }
}
