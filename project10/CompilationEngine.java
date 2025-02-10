/**
 * CompilationEngine is responsible for generating the compiled output
 * of the source code. It uses a FileWriter to write the compiled code
 * to a specified file.
 *
 * This class is part of the project for the Digital Systems course.
 *
 * Dependencies:
 * - java.io.FileWriter: Used to write the compiled output to a file.
 *
 * Usage:
 * Instantiate this class and use its methods to compile source code
 * and write the output to a file.
 *
 * Example:
 * CompilationEngine engine = new CompilationEngine(outputFilePath);
 * engine.compile(sourceCode);
 *
 * Note:
 * Ensure that the output file path is valid and accessible.
 *
 */
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer jackTokenizer;
    private FileWriter fileWriter;
    private boolean elseStatement = false;

    /**
     * Constructor for CompilationEngine.
     *
     * @param tokenizer the JackTokenizer instance to use for tokenizing the source code
     * @param output the FileWriter instance to use for writing the compiled output
     */
    public CompilationEngine(JackTokenizer tokenizer, FileWriter output) {
        this.jackTokenizer = tokenizer;
        this.fileWriter = output;
    }

    /**
     * Compiles a class.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileClass() throws IOException {
        fileWriter.write("<class>\n");
        boolean end = false;

        String currentWord;
        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();
            currentWord = jackTokenizer.keyWord();

            switch (tokenType) {
                case "KEYWORD":
                    switch (currentWord) {
                        case "class":
                            fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                            break;

                        case "static":
                        case "field":
                            compileClassVarDec();
                            break;

                        case "constructor":
                        case "function":
                        case "method":
                            compileSubroutine();
                            break;

                        default:
                            System.err.println("Error in compileClass KEYWORD");
                    }
                    break;

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                    if (jackTokenizer.symbol() == '}') {
                        end = true;
                    }
                    break;


                default:
                    System.err.println("Error in compileClass");
                    break;
            }

        }
        fileWriter.write("</class>\n");
    }

    /**
     * Compiles a class variable declaration.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileClassVarDec() throws IOException {
        fileWriter.write("<classVarDec>\n");
        fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
        boolean end = false;

        String currentWord;
        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();
            currentWord = jackTokenizer.keyWord();

            switch (tokenType) {
                case "KEYWORD":
                    switch (currentWord) {
                        case "feild":
                        case "static":
                        case "int":
                        case "char":
                        case "boolean":
                            fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                            break;

                        default:
                            System.err.println("Error in compileClassVarDec KEYWORD");
                    }
                    break;

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                    if (jackTokenizer.symbol() == ';') {
                        end = true;
                    }
                    break;

                default:
                    System.err.println("Error in compileClassVarDec");
                    break;
            }

        }
        fileWriter.write("</classVarDec>\n");
    }

    /**
     * Compiles a subroutine.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileSubroutine() throws IOException {
        fileWriter.write("<subroutineDec>\n");
        fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
        boolean end = false;

        String currentWord;
        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();
            currentWord = jackTokenizer.keyWord();

            switch (tokenType) {
                case "KEYWORD":
                    switch (currentWord) {
                        case "constructor":
                        case "function":
                        case "method":
                        case "int":
                        case "char":
                        case "boolean":
                        case "void":
                            // className is identifier
                            fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                            break;

                        default:
                            System.err.println("Error in compileSubroutine KEYWORD");
                    }
                    break;

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    if (jackTokenizer.symbol() == '(') {
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                        compileParameterList();
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ')'

                    } else if (jackTokenizer.symbol() == '{') {
                        compileSubroutineBody();
                        end = true;

                    } else {
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                    }
                    break;

                default:
                    System.err.println("Error in compileSubroutine");
                    break;
            }

        }

        fileWriter.write("</subroutineDec>\n");
    }

    /**
     * Compiles a parameter list.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileParameterList() throws IOException {
        fileWriter.write("<parameterList>\n");
        boolean end = false;

        String currentWord;
        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();
            currentWord = jackTokenizer.keyWord();

            switch (tokenType) {
                case "KEYWORD":
                    switch (currentWord) {
                        case "int":
                        case "char":
                        case "boolean":
                            // className is identifier
                            fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                            break;

                        default:
                            System.err.println("Error in compileParameterList KEYWORD");
                    }
                    break;

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    if (jackTokenizer.symbol() == ')') {
                        end = true;
                    } else {
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ','
                    }
                    break;

                default:
                    System.err.println("Error in compileParameterList");
                    break;
            }
        }

        fileWriter.write("</parameterList>\n");
    }

    /**
     * Compiles a subroutine body.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileSubroutineBody() throws IOException {
        fileWriter.write("<subroutineBody>\n");
        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be '{'
        boolean end = false;

        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();
            String tokenType = jackTokenizer.tokenType().toString();

            switch (tokenType) {
                case "KEYWORD":
                    if (jackTokenizer.keyWord().equals("var")) {
                        compileVarDec();
                    } else {
                        end = true;
                    }
                    break;

                default:
                    System.err.println("Error in compileSubroutineBody");
                    break;
            }
        }
        compileStatements();
        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be '}'

        fileWriter.write("</subroutineBody>\n");
    }

    /**
     * Compiles a variable declaration.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileVarDec() throws IOException {
        fileWriter.write("<varDec>\n");
        fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n"); // This will be 'var'
        boolean end = false;

        String currentWord;
        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();
            currentWord = jackTokenizer.keyWord();

            switch (tokenType) {
                case "KEYWORD":
                    switch (currentWord) {
                        case "int":
                        case "char":
                        case "boolean":
                            // className is identifier
                            fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                            break;

                        default:
                            System.err.println("Error in compileVarDec KEYWORD");
                    }
                    break;

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                    if (jackTokenizer.symbol() == ';') {
                        end = true;
                    }
                    break;

                default:
                    System.err.println("Error in compileVarDec");
                    break;
            }
        }

        fileWriter.write("</varDec>\n");
    }

    /**
     * Compiles a sequence of statements.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileStatements() throws IOException {
        fileWriter.write("<statements>\n");
        boolean end = false;

        String currentWord;
        while (jackTokenizer.hasMoreTokens() && !end) {

            String tokenType = jackTokenizer.tokenType().toString();

            if (tokenType.equals("KEYWORD")) {
                currentWord = jackTokenizer.keyWord();
                if (!"let if while do return".contains(currentWord)) {
                    jackTokenizer.advance();
                }
            } else {
                jackTokenizer.advance();
            }

            tokenType = jackTokenizer.tokenType().toString();
            currentWord = jackTokenizer.keyWord();

            if (!currentWord.equals("else") && elseStatement) {
                fileWriter.write("</ifStatement>\n");
                elseStatement = false;
            }

            switch (tokenType) {
                case "KEYWORD":
                    switch (currentWord) {
                        case "let":
                            compileLet();
                            break;

                        case "if":
                            compileIf();
                            break;
                        case "else":
                            elseStatement = false;
                            fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                            jackTokenizer.advance();
                            fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be '{'
                            compileStatements();
                            fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be '}'
                            fileWriter.write("</ifStatement>\n");
                            break;

                        case "while":
                            compileWhile();
                            break;

                        case "do":
                            compileDo();
                            break;

                        case "return":
                            compileReturn();
                            break;

                        default:
                            System.err.println("Error in compileStatements KEYWORD");
                    }
                    break;

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    if (jackTokenizer.symbol() == '}') {
                        end = true;
                    }
                    break;

                default:
                    System.err.println("Error in compileStatements");
                    break;
            }
        }

        fileWriter.write("</statements>\n");
    }

    /**
     * Compiles a let statement.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileLet() throws IOException {
        fileWriter.write("<letStatement>\n");
        fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
        boolean end = false;

        String currentWord;
        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();
            currentWord = jackTokenizer.keyWord();

            switch (tokenType) {
                case "KEYWORD":
                    fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                    switch (currentWord) {
                        case "int":
                        case "char":
                        case "boolean":
                            // className is identifier
                            fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                            break;

                        default:
                            System.err.println("Error in compileLet KEYWORD");
                    }
                    break;

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                    if (jackTokenizer.symbol() == '[') {
                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }
                        compileExpression();
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ']'

                    } else if (jackTokenizer.symbol() == '=') {

                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }
                        compileExpression();
                        if (jackTokenizer.symbol() == ';') {
                            fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                            end = true;
                        }
                    }

                    if (jackTokenizer.symbol() == ';') {
                        end = true;
                    }
                    break;

                default:
                    System.err.println("Error in compileLet");
                    break;
            }
        }

        fileWriter.write("</letStatement>\n");
    }

    /**
     * Compiles an if statement.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileIf() throws IOException {
        fileWriter.write("<ifStatement>\n");
        fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n"); // This will be 'if'
        boolean end = false;
        String symbol;

        String currentWord;
        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();
            currentWord = jackTokenizer.keyWord();

            switch (tokenType) {
                case "KEYWORD":
                    fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                    switch (currentWord) {
                        case "else":
                            fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");
                            break;

                        default:
                            System.err.println("Error in compileIf KEYWORD");
                    }
                    break;

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                    if (jackTokenizer.symbol() == '(') {

                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }
                        compileExpression();
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ')'

                    } else if (jackTokenizer.symbol() == '{') {
                        compileStatements();
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be '}'
                        end = true;

                    } else if ("+-*/&|<>=~".contains(jackTokenizer.symbol() + "")) {
                        symbol = jackTokenizer.symbol() + "";
                        switch (jackTokenizer.symbol()) {
                            case '<':
                                symbol = "&lt;";
                                break;
                            case '>':
                                symbol = "&gt;";
                                break;
                            case '\"':
                                symbol = "&quot;";
                                break;
                            case '&':
                                symbol = "&amp;";
                                break;

                            default:
                                symbol = "" + jackTokenizer.symbol();
                        }
                        fileWriter.write("<symbol> " + symbol + " </symbol>\n");

                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }
                        compileExpression();

                        // } else if (jackTokenizer.symbol() == '}') {
                        //     end = true;
                        // }
                    }
                    break;

                default:
                    System.err.println("Error in compileIf");
                    break;
            }
        }
        elseStatement = true;
        // fileWriter.write("</ifStatement>\n");
    }

    /**
     * Compiles a while statement.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileWhile() throws IOException {
        fileWriter.write("<whileStatement>\n");
        fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n"); // This will be 'while'
        boolean end = false;

        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();

            switch (tokenType) {

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;

                case "SYMBOL":
                    fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                    if (jackTokenizer.symbol() == '(') {

                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }
                        compileExpression();
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ')'

                    } else if (jackTokenizer.symbol() == '{') {
                        compileStatements();
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be '}'
                        end = true;
                    }
                    break;

                default:
                    System.err.println("Error in compileWhile");
                    break;
            }
        }
        fileWriter.write("</whileStatement>\n");
    }

    /**
     * Compiles a do statement.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileDo() throws IOException {
        fileWriter.write("<doStatement>\n");
        fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n"); // This will be 'do'
        boolean end = false;

        while (jackTokenizer.hasMoreTokens() && !end) {

            jackTokenizer.advance();

            String tokenType = jackTokenizer.tokenType().toString();
            
            switch (tokenType) {

                case "IDENTIFIER":
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    break;
            
                case "SYMBOL":
                    fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                    if (jackTokenizer.symbol() == '(') {
                        
                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();            
                        }         
                        compileExpressionList();
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ')'

                    } else if (jackTokenizer.symbol() == ';') {
                        end = true;
                    }
                    break;
            
                default:
                    System.err.println("Error in compileDo");
                    break;
            }
        }
        fileWriter.write("</doStatement>\n");
    }

    /**
     * Compiles a return statement.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileReturn() throws IOException {
        fileWriter.write("<returnStatement>\n");
        fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n"); // This will be 'return'
        boolean end = false;

        while (jackTokenizer.hasMoreTokens() && !end) {
            jackTokenizer.advance();

            if (jackTokenizer.symbol() == ';') {
                fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");
                end = true;
            } else {
                compileExpression();
                fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ';'
                end = true;
            }
        }

        fileWriter.write("</returnStatement>\n");
    }

    /**
     * Compiles an expression.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileExpression() throws IOException {
        fileWriter.write("<expression>\n");
        boolean end = false;
        String symbol;

        compileTerm();

        while (jackTokenizer.hasMoreTokens() && !end) {
            if ("+-*/&|<>=~".contains(jackTokenizer.symbol() + "")) {
                symbol = jackTokenizer.symbol() + "";
                switch (jackTokenizer.symbol()) {
                    case '<':
                        symbol = "&lt;";
                        break;
                    case '>':
                        symbol = "&gt;";
                        break;
                    case '\"':
                        symbol = "&quot;";
                        break;
                    case '&':
                        symbol = "&amp;";
                        break;
                    default:
                        symbol = "" + jackTokenizer.symbol();
                }

                fileWriter.write("<symbol> " + symbol + " </symbol>\n");

                if (jackTokenizer.hasMoreTokens()) {
                    jackTokenizer.advance();
                }
                compileTerm();
            } else {
                end = true;
            }
        }

        fileWriter.write("</expression>\n");
    }

    /**
     * Compiles a term.
     *
     * @throws IOException if an I/O error occurs
     */
    public void compileTerm() throws IOException {
        fileWriter.write("<term>\n");
        boolean end = false;

        while (jackTokenizer.hasMoreTokens() && !end) {
            String tokenType = jackTokenizer.tokenType().toString();

            switch (tokenType) {
                case "KEYWORD":
                    fileWriter.write("<keyword> " + jackTokenizer.keyWord() + " </keyword>\n");

                    if (jackTokenizer.hasMoreTokens()) {
                        jackTokenizer.advance();
                    }
                    end = true;
                    break;

                case "INT_CONST":
                    fileWriter.write("<integerConstant> " + jackTokenizer.intVal() + " </integerConstant>\n");

                    if (jackTokenizer.hasMoreTokens()) {
                        jackTokenizer.advance();
                    }
                    end = true;
                    break;

                case "STRING_CONST":
                    String str = jackTokenizer.stringVal();
                    fileWriter.write("<stringConstant> " + str + " </stringConstant>\n");

                    if (jackTokenizer.hasMoreTokens()) {
                        jackTokenizer.advance();
                    }
                    end = true;
                    break;

                case "IDENTIFIER":
                    end = true;
                    fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    if (jackTokenizer.hasMoreTokens()) {
                        jackTokenizer.advance();
                        if (jackTokenizer.symbol() == '[') {
                            fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");

                            if (jackTokenizer.hasMoreTokens()) {
                                jackTokenizer.advance();
                            }
                            compileExpression();
                            fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ']'

                        } else if (jackTokenizer.symbol() == '(') {
                            fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");

                            if (jackTokenizer.hasMoreTokens()) {
                                jackTokenizer.advance();
                            }
                            compileExpressionList();
                            fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ')'

                        } else if (jackTokenizer.symbol() == '.') {
                            fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");

                            if (jackTokenizer.hasMoreTokens()) {
                                jackTokenizer.advance();
                            }
                            end = false;
                        }
                    }
                    break;

                case "SYMBOL":
                    if (jackTokenizer.symbol() == '(') {
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");

                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }
                        compileExpression();
                        // This is the end of a term of kind '(' expression ')'
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n"); // This will be ')'

                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }

                    } else if (jackTokenizer.symbol() == '-' || jackTokenizer.symbol() == '~') {
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");

                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }
                        compileTerm();

                    } else {
                        fileWriter.write("<identifier> " + jackTokenizer.identifier() + " </identifier>\n");
                    }

                    end = true;
                    break;

                default:
                    System.err.println("Error in compileTerm");
                    break;
            }
        }

        fileWriter.write("</term>\n");
    }

    /**
     * Compiles a list of expressions.
     *
     * @return the number of expressions in the list
     * @throws IOException if an I/O error occurs
     */
    public int compileExpressionList() throws IOException {
        int counter = 0;
        fileWriter.write("<expressionList>\n");
        boolean end = false;

        if (jackTokenizer.tokenType().toString() == "SYMBOL" && jackTokenizer.symbol() == ')') {
            fileWriter.write("</expressionList>\n");
            return counter;
        } else {
            counter++;
            compileExpression();
        }
        while (jackTokenizer.hasMoreTokens() && !end) {
            String tokenType = jackTokenizer.tokenType().toString();

            switch (tokenType) {
                case "SYMBOL":
                    if (jackTokenizer.symbol() == ')') {
                        end = true;

                    } else if (jackTokenizer.symbol() == ',') {
                        counter++;
                        fileWriter.write("<symbol> " + jackTokenizer.symbol() + " </symbol>\n");

                        if (jackTokenizer.hasMoreTokens()) {
                            jackTokenizer.advance();
                        }
                        compileExpression();
                    }
                    break;

                default:
                    compileExpression();
                    break;
            }
        }

        fileWriter.write("</expressionList>\n");
        return counter;
    }
}