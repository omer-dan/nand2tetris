import java.io.File;

/**
 * The CompilationEngine class is responsible for compiling a given input file
 * into a VM code output file. It uses a JackTokenizer to tokenize the input,
 * a SymbolTable to manage variable scopes, and a VMWriter to generate the VM code.
 */
public class CompilationEngine {
    private int labelCounter = 0;

    private VMWriter vmWriter;
    private JackTokenizer jackTokenizer;
    private SymbolTable symbolTable;
    private String className;
    private String subroutineName;

    /**
     * Creates a new compilation engine with the given input and output files.
     * The next routine called (by the JackAnalyzer module) must be compileClass.
     *
     * @param in  the input file to be compiled
     * @param out the output file to write the compiled VM code
     */
    public CompilationEngine(File in, File out) {
        jackTokenizer = new JackTokenizer(in);
        symbolTable = new SymbolTable();
        vmWriter = new VMWriter(out);
    }

    
    private String currentFunctionName() {
        if (className.length() != 0 && subroutineName.length() !=0) {
            return className + "." + subroutineName;
        }
        return "";
    }

    /**
     * Compiles a complete class.
     * The structure of a class is: 'class' className '{' classVarDec* subroutineDec* '}'
     * 
     * @throws IllegalStateException if the class structure is invalid, including:
     *         - Missing or incorrect 'class' keyword
     *         - Missing or invalid class name
     *         - Missing opening or closing braces
     *         - Extra tokens after class closing brace
     */
    public void compileClass() {
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD
                || jackTokenizer.keyWord() != JackTokenizer.KEYWORD.CLASS)
        {
            System.out.println(jackTokenizer.getCurrentToken());
            throwError("class instead of " + jackTokenizer.getCurrentToken());
        }

        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            throwError("className instead of " + jackTokenizer.getCurrentToken());
        }

        className = jackTokenizer.identifier();
        checkForSymbol('{');
        compileClassVarDec();
        compileSubroutine();
        checkForSymbol('}');
        if (jackTokenizer.hasMoreTokens()){
            throw new IllegalStateException("Should be end of file. next token: " + jackTokenizer.getCurrentToken());
        }
        vmWriter.close();
    }

    /**
     * Compiles a class variable declaration (`static` or `field`).
     *
     * - Parses `static`/`field` declarations, their types, and names.
     * - Updates the symbol table with the variables.
     * - Processes multiple declarations recursively.
     *
     * Example:
     * Input: `static int x, y; field boolean isActive;`
     * Output: Symbol table updated with `x`, `y` (static) and `isActive` (field).
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileClassVarDec(){
        jackTokenizer.advance();

        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                && jackTokenizer.symbol() == '}')
        {
            jackTokenizer.movePointerBack();
            return;
        }
        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD){
            throwError("Keywords instead of " + jackTokenizer.getCurrentToken());
        }
        if (jackTokenizer.keyWord() == JackTokenizer.KEYWORD.CONSTRUCTOR
                || jackTokenizer.keyWord() == JackTokenizer.KEYWORD.FUNCTION
                || jackTokenizer.keyWord() == JackTokenizer.KEYWORD.METHOD)
        {
            jackTokenizer.movePointerBack();
            return;
        }
        if (jackTokenizer.keyWord() != JackTokenizer.KEYWORD.STATIC
                && jackTokenizer.keyWord() != JackTokenizer.KEYWORD.FIELD)
        {
            throwError("static or field instead of " + jackTokenizer.getCurrentToken());
        }
        Symbol.KIND kind = null;
        String type = "";
        String name = "";
        switch (jackTokenizer.keyWord()){
            case STATIC:kind = Symbol.KIND.STATIC;break;
            case FIELD:kind = Symbol.KIND.FIELD;break;
        }

        type = compileType();
        do {
            jackTokenizer.advance();
            if (jackTokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER)
            {
                throwError("identifier instead of " + jackTokenizer.getCurrentToken());
            }
            name = jackTokenizer.identifier();
            symbolTable.define(name,type,kind);
            jackTokenizer.advance();
            if (jackTokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL
                    || (jackTokenizer.symbol() != ',' && jackTokenizer.symbol() != ';'))
            {
                throwError("',' or ';' instead of " + jackTokenizer.getCurrentToken());
            }
            if (jackTokenizer.symbol() == ';') {
                break;
            }
        }
        while(true);
        compileClassVarDec();
    }
    
    /**
     * Compiles a subroutine declaration (`constructor`, `function`, or `method`).
     *
     * - Parses the subroutine type, return type, name, and parameter list.
     * - Initializes a new subroutine symbol table and handles `this` for methods.
     * - Compiles the subroutine body, including variable declarations and statements.
     *
     * Example:
     * Input: `function void myFunction(int x) { var int y; let y = x; }`
     * Output: Symbol table updated, VM code generated for the function.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileSubroutine() {
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                && jackTokenizer.symbol() == '}')
        {
            jackTokenizer.movePointerBack();
            return;
        }

        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD
                || (jackTokenizer.keyWord() != JackTokenizer.KEYWORD.CONSTRUCTOR
                && jackTokenizer.keyWord() != JackTokenizer.KEYWORD.FUNCTION
                && jackTokenizer.keyWord() != JackTokenizer.KEYWORD.METHOD))
        {
            throwError("constructor | function | method instead of " + jackTokenizer.getCurrentToken());
        }

        JackTokenizer.KEYWORD keyword = jackTokenizer.keyWord();
        symbolTable.startSubroutine();

        if (jackTokenizer.keyWord() == JackTokenizer.KEYWORD.METHOD)
        {
            symbolTable.define("this",className, Symbol.KIND.ARG);
        }

        String type = "";
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD
                && jackTokenizer.keyWord() == JackTokenizer.KEYWORD.VOID)
        {
            type = "void";
        }
        else {
            jackTokenizer.movePointerBack();
            type = compileType();
        }

        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER)
        {
            throwError("subroutineName  instead of " + jackTokenizer.getCurrentToken());
        }

        subroutineName = jackTokenizer.identifier();
        checkForSymbol('(');
        compileParameterList();
        checkForSymbol(')');
        compileSubroutineBody(keyword);
        compileSubroutine();
    }

    /**
     * Compiles the body of a subroutine.
     *
     * - Parses variable declarations and statements within `{}`.
     * - Writes the function declaration in VM code.
     * - Handles initialization for `METHOD` and `CONSTRUCTOR` types.
     *
     * Example:
     * Input: `{ var int x; let x = 5; return; }`
     * Output: Symbol table updated, VM code written for the body.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileSubroutineBody(JackTokenizer.KEYWORD keyword)
    {
        checkForSymbol('{');
        compileVarDec();
        wrtieFunctionDec(keyword);
        compileStatement();
        checkForSymbol('}');
    }

    /**
     * Writes the function declaration and handles initialization for methods or constructors.
     *
     * - Outputs the function VM code with the correct local variable count.
     * - Initializes the `this` pointer for methods or allocates memory for constructors.
     *
     * Example:
     * Input: `METHOD` -> Pushes argument 0 as `this`.
     *        `CONSTRUCTOR` -> Allocates memory for fields.
     *
     * Throws:
     * - Exception for invalid subroutine type.
     */
    private void wrtieFunctionDec(JackTokenizer.KEYWORD keyword)
    {
        vmWriter.writeFunction(currentFunctionName(), symbolTable.varCount(Symbol.KIND.VAR));
        if (keyword == JackTokenizer.KEYWORD.METHOD){
            vmWriter.writePush(VMWriter.SEGMENT.ARG, 0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,0);
        }
        else if (keyword == JackTokenizer.KEYWORD.CONSTRUCTOR){
            vmWriter.writePush(VMWriter.SEGMENT.CONST, symbolTable.varCount(Symbol.KIND.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,0);
        }
    }

    /**
     * Compiles a single statement (`let`, `if`, `while`, `do`, or `return`).
     *
     * - Identifies the statement type and calls the respective compile method.
     * - Recursively processes all statements in a block.
     *
     * Example:
     * Input: `let x = 5; if (x > 0) { do something(); } return;`
     * Output: VM code generated for each statement.
     *
     * Throws:
     * - Exception for unrecognized statement types.
     */
    private void compileStatement(){
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                && jackTokenizer.symbol() == '}')
        {
            jackTokenizer.movePointerBack();
            return;
        }

        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD)
        {
            throwError("keyword instead of " + jackTokenizer.getCurrentToken());
        }
        else {
            switch (jackTokenizer.keyWord())
            {
                case LET:
                    compileLet();break;
                case IF:
                    compileIf();break;
                case WHILE:
                    compilesWhile();break;
                case DO:
                    compileDo();break;
                case RETURN:
                    compileReturn();break;
                default:
                    throwError("should be let | if | while | do | return instead of " + jackTokenizer.getCurrentToken());
            }
        }
        compileStatement();
    }

    /**
     * Compiles a parameter list for a subroutine.
     *
     * - Parses parameter types and names, adding them to the symbol table as arguments.
     * - Handles an empty parameter list or multiple parameters separated by commas.
     *
     * Example:
     * Input: `(int x, boolean flag)`
     * Output: `x` and `flag` added to the symbol table as arguments.
     *
     * Throws:
     * - Exception for invalid syntax or missing identifiers.
     */
    private void compileParameterList()
    {
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                && jackTokenizer.symbol() == ')')
        {
            jackTokenizer.movePointerBack();
            return;
        }

        String type = "";
        jackTokenizer.movePointerBack();
        do {
            type = compileType();
            jackTokenizer.advance();
            if (jackTokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER)
            {
                throwError("identifier instead of " + jackTokenizer.getCurrentToken());
            }
            symbolTable.define(jackTokenizer.identifier(),type, Symbol.KIND.ARG);
            jackTokenizer.advance();

            if (jackTokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL
                    || (jackTokenizer.symbol() != ',' && jackTokenizer.symbol() != ')'))
            {
                throwError("',' or ')' instead of " + jackTokenizer.getCurrentToken());
            }

            if (jackTokenizer.symbol() == ')'){
                jackTokenizer.movePointerBack();
                break;
            }
        }
        while(true);
    }

    /**
     * Compiles a variable declaration within a subroutine.
     *
     * - Parses `var` declarations, their types, and variable names.
     * - Updates the symbol table with local variables.
     * - Processes multiple declarations separated by commas.
     *
     * Example:
     * Input: `var int x, y;`
     * Output: `x` and `y` added to the symbol table as local variables.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileVarDec(){
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD
                || jackTokenizer.keyWord() != JackTokenizer.KEYWORD.VAR)
        {
            jackTokenizer.movePointerBack();
            return;
        }
        String type = compileType();
        do {
            jackTokenizer.advance();
            if (jackTokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                throwError("identifier instead of " + jackTokenizer.getCurrentToken());
            }
            symbolTable.define(jackTokenizer.identifier(),type, Symbol.KIND.VAR);
            jackTokenizer.advance();

            if (jackTokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL
                    || (jackTokenizer.symbol() != ',' && jackTokenizer.symbol() != ';'))
            {
                throwError("',' or ';' instead of " + jackTokenizer.getCurrentToken());
            }

            if (jackTokenizer.symbol() == ';'){
                break;
            }
        }
        while(true);
        compileVarDec();
    }

    /**
     * Compiles a `do` statement.
     *
     * - Parses and compiles a subroutine call.
     * - Discards the return value by popping it into `temp 0`.
     *
     * Example:
     * Input: `do myFunction(5);`
     * Output: VM code for calling `myFunction`.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileDo() {
        compileSubroutineCall();
        checkForSymbol(';');
        vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
    }

     /**
     * Compiles a subroutine call.
     *
     * - Handles calls in both `object.method()` and `function()` formats.
     * - Resolves the object type and pushes `this` if required.
     * - Generates VM code for the call and tracks argument counts.
     *
     * Example:
     * Input: `do obj.method(x, y);`
     * Output: VM code for calling `obj.method`.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileSubroutineCall() {
        jackTokenizer.advance();

        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER) {
            throwError("Should be identifier, instead of " + jackTokenizer.tokenType());
        }

        String name = jackTokenizer.identifier();
        int nargs = 0;

        jackTokenizer.advance();

        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                && jackTokenizer.symbol() == '(')
        {
            vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);
            nargs = compileExpressionList() + 1;
            checkForSymbol(')');
            vmWriter.writeCall(className + '.' + name, nargs);
        }
        else if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                && jackTokenizer.symbol() == '.')
        {
            String objName = name;
            jackTokenizer.advance();
            if (jackTokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER) {
                throwError("Should be identifier, instead of " + jackTokenizer.tokenType());
            }

            name = jackTokenizer.identifier();
            String type = symbolTable.typeOf(objName);

            if (type.equals("int")
                    || type.equals("boolean")
                    || type.equals("char")
                    || type.equals("void"))
            {
                throwError("no built-in type, received " + type);
            }

            else if (type.equals(""))
            {
                name = objName + "." + name;
            }
            else {
                nargs = 1;
                vmWriter.writePush(getSeg(symbolTable.kindOf(objName)), symbolTable.indexOf(objName));
                name = symbolTable.typeOf(objName) + "." + name;
            }

            checkForSymbol('(');
            nargs += compileExpressionList();
            checkForSymbol(')');
            vmWriter.writeCall(name,nargs);
        }
        else {
            throwError("'('|'.' instead of " + jackTokenizer.getCurrentToken());
        }
    }

    /**
     * Compiles a `let` statement.
     *
     * - Parses variable assignment, including optional array indexing.
     * - Generates VM code to compute the target address and assign the value.
     *
     * Example:
     * Input: `let a[i] = 8;`
     * Output: VM code for computing `a[i]` and assigning `8`.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileLet(){
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER)
        {
            throwError("varName instead of " + jackTokenizer.getCurrentToken());
        }

        String varName = jackTokenizer.identifier();
        jackTokenizer.advance();

        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL
                || (jackTokenizer.symbol() != '[' && jackTokenizer.symbol() != '='))
        {
            throwError("'['|'=' instead of " + jackTokenizer.getCurrentToken());
        }

        boolean expresExist = false;
        if (jackTokenizer.symbol() == '[')
        {
            expresExist = true;
            vmWriter.writePush(getSeg(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
            compileExpression();
            checkForSymbol(']');
            vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
        }

        if (expresExist) {
            jackTokenizer.advance();
        }

        compileExpression();
        checkForSymbol(';');

        if (expresExist)
        {
            vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
            vmWriter.writePush(VMWriter.SEGMENT.TEMP,0);
            vmWriter.writePop(VMWriter.SEGMENT.THAT,0);
        }
        else {
            vmWriter.writePop(getSeg(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
        }
    }
    
    /**
     * Maps a symbol kind to its corresponding VM segment.
     *
     * - Converts `FIELD`, `STATIC`, `VAR`, and `ARG` to `this`, `static`, `local`, and `argument`.
     *
     * Example:
     * Input: `Symbol.KIND.FIELD`
     * Output: `VMWriter.SEGMENT.THIS`
     *
     * Returns:
     * - `VMWriter.SEGMENT.NONE` for unrecognized kinds.
     */
    private VMWriter.SEGMENT getSeg(Symbol.KIND kind){
        return switch (kind) {
            case FIELD -> VMWriter.SEGMENT.THIS;
            case STATIC -> VMWriter.SEGMENT.STATIC;
            case VAR -> VMWriter.SEGMENT.LOCAL;
            case ARG -> VMWriter.SEGMENT.ARG;
            default -> VMWriter.SEGMENT.NONE;
        };
    }

    /**
     * Compiles a `while` statement.
     *
     * - Generates unique labels for the loop condition and end.
     * - Writes VM code for evaluating the condition and looping if true.
     *
     * Example:
     * Input: `while (x > 0) { let x = x - 1; }`
     * Output: VM code for the loop condition and body.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compilesWhile(){
        String firstLabel = newLabel();
        String secondLabel = newLabel();
        vmWriter.writeLabel(secondLabel);

        checkForSymbol('(');
        compileExpression();
        checkForSymbol(')');

        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(firstLabel);

        checkForSymbol('{');
        compileStatement();
        checkForSymbol('}');

        vmWriter.writeGoto(secondLabel);
        vmWriter.writeLabel(firstLabel);
    }

    /**
     * Compiles a `return` statement.
     *
     * - Handles both `return;` and `return expression;`.
     * - Pushes `0` for void returns or evaluates and returns an expression.
     *
     * Example:
     * Input: `return x + 1;`
     * Output: VM code to compute `x + 1` and return it.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileReturn(){
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                && jackTokenizer.symbol() == ';')
        {
            vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
        }
        else {
            jackTokenizer.movePointerBack();
            compileExpression();
            checkForSymbol(';');
        }

        vmWriter.writeReturn();
    }

    /**
     * Compiles an `if` statement, optionally with an `else` clause.
     *
     * - Generates unique labels for the `else` and `end` branches.
     * - Writes VM code to evaluate the condition and execute the appropriate block.
     *
     * Example:
     * Input: `if (x > 0) { let x = x - 1; } else { let x = 0; }`
     * Output: VM code for the condition, `if` body, and `else` body.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileIf(){
        String elseLabel = newLabel();
        String endLabel = newLabel();

        checkForSymbol('(');
        compileExpression();
        checkForSymbol(')');

        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(elseLabel);

        checkForSymbol('{');
        compileStatement();
        checkForSymbol('}');

        vmWriter.writeGoto(endLabel);
        vmWriter.writeLabel(elseLabel);

        jackTokenizer.advance();

        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD
                && jackTokenizer.keyWord() == JackTokenizer.KEYWORD.ELSE)
        {
            checkForSymbol('{');
            compileStatement();
            checkForSymbol('}');
        }
        else {
            jackTokenizer.movePointerBack();
        }

        vmWriter.writeLabel(endLabel);
    }

    /**
     * Compiles a single term in an expression.
     *
     * - Handles constants, variables, array access, subroutine calls, expressions in parentheses, and unary operators.
     * - Generates appropriate VM code for each term type.
     *
     * Example:
     * Input: `5`, `x[i]`, `-y`, `(x + 1)`
     * Output: VM code to evaluate the term.
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private void compileTerm() {
        jackTokenizer.advance();

        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.IDENTIFIER) {
            String tempId = jackTokenizer.identifier();
            jackTokenizer.advance();

            if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                    && jackTokenizer.symbol() == '[')
            {
                vmWriter.writePush(getSeg(symbolTable.kindOf(tempId)), symbolTable.indexOf(tempId));

                compileExpression();
                checkForSymbol(']');

                vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
                vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
                vmWriter.writePush(VMWriter.SEGMENT.THAT,0);
            }
            else if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                    && (jackTokenizer.symbol() == '(' || jackTokenizer.symbol() == '.'))
            {
                jackTokenizer.movePointerBack();
                jackTokenizer.movePointerBack();
                compileSubroutineCall();
            }
            else {
                jackTokenizer.movePointerBack();
                vmWriter.writePush(getSeg(symbolTable.kindOf(tempId)), symbolTable.indexOf(tempId));
            }
        }
        else {
            if (jackTokenizer.tokenType() == JackTokenizer.TYPE.INT_CONST) {
                vmWriter.writePush(VMWriter.SEGMENT.CONST, jackTokenizer.intVal());
            }
            else if (jackTokenizer.tokenType() == JackTokenizer.TYPE.STRING_CONST) {
                String str = jackTokenizer.stringVal();

                vmWriter.writePush(VMWriter.SEGMENT.CONST,str.length());
                vmWriter.writeCall("String.new",1);

                for (int i = 0; i < str.length(); i++) {
                    vmWriter.writePush(VMWriter.SEGMENT.CONST,(int)str.charAt(i));
                    vmWriter.writeCall("String.appendChar",2);
                }
            }
            else if (jackTokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD
                    && jackTokenizer.keyWord() == JackTokenizer.KEYWORD.TRUE)
            {
                vmWriter.writePush(VMWriter.SEGMENT.CONST,1);
                vmWriter.writeArithmetic(VMWriter.COMMAND.NEG);
            }
            else if (jackTokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD
                    && jackTokenizer.keyWord() == JackTokenizer.KEYWORD.THIS)
            {
                vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);
            }
            else if (jackTokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD
                    && (jackTokenizer.keyWord() == JackTokenizer.KEYWORD.FALSE
                        || jackTokenizer.keyWord() == JackTokenizer.KEYWORD.NULL))
            {
                vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
            }
            else if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                    && jackTokenizer.symbol() == '(')
            {
                compileExpression();
                checkForSymbol(')');
            }
            else if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                    && (jackTokenizer.symbol() == '-' || jackTokenizer.symbol() == '~'))
            {
                char symbol = jackTokenizer.symbol();

                compileTerm();

                if (symbol == '-') {
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NEG);
                }
                else {
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
                }
            }
            else {
                throwError("integerConstant | stringConstant | keywordConstant | '(' expression ')' | unaryOp term instead of " + jackTokenizer.getCurrentToken());
            }
        }
    }

    /**
     * Compiles an expression.
     *
     * - Parses terms and binary operators (`+`, `-`, `*`, `/`, etc.).
     * - Generates VM code for evaluating the expression.
     *
     * Example:
     * Input: `x + (y * 2)`
     * Output: VM code for computing `x + (y * 2)`.
     *
     * Throws:
     * - Exception for invalid syntax or unknown operators.
     */
    private void compileExpression(){
        compileTerm();
        do {
            jackTokenizer.advance();
            if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && jackTokenizer.isOp()) {
                String operand = "";
                switch (jackTokenizer.symbol()){
                    case '+':
                        operand = "add";
                        break;
                    case '-':
                        operand = "sub";
                        break;
                    case '*':
                        operand = "call Math.multiply 2";
                        break;
                    case '/':
                        operand = "call Math.divide 2";
                        break;
                    case '<':
                        operand = "lt";
                        break;
                    case '>':
                        operand = "gt";
                        break;
                    case '=':
                        operand = "eq";
                        break;
                    case '&':
                        operand = "and";
                        break;
                    case '|':
                        operand = "or";
                        break;
                    default:
                        throwError("Unknown op! received " + jackTokenizer.symbol());
                }
                compileTerm();
                vmWriter.writeCommand(operand,"","");
            }
            else {
                jackTokenizer.movePointerBack();
                break;
            }
        }
        while (true);
    }

    /**
     * Compiles a comma-separated list of expressions.
     *
     * - Parses and compiles each expression in the list.
     * - Handles an empty list or multiple expressions separated by commas.
     *
     * Example:
     * Input: `(x, y + 1, 5)`
     * Output: VM code for evaluating `x`, `y + 1`, and `5`.
     *
     * Returns:
     * - The number of expressions compiled (used for subroutine calls).
     *
     * Throws:
     * - Exception for invalid syntax.
     */
    private int compileExpressionList(){
        int nargs = 0;
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                && jackTokenizer.symbol() == ')')
        {
            jackTokenizer.movePointerBack();
        }
        else {
            nargs = 1;
            jackTokenizer.movePointerBack();
            compileExpression();
            do {
                jackTokenizer.advance();
                if (jackTokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL
                        && jackTokenizer.symbol() == ',')
                {
                    compileExpression();
                    nargs++;
                }
                else {
                    jackTokenizer.movePointerBack();
                    break;
                }
            }
            while (true);
        }
        return nargs;
    }

    /**
     * Throws an exception to report syntax errors.
     *
     * - Includes the expected and actual tokens in the error message.
     *
     * Example:
     * Input: `throwError("expected ; instead of ,")`
     * Output: Throws an `IllegalStateException` with the provided message.
     */
    private void throwError(String val) {
        throw new IllegalStateException("Expected token missing : " + val + " Current token: " + jackTokenizer.getCurrentToken());
    }

    /**
     * Compiles a type (`int`, `char`, `boolean`, or a class name).
     *
     * - Parses and returns the type of a variable or parameter.
     *
     * Example:
     * Input: `int`, `boolean`, `MyClass`
     * Output: `"int"`, `"boolean"`, `"MyClass"`
     *
     * Throws:
     * - Exception for invalid or missing type.
     */
    private String compileType() {
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD &&
            (jackTokenizer.keyWord() == JackTokenizer.KEYWORD.INT
                    || jackTokenizer.keyWord() == JackTokenizer.KEYWORD.CHAR
                    || jackTokenizer.keyWord() == JackTokenizer.KEYWORD.BOOLEAN)
            )
        {
            return jackTokenizer.getCurrentToken();
        }

        if (jackTokenizer.tokenType() == JackTokenizer.TYPE.IDENTIFIER) {
            return jackTokenizer.identifier();
        }
        throwError("int | char | boolean | className instead of " + jackTokenizer.getCurrentToken());
        return "";
    }

    /**
     * Checks for the presence of a specific symbol.
     *
     * - Advances the tokenizer and ensures the current token matches the expected symbol.
     *
     * Example:
     * Input: `checkForSymbol(';')`
     * Behavior: Throws an error if the current token is not `;`.
     *
     * Throws:
     * - Exception if the symbol does not match.
     */
    private void checkForSymbol(char symbol) {
        jackTokenizer.advance();
        if (jackTokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL
                || jackTokenizer.symbol() != symbol) {
            throwError("'" + symbol + "'");
        }
    }

    /**
     * Generates a unique label for VM code.
     *
     * - Combines the class name with an incrementing counter for uniqueness.
     *
     * Example:
     * Output: `ClassName_0`, `ClassName_1`, etc.
     *
     * Returns:
     * - A unique string label.
     */
    private String newLabel(){
        return className + "_" + (labelCounter++);
    }

}

