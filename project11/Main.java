import java.io.File;
import java.io.IOException;

/**
 * Main class for the Jack Compiler.
 * This class processes .jack files and compiles them into XML format.
 * It can handle both single files and directories containing multiple .jack files.
 *
 * The program accepts a command line argument which can be either:
 * 1. A path to a single .jack file
 * 2. A path to a directory containing one or more .jack files
 *
 * For each input file, it creates a corresponding .xml output file with the same base name.
 *
 */
public class Main {
    private static int counter = 0;
    private static String fileName;

    public static void main(String[] args) throws IOException {
        String name;
        String inputFile = args[0];
        String[] strArr = inputFile.split("/");
        int len = strArr.length;
        fileName = strArr[len-1].split("\\.")[0];

        if (!inputFile.endsWith(".jack")) {

            name = inputFile + "/" + fileName + ".vm";

        } else {
            name = inputFile.replace(".jack", "") + ".vm";
        }

        File outFile = new File(name);
        File input = new File(inputFile);
        if (input.isDirectory()) {
            File[] inputListFiles = input.listFiles();
            for (File file : inputListFiles) {
                if (file.getPath().endsWith(".jack")) {
                    counter++;
                }
            }
            
            for (File file : input.listFiles()) {
                if (!file.getPath().endsWith(".jack")) {
                    continue;
                }

                if (counter == 1) {
                    name = file.getPath().replace(".jack", ".vm");
                    outFile = new File(name);
                    Translate(file, outFile);
                    return;
                } else {
                    Translate(file, outFile);
                }
                
            }

        } else {
            Translate(input, outFile);
        }
    }


    /**
     * Translates a single .jack file into a .xml file.
     * @param file The input .jack file
     * @param outFile The output .xml file
     * @throws IOException
     */
    public static void Translate(File file, File outFile) throws IOException{
        CompilationEngine compilationEngine = new CompilationEngine(file, outFile);
        compilationEngine.compileClass();
    }
}