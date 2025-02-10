import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
    public static enum TYPE { KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST, NONE }; // Constant for types.
    public static enum KEYWORD {
        CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID,
        VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS
    };

    private String token;
    private TYPE tokenType;
    private int pointer;
    private ArrayList<String> tokens;
    private static Pattern tokenPatterns;
    private static String keywordReg;
    private static String symbolReg;
    private static String intReg;
    private static String stringReg;
    private static String idReg;
    private static HashMap<String,KEYWORD> keyword = new HashMap<String, KEYWORD>();
    private static HashSet<Character> op = new HashSet<Character>();
    
    static {
        // Array of keyword strings and their corresponding KEYWORD enum values
        String[][] keywordsArray = {
            {"class", "CLASS"}, {"constructor", "CONSTRUCTOR"}, {"function", "FUNCTION"},
            {"method", "METHOD"}, {"field", "FIELD"}, {"static", "STATIC"},
            {"var", "VAR"}, {"int", "INT"}, {"char", "CHAR"}, {"boolean", "BOOLEAN"},
            {"void", "VOID"}, {"true", "TRUE"}, {"false", "FALSE"}, {"null", "NULL"},
            {"this", "THIS"}, {"let", "LET"}, {"do", "DO"}, {"if", "IF"},
            {"else", "ELSE"}, {"while", "WHILE"}, {"return", "RETURN"}
        };
    
        // Populate the keyword map
        for (String[] pair : keywordsArray) {
            keyword.put(pair[0], KEYWORD.valueOf(pair[1]));
        }
    
        // Array of operator characters
        char[] opsArray = {'+', '-', '*', '/', '&', '|', '<', '>', '='};
    
        // Populate the operator set
        for (char opChar : opsArray) {
            op.add(opChar);
        }
    }

    /*
     * Opens the input .jack file/ stream and gets readt to tokenize it.
     */
    public JackTokenizer(File file) {
        try {
            Scanner scan = new Scanner(file);
            String preprocessed = "";
            String line = "";

            while(scan.hasNext()){
                line = removeComments(scan.nextLine()).trim();
                if (line.length() > 0) {
                    preprocessed += line + "\n";
                }
            }
            preprocessed = removeColorBlocks(preprocessed).trim();
            initRegs();

            Matcher match = tokenPatterns.matcher(preprocessed);
            tokens = new ArrayList<String>();
            pointer = 0;
            while (match.find()){
                tokens.add(match.group());
            }
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        token = "";
        tokenType = TYPE.NONE;
    }

     /**
     * inti regex we need in tokenizer
     */
    private void initRegs(){
        keywordReg = "";

        for (String seg: keyword.keySet()) {
            keywordReg += seg + "|";
        }

        symbolReg = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
        intReg = "[0-9]+";
        stringReg = "\"[^\"\n]*\"";
        idReg = "[a-zA-Z_]\\w*";
        tokenPatterns = Pattern.compile(idReg + "|" + keywordReg + symbolReg + "|" + intReg + "|" + stringReg);
    }

    public boolean hasMoreTokens() {
        return (pointer < tokens.size());
    }

    /**
     * advances the pointer and sets the current token
     */
    public void advance(){
        if (hasMoreTokens()) {
            token = tokens.get(pointer);
            pointer++;
        }
        else {
            throw new IllegalStateException("there is no tokens");
        }

        if (token.matches(keywordReg)) {
            tokenType = TYPE.KEYWORD;
        }
        else if (token.matches(symbolReg)) {
            tokenType = TYPE.SYMBOL;
        }
        else if (token.matches(intReg)) {
            tokenType = TYPE.INT_CONST;
        }
        else if (token.matches(stringReg)) {
            tokenType = TYPE.STRING_CONST;
        }
        else if (token.matches(idReg)) {
            tokenType = TYPE.IDENTIFIER;
        }
        else {
            throw new IllegalArgumentException("no such token:" + token);
        }
    }

    /**
     * Get the current token.
     */
    public String getCurrentToken() {
        return token;
    }

    /**
     * Returns the type of the current token, as a constant.
     */
    public TYPE tokenType() {
        return tokenType;
    }

    /**
     * Returns the keyword which is the current token, as a constant.
     * This method should be called only if tokenType is KEYWORD.
     */
    public KEYWORD keyWord() {
        if (tokenType == TYPE.KEYWORD) {
            return keyword.get(token);
        }
        else {
            throw new IllegalStateException("the token is not a keyword!, it is a " + tokenType);
        }
    }

    /**
     * Returns the character which is the current token.
     * Should be called only if tokenType is SYMBOL.
     */
    public char symbol(){
        if (tokenType == TYPE.SYMBOL)
        {
            return token.charAt(0);
        }
        else {
            throw new IllegalStateException("the token is not a symbol! it is a " + tokenType);
        }
    }

    /**
     * Returns the string which is the current token. 
     * Should be called only if tokenType is IDENTIFIER.
     */
    public String identifier() {
        if (tokenType == TYPE.IDENTIFIER) {
            return token;
        }
        else {
            throw new IllegalStateException("the token is not an identifier! it is a " + tokenType);
        }
    }

    /**
     * Returns the integer value of the current token.
     * Should be called only if tokenType is INT_CONST.
     */
    public int intVal(){
        if (tokenType == TYPE.INT_CONST) {
            return Integer.parseInt(token);
        }
        else {
            throw new IllegalStateException("the token is not an integer constant! it is a " + tokenType);
        }
    }

    /**
     * Returns the string value of the current token, without the opening and closing double qoutes.
     * Should be called only if tokenType is STRING_CONST.
     */
    public String stringVal() {
        if (tokenType == TYPE.STRING_CONST) {
            return token.substring(1, token.length() - 1);
        }
        else {
            throw new IllegalStateException("the token is not a string constant! it is a " + tokenType);
        }
    }

    /**
     * move pointer back
     */
    public void movePointerBack() {
        if (pointer > 0) {
            pointer--;
            token = tokens.get(pointer);
        }
    }

    /**
     * return if current symbol is a op
     */
    public boolean isOp() {
        return op.contains(symbol());
    }

    /**
     * Delete comments(String after "//") from a String
     * @param str
     */
    public static String removeComments(String str) {
        int position = str.indexOf("//");
        if (position != -1) {
            str = str.substring(0, position);
        }

        return str;
    }
    
    /**
     * Delete spaces from a String
     * @param str
     * @return
     */
    public static String removeWhiteSpaces(String str) {
        String result = "";

        if (str.length() != 0) {
            String[] segs = str.split(" ");
            for (String s: segs) {
                result += s;
            }
        }
        return result;
    }

    /**
     * delete block comment
     * @param str
     */
    public static String removeColorBlocks(String str) {
        int startIndex = str.indexOf("/*");
        if (startIndex == -1) {
            return str;
        }

        String res = str;
        int endIndex = str.indexOf("*/");
        while(startIndex != -1) {
            if (endIndex == -1) {
                return str.substring(0,startIndex - 1);
            }

            res = res.substring(0,startIndex) + res.substring(endIndex + 2);
            startIndex = res.indexOf("/*");
            endIndex = res.indexOf("*/");
        }
        return res;
    }
}

