import java.io.File;
import java.io.IOException;

public class Main {
    private static CodeWriter codeWriter;
    private static int counter = 0;

    public static void main(String[] args) throws IOException {
        File input = new File(args[0]);
        if (input.isDirectory()) {
            File[] inputListFiles = input.listFiles();
            for (File file : inputListFiles) {
                if (file.getPath().endsWith(".vm")) {
                    counter++;
                }
            }
            codeWriter = new CodeWriter(args[0], false);
            
            for (File file : input.listFiles()) {
                if (!file.getPath().endsWith(".vm")) {
                    continue;
                }
                counter--;
                Parser parser = new Parser(file);
                if (counter > 0) {
                    Translate(parser, codeWriter, false);   
                } else {
                    Translate(parser, codeWriter, true);
                }    
            }
        
        } else {
            Parser parser = new Parser(input);
            codeWriter = new CodeWriter(args[0], true);
            Translate(parser, codeWriter, true);
        }
    }


    public static void Translate(Parser parser, CodeWriter codeWriter, boolean lastFile) throws IOException{

        while (parser.hasMoreLines()) {

            parser.advance();
            Parser.commandType commandType = parser.getCommandType();
            String command;
            
            switch (commandType) {
                case Parser.commandType.C_ARITHMETIC:
                    command = parser.arg1();
                    codeWriter.writeArithmetic(command);
                    break;
            
                case Parser.commandType.C_PUSH:
                case  Parser.commandType.C_POP:
                    codeWriter.writePushPop(parser.getCommandType(), parser.arg1(), parser.arg2());
                    break;
            
                case Parser.commandType.C_LABEL:
                    command = parser.arg1();
                    codeWriter.writeLabel(command);
                    break;
            
                case Parser.commandType.C_GOTO:
                    command = parser.arg1();
                    codeWriter.writeGoto(command);
                    break;
            
                case Parser.commandType.C_IF:
                    command = parser.arg1();
                    codeWriter.writeIf(command);
                    break;
            
                case Parser.commandType.C_FUNCTION:
                    command = parser.arg1();
                    int nVars = parser.arg2();
                    codeWriter.writeFunction(command, nVars);
                    break;
            
                case Parser.commandType.C_CALL:
                    command = parser.arg1();
                    int nArgs = parser.arg2();
                    codeWriter.writeCall(command, nArgs);
                    break;

                case Parser.commandType.C_RETURN:
                    codeWriter.writeReturn();
                    break;
            
                default:
                    break;
            }

        }
        codeWriter.close(lastFile);
    }

}
