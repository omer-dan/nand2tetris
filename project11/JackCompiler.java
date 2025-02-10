import java.io.File;
import java.util.ArrayList;

public class JackCompiler {
    
    /**
     * The function goes through all the files in the folder and takes only the .jack files
     */
    public static ArrayList<File> getJackFiles(File directory){
        File[] files = directory.listFiles();
        ArrayList<File> result = new ArrayList<File>();

        if (files == null) {
            return result;
        }

        for (File f:files) {
            if (f.getName().endsWith(".jack")) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * We receive as input the files in jack language and turn each file in jack into a VM file, with the help of JackTokenizer and CompilationEngine calsses.
     * At the end we return a VM file for each jack file. 
     */
    public static void main(String[] args) {
            String fileInName = args[0];
            File input = new File(fileInName);

            String fileOutPath = "";
            File out;

            ArrayList<File> jackFiles = new ArrayList<File>();

            if (input.isFile()) {
                jackFiles.add(input);
            }
            else if (input.isDirectory()) {
                jackFiles = getJackFiles(input);
            }

            for (File f: jackFiles) {
                fileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + ".vm";
                out = new File(fileOutPath);
                CompilationEngine compilationEngine = new CompilationEngine(f, out);
                compilationEngine.compileClass();
            }
    }
}
