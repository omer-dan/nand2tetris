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
            codeWriter = new CodeWriter(args[0]);
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
            codeWriter = new CodeWriter(args[0]);
            Translate(parser, codeWriter, true);
        }
    }


    public static void Translate(Parser parser, CodeWriter codeWriter, boolean lastFile) throws IOException{

        while (parser.hasMoreLines()) {

            parser.advance();
            
            if (parser.getCommandType() == Parser.commandType.C_ARITHMETIC) {
                String command = parser.arg1();
                codeWriter.writeArithmetic(command);
                
            } else {
                codeWriter.WritePushPop(parser.getCommandType(), parser.arg1(), parser.arg2());
            }
        }
        codeWriter.close(lastFile);
    }

}
