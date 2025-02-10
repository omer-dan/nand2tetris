import java.io.File;
import java.io.FileWriter;
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
    private static JackTokenizer jackTokenizer;
    private static int counter = 0;
    private static FileWriter fileWriter;
    private static String fileName;
    private static boolean firstFile = true;

    public static void main(String[] args) throws IOException {
        String name;
        String inputFile = args[0];

        String[] strArr = inputFile.split("/");
        int len = strArr.length;
        fileName = strArr[len-1].split("\\.")[0];
        
        if (!inputFile.endsWith(".jack")) {
            
            name = inputFile + "/" + fileName + ".xml";
        
        } else {
            name = inputFile.replace(".jack", "") + ".xml";
        } 
        fileWriter = new FileWriter(name);

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
                counter--;
                jackTokenizer = new JackTokenizer(file.getPath());
                if (counter > 0) {
                    Translate(jackTokenizer, false, firstFile);   
                    firstFile = false;
                // last file  
                } else {
                    Translate(jackTokenizer, true, firstFile);
                }    
            }
        
        } else {
            jackTokenizer = new JackTokenizer(inputFile);
            Translate(jackTokenizer, true, true);
        }
    }


    /**
     * Translates Jack code using the provided tokenizer and compilation engine.
     * 
     * @param jackTokenizer The tokenizer used to parse Jack code tokens
     * @param lastFile Boolean flag indicating if this is the last file being processed
     * @param firstFile Boolean flag indicating if this is the first file being processed
     * @throws IOException If an I/O error occurs while writing to output file
     */
    public static void Translate(JackTokenizer jackTokenizer, boolean lastFile, boolean firstFile) throws IOException{
        CompilationEngine compilationEngine = new CompilationEngine(jackTokenizer, fileWriter);
        compilationEngine.compileClass();

        if(lastFile) {
            // fileWriter.write("</tokens>\n");
            fileWriter.close();
        }
    }
}