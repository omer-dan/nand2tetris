import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

/*
 * A simple module that writes individual VM commands to the output .vm file
 */
public class VMWriter {
    public enum SEGMENT { CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP, NONE };
    public enum COMMAND { ADD, SUB, NEG, EQ , GT, LT, AND, OR, NOT };
    private HashMap<SEGMENT,String> segmentHashMap = new HashMap<SEGMENT, String>();
    private HashMap<COMMAND,String> commandHashMap = new HashMap<COMMAND, String>();
    private PrintWriter printWriter;


    public VMWriter(File OutFile) {
        try {
            printWriter = new PrintWriter(OutFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        initializeHash();
    }

    public void writePush(SEGMENT seg, int index) {
        writeCommand("push", segmentHashMap.get(seg),String.valueOf(index));
    }

    public void writePop(SEGMENT seg, int index) {
        writeCommand("pop", segmentHashMap.get(seg),String.valueOf(index));
    }

    public void writeArithmetic(COMMAND commands){
        writeCommand(commandHashMap.get(commands),"","");
    }

    public void writeLabel(String label){
        writeCommand("label",label,"");
    }

    public void writeGoto(String label){
        writeCommand("goto",label,"");
    }

    public void writeIf(String label){
        writeCommand("if-goto",label,"");
    }

    public void writeCall(String name, int args){
        writeCommand("call",name,String.valueOf(args));
    }

    public void writeFunction(String name, int local){
        writeCommand("function",name,String.valueOf(local));
    }

    public void writeReturn(){
        writeCommand("return","","");
    }

    public void writeCommand(String str, String arg_1, String arg_2){
        printWriter.print(str + " " + arg_1 + " " + arg_2 + "\n");
    }

    public void close(){
        printWriter.close();
    }

    private void initializeHash(){
        String[][] segmentArray = {
            {"constant", "CONST"}, {"argument", "ARG"}, {"local", "LOCAL"},
            {"static", "STATIC"}, {"this", "THIS"}, {"that", "THAT"},
            {"pointer", "POINTER"}, {"temp", "TEMP"}
        };
    
        for (String[] pairSeg : segmentArray) {
            segmentHashMap.put(SEGMENT.valueOf(pairSeg[1]), pairSeg[0]);
        }
    
        String[][] commandArray = {
            {"add", "ADD"}, {"sub", "SUB"}, {"neg", "NEG"},
            {"eq", "EQ"}, {"gt", "GT"}, {"lt", "LT"},
            {"and", "AND"}, {"or", "OR"}, {"not", "NOT"}
        };
    
        for (String[] pair : commandArray) {
            commandHashMap.put(COMMAND.valueOf(pair[1]), pair[0]);
        }
    }
}