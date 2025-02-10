import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CodeWriter {
    private FileWriter fileWriter;
    private String fileName;
    private String currentFuncName;
    private int lineCounter = 0;
    private StringBuilder stringToWrite = new StringBuilder();
    private Boolean bootStrapped = false;
    private HashMap<String, Integer> labelMap = new HashMap<>();

    public CodeWriter(String outputFileName, Boolean singleFile) throws IOException{
        String name;

        String[] strArr = outputFileName.split("/");
        int len = strArr.length;
        fileName = strArr[len-1].split("\\.")[0];
        
        if (!outputFileName.endsWith(".vm")) {
            
            name = outputFileName + "/" + fileName + ".asm";
        
        } else {
            name = outputFileName.replace(".vm", "") + ".asm";
        }
        
        fileWriter = new FileWriter(name);
        // ./staticboi.vm


        if (singleFile == false && !bootStrapped) {
            // Bootstrap Code
            bootStrapped = true;
            stringToWrite.append("// bootstrap" + "\n");
            appendToStringBuilder(stringToWrite, "@256" + "\n");
            appendToStringBuilder(stringToWrite, "D=A" + "\n");
            appendToStringBuilder(stringToWrite, "@SP" + "\n");
            appendToStringBuilder(stringToWrite, "M=D" + "\n");
            currentFuncName = "Sys.init";
            writeCall(currentFuncName, 0);
        }
    }
    
    public void close(boolean lastFile) throws IOException {
        appendToStringBuilder(stringToWrite, "@" + lineCounter + "\n");
        appendToStringBuilder(stringToWrite, "0;JMP");
        if (!lastFile) { 
            stringToWrite.append("\n");
        }
        
        if (lastFile) {
            fileWriter.write(stringToWrite.toString());
            fileWriter.close();
        }
    }

    private void appendToStringBuilder(StringBuilder stringBuilder, String string) {
        stringBuilder.append(string);
        lineCounter++;
    }
    
    public void writeLabel(String command) {
        stringToWrite.append("// label " + command + "\n");

        // Doesn't count as a row
        stringToWrite.append("(" + fileName + "." + currentFuncName + "$" + command + ")" + "\n"); // (Sys.init$label)
    }

    public void writeGoto(String command) {
        stringToWrite.append("// goto " + command + "\n");
        appendToStringBuilder(stringToWrite, "@" + fileName + "." + currentFuncName + "$" + command + "\n");
        appendToStringBuilder(stringToWrite, "0;JMP" + "\n");

        // @Sys.init$label
        // 0;JMP
    }

    public void writeIf(String command) {
        stringToWrite.append("// if-goto " + command + "\n");
        appendToStringBuilder(stringToWrite, "@SP" + "\n");
        appendToStringBuilder(stringToWrite, "M=M-1" + "\n");
        appendToStringBuilder(stringToWrite, "A=M" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@" + fileName + "." + currentFuncName + "$" + command + "\n");
        appendToStringBuilder(stringToWrite, "D;JNE" + "\n");
    }

    public void writeFunction(String command, int nVars) {
        fileName = command.split("\\.")[0];
        currentFuncName = command.split("\\.")[1];
        stringToWrite.append("// function " + command + " " + nVars + "\n");
        stringToWrite.append("(" + command + ")" + "\n");
        for (int i = 0; i < nVars; i++) {
            appendToStringBuilder(stringToWrite, "@LCL" + "\n");
            appendToStringBuilder(stringToWrite, "D=M" + "\n");
            appendToStringBuilder(stringToWrite, "@0" + "\n");
            appendToStringBuilder(stringToWrite, "A=D+A" + "\n");
            appendToStringBuilder(stringToWrite, "M=0" + "\n");
            appendToStringBuilder(stringToWrite, "@SP" + "\n");
            appendToStringBuilder(stringToWrite, "M=M+1" + "\n");
        }
    }

    public void writeCall(String command, int nArgs) {
        fileName = command.split("\\.")[0];
        currentFuncName = command.split("\\.")[1];
        stringToWrite.append("// call " + command + "\n");

        int returnCount = labelMap.getOrDefault(command, 0);
        labelMap.put(command, ++returnCount);

        String returnAdr = command + "$ret." + returnCount;

        appendToStringBuilder(stringToWrite, "@" + returnAdr + "\n");
        appendToStringBuilder(stringToWrite, "D=A\n");
        appendToStringBuilder(stringToWrite, "@SP\n");
        appendToStringBuilder(stringToWrite, "A=M\n");
        appendToStringBuilder(stringToWrite, "M=D\n");
        appendToStringBuilder(stringToWrite, "@SP\n");
        appendToStringBuilder(stringToWrite, "M=M+1\n");

        functionPush("LCL");
        functionPush("ARG");
        functionPush("THIS");
        functionPush("THAT");

        appendToStringBuilder(stringToWrite, "@SP" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@LCL" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        appendToStringBuilder(stringToWrite, "@5" + "\n");
        appendToStringBuilder(stringToWrite, "D=D-A" + "\n");

        appendToStringBuilder(stringToWrite, "@" + nArgs + "\n");
        appendToStringBuilder(stringToWrite, "D=D-A" + "\n");
        appendToStringBuilder(stringToWrite, "@ARG" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        appendToStringBuilder(stringToWrite, "@" + fileName + "." + currentFuncName + "\n");
        appendToStringBuilder(stringToWrite, "0;JMP" + "\n");

        stringToWrite.append("(" + returnAdr + ")" + "\n"); // (Sys.init$ret.1)
        
    }

    private void functionPush(String location) {
        stringToWrite.append("// push " + location + "\n");
        appendToStringBuilder(stringToWrite, "@" + location + "\n");
        appendToStringBuilder(stringToWrite, "D=M\n");
        appendToStringBuilder(stringToWrite, "@SP\n");
        appendToStringBuilder(stringToWrite, "A=M\n");
        appendToStringBuilder(stringToWrite, "M=D\n");
        appendToStringBuilder(stringToWrite, "@SP\n");
        appendToStringBuilder(stringToWrite, "M=M+1\n");
    }
    
    public void writeReturn() {
        // currentFuncName = "";
        stringToWrite.append("// return \n");

        // Save endFrame and retAddr
        appendToStringBuilder(stringToWrite, "@LCL" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@endFrame" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");
        appendToStringBuilder(stringToWrite, "@5" + "\n");
        appendToStringBuilder(stringToWrite, "A=D-A" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@retAddr" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        // Pop into RAM[ARG] -> *ARG = pop
        appendToStringBuilder(stringToWrite, "@SP" + "\n");
        appendToStringBuilder(stringToWrite, "M=M-1" + "\n");
        appendToStringBuilder(stringToWrite, "A=M" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@ARG" + "\n");
        appendToStringBuilder(stringToWrite, "A=M" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        // Reposition
        appendToStringBuilder(stringToWrite, "@ARG" + "\n");
        appendToStringBuilder(stringToWrite, "D=M+1" + "\n");
        appendToStringBuilder(stringToWrite, "@SP" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        // Restoration
        appendToStringBuilder(stringToWrite, "@endFrame" + "\n");
        appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@THAT" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        appendToStringBuilder(stringToWrite, "@endFrame" + "\n");
        appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@THIS" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        appendToStringBuilder(stringToWrite, "@endFrame" + "\n");
        appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@ARG" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        appendToStringBuilder(stringToWrite, "@endFrame" + "\n");
        appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
        appendToStringBuilder(stringToWrite, "D=M" + "\n");
        appendToStringBuilder(stringToWrite, "@LCL" + "\n");
        appendToStringBuilder(stringToWrite, "M=D" + "\n");

        appendToStringBuilder(stringToWrite, "@retAddr" + "\n");
        appendToStringBuilder(stringToWrite, "A=M" + "\n");
        appendToStringBuilder(stringToWrite, "0;JMP" + "\n");
    }

    public void writeArithmetic(String command) {
        stringToWrite.append("// " + command + "\n");

        switch (command) {
            case "add":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "M=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "A=M" + "\n");
                appendToStringBuilder(stringToWrite, "D=M" + "\n");
                appendToStringBuilder(stringToWrite, "A=A-1" + "\n");
                appendToStringBuilder(stringToWrite, "M=D+M" + "\n");
                break;
            
            case "sub":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "M=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "A=M" + "\n");
                appendToStringBuilder(stringToWrite, "D=M" + "\n");
                appendToStringBuilder(stringToWrite, "A=A-1" + "\n");
                appendToStringBuilder(stringToWrite, "M=M-D" + "\n");
                break;
        
            
            case "neg":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "A=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "M=-M" + "\n");
                break;
        
            
            case "eq":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "D=M" + "\n");    
                appendToStringBuilder(stringToWrite, "A=A-1" + "\n");    
                appendToStringBuilder(stringToWrite, "D=M-D" + "\n");    
                appendToStringBuilder(stringToWrite, "M=0" + "\n");    
                appendToStringBuilder(stringToWrite, "@" + (lineCounter + 5) + "\n");    
                appendToStringBuilder(stringToWrite, "D;JNE" + "\n");    
                appendToStringBuilder(stringToWrite, "@SP" + "\n");    
                appendToStringBuilder(stringToWrite, "A=M-1" + "\n");    
                appendToStringBuilder(stringToWrite, "M=-1" + "\n");    
                break;
            
            case "gt":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "D=M" + "\n");
                appendToStringBuilder(stringToWrite, "A=A-1" + "\n");
                appendToStringBuilder(stringToWrite, "D=M-D" + "\n");
                appendToStringBuilder(stringToWrite, "M=0" + "\n");
                appendToStringBuilder(stringToWrite, "@" + (lineCounter + 5) + "\n");
                appendToStringBuilder(stringToWrite, "D;JLE" + "\n");
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "A=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "M=-1" + "\n");
                break;
        
            case "lt":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "D=M" + "\n");
                appendToStringBuilder(stringToWrite, "A=A-1" + "\n");
                appendToStringBuilder(stringToWrite, "D=M-D" + "\n");
                appendToStringBuilder(stringToWrite, "M=0" + "\n");
                appendToStringBuilder(stringToWrite, "@" + (lineCounter + 5) + "\n");
                appendToStringBuilder(stringToWrite, "D;JGE" + "\n");
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "A=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "M=-1" + "\n");
                break;
        
            case "and":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "D=M" + "\n");
                appendToStringBuilder(stringToWrite, "A=A-1" + "\n");
                appendToStringBuilder(stringToWrite, "M=D&M" + "\n");
                break;
            
            case "or":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "AM=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "D=M" + "\n");
                appendToStringBuilder(stringToWrite, "A=A-1" + "\n");
                appendToStringBuilder(stringToWrite, "M=D|M" + "\n");
                break;
            
            case "not":
                appendToStringBuilder(stringToWrite, "@SP" + "\n");
                appendToStringBuilder(stringToWrite, "A=M-1" + "\n");
                appendToStringBuilder(stringToWrite, "M=!M" + "\n");
                break;
        
            default:
                appendToStringBuilder(stringToWrite, command + " NOTHING HAPPENED\n");
                break;
        }


    }

    public void writePushPop(Parser.commandType commandType, String segment, int offset) {
        Boolean simpleseg = false;
        Boolean constant = false;
        Boolean staticCommand = false;
        Boolean thisThat = false;
        Boolean temp = false;

        StringBuilder address = new StringBuilder();
        switch (segment) {
            case "local":
                address.append("@LCL");
                simpleseg = true;
                break;
            
            case "argument":
                address.append("@ARG");
                simpleseg = true;
                break;
            
            case "this":
                address.append("@THIS");
                thisThat = true;
                break;
            
            case "that":
                address.append("@THAT");
                thisThat = true;
                break;

            case "pointer":
                address.append("@POINTER" + offset);
                break;

            case "static":
                address.append("@" + fileName + "." + offset);
                staticCommand = true;
                break;
                
            case "temp":
                address.append("@" + (5 + offset));
                temp = true;
                break;
                
            case "constant":
                address.append("@" + offset);
                constant = true;
                break;
        
            default:
                appendToStringBuilder(stringToWrite, segment + " " + offset + " NOTHING HAPPENED\n");
                break;
        }

        if (commandType == Parser.commandType.C_PUSH) {
            stringToWrite.append("// push " + segment + " " + offset + "\n");
            
            if (simpleseg) {
                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
                appendToStringBuilder(stringToWrite, "@" + offset + "\n");
                appendToStringBuilder(stringToWrite, "D=D+A\n");
                appendToStringBuilder(stringToWrite, "A=D\n");
                appendToStringBuilder(stringToWrite, "D=M\n");

            } else if (thisThat) {
                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
                appendToStringBuilder(stringToWrite, "@" + offset + "\n");
                appendToStringBuilder(stringToWrite, "A=D+A\n");
                appendToStringBuilder(stringToWrite, "D=M\n");

            } else if (constant) {
                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "D=A\n");

            } else if (staticCommand) {
                
                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "D=M\n");

            } else if (temp) {
                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
 
            } else if (address.toString().contains("@POINTER")) {
                if (offset == 0) {
                    appendToStringBuilder(stringToWrite, "@THIS" + "\n");

                } else if (offset == 1) {
                    appendToStringBuilder(stringToWrite, "@THAT" + "\n");
                }

                appendToStringBuilder(stringToWrite, "D=M\n");
            }

            // Push value to stack and SP++
            appendToStringBuilder(stringToWrite, "@SP\n");
            appendToStringBuilder(stringToWrite, "A=M\n");
            appendToStringBuilder(stringToWrite, "M=D\n");
            appendToStringBuilder(stringToWrite, "@SP\n");
            appendToStringBuilder(stringToWrite, "M=M+1\n");
            

        } else if (commandType == Parser.commandType.C_POP) {
            stringToWrite.append("// pop " + segment + " " + offset + "\n");

            if (simpleseg) {
                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
                appendToStringBuilder(stringToWrite, "@" + offset + "\n"); 
                appendToStringBuilder(stringToWrite, "D=D+A\n");
                appendToStringBuilder(stringToWrite, "@R13\n");
                appendToStringBuilder(stringToWrite, "M=D\n");

                appendToStringBuilder(stringToWrite, "@SP\n");
                appendToStringBuilder(stringToWrite, "M=M-1\n");
                appendToStringBuilder(stringToWrite, "A=M\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
                appendToStringBuilder(stringToWrite, "@R13\n");
                appendToStringBuilder(stringToWrite, "A=M\n");
                appendToStringBuilder(stringToWrite, "M=D\n");

            } else if (thisThat) {
                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
                appendToStringBuilder(stringToWrite, "@" + offset + "\n");
                appendToStringBuilder(stringToWrite, "D=D+A\n");
                appendToStringBuilder(stringToWrite, "@R13\n");
                appendToStringBuilder(stringToWrite, "M=D\n");
                appendToStringBuilder(stringToWrite, "@SP\n");
                appendToStringBuilder(stringToWrite, "AM=M-1\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
                appendToStringBuilder(stringToWrite, "@R13\n");
                appendToStringBuilder(stringToWrite, "A=M\n");
                appendToStringBuilder(stringToWrite, "M=D\n");
               
            } else if (temp) {
                appendToStringBuilder(stringToWrite, "@SP\n");
                appendToStringBuilder(stringToWrite, "M=M-1\n");
                appendToStringBuilder(stringToWrite, "A=M\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
                
                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "M=D\n");

            } else if (address.toString().contains("@" + fileName)) {
                appendToStringBuilder(stringToWrite, "@SP\n");
                appendToStringBuilder(stringToWrite, "M=M-1\n");
                appendToStringBuilder(stringToWrite, "A=M\n");
                appendToStringBuilder(stringToWrite, "D=M\n");

                appendToStringBuilder(stringToWrite, address + "\n");
                appendToStringBuilder(stringToWrite, "M=D" + "\n");
                
            } else if (address.toString().contains("@POINTER")) {
                appendToStringBuilder(stringToWrite, "@SP\n");
                appendToStringBuilder(stringToWrite, "M=M-1\n");
                appendToStringBuilder(stringToWrite, "A=M\n");
                appendToStringBuilder(stringToWrite, "D=M\n");
                
                if (offset == 0) {
                    appendToStringBuilder(stringToWrite, "@THIS" + "\n");

                } else if (offset == 1) {
                    appendToStringBuilder(stringToWrite, "@THAT" + "\n");
                }

                appendToStringBuilder(stringToWrite, "M=D\n");
            }

        }
    }
}

/*
 * push static 2
 * @fileName.2
 * D=M
 * @SP
 * A=M go to next stack location
 * M=D
 * @SP
 * M=M+1 SP++
 * 
 * pop static 2
 * 
 * @SP
 * M=M-1
 * A=M
 * D=M // val to pop
 * 
 * @fileName.2
 * M=D
 */





/* pop local 2

@LCL
D=M pointer of lcl
@2
D=D+A base of lcl + 2
@R13
M=D

@SP
M=M-1
A=M
D=M // val to pop

@R13
A=M
M=D

pop temp 3

@SP
M=M-1
A=M
D=M // val to pop

@TEMP3
M=D



@LCL
D=M pointer of lcl
@2
D=D+A base of lcl + 2
A=D @lcl + 2
D=M D contains val at lcl[2]
@SP
A=M go to next stack location
M=D
@SP
M=M+1 SP++
*/

/* push local 2
@LCL
D=M pointer of lcl
@2
D=D+A base of lcl + 2
A=D @lcl + 2
D=M D contains val at lcl[2]
@SP
A=M go to next stack location
M=D
@SP
M=M+1 SP++
*/

/*
 * push pointer 0
 * @THIS
 * D=M
 * @SP
 * 
 *   
 * */

 /*@LCL
D=M pointer of lcl
@2
D=D+A base of lcl + 2
@R13
M=D
@SP
// What if sp 256?
A=M-1
D=M
@R13
A=M
M=D
@SP */

/*
 * 
 * pop this 2  [100] - > RAM[3032] = 100
 * @THIS
 * D=M
 * @2
 * D=D+A == 3032
 * @R13
 * M=D
 * @SP
 * AM=M-1
 * D=M == 100
 * @R13
 * A=M
 * M=D
 * 
 * push this 2 [] -> [ 100 ] // RAM[3032] = 100
 * @THIS
 * D=M
 * @2
 * D=D+A
 * @SP
 * A=M
 * M=D
 * @SP
 * M=M+1
 * 
 */


/*
 * add [ 5 , 6 ] -- > [ 11 ]
 * 
 * @SP
 * M=M-1
 * A=M // pointer to 6
 * D=M = 6
 * A=A-1 // pointer to 5
 * M=M+D
 * 
 * 
 * sub
 * @SP
 * M=M-1
 * A=M // pointer to 6
 * D=M = 6
 * A=A-1 // pointer to 5
 * M=M-D
 * 
 * 
 * neg
 * @SP
 * A=M-1
 * M=-M
 * 
 * eq [ 5, 6 ] -> [ 0 ]
 * 
 * @SP
 * AM=M-1 // pointer to 6
 * D=M = 6
 * A=A-1 // pointer to 5
 * D=D-M
 * M=0
 * @lineCounter + 5
 * D;JEQ
 * @SP
 * A=M-1
 * M=-1
 * 
 * and
 * 
 * [ -1, -1 ] - > [ -1 ]
 * 
 * -1 & 0 = 0
 * -1 & -1 = -1
 * 
 * @SP
 * AM=M-1
 * D=M
 * A=A-1
 * D=D&M = 0
 * M=0
 * @lineCounter + 5
 * D;JEQ
 * @SP
 * A=M-1
 * M=-1
 * 
 * 
 * [ 0 , 0]
 * 
 * @SP
 * AM=M-1
 * D=M == 0
 * A=A-1
 * D=D|M == 0
 * M=-1 == [-1, 0]
 * @lineCounter + 5
 * D;JNE
 * @SP
 * A=M-1
 * M=0 == [ 0 , 0]
 * 
 * [ -1 ]
 * 
 * @SP
 * A=M-1 
 * DM=M+1 == [ 0 ], D == 0
 * @lineCounter + 3
 * D;JEQ
 * M=-1
 */
