import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * The `JackTokenizer` class is designed to tokenize a `.jack` file.
 * It processes the file line by line, tokenizing it into meaningful components for further processing.
 */
public class JackTokenizer {
    private Scanner scanner;
    private File fileToScan;
    private String currentLine;
    private String currentToken;
    private StringTokenizer stringTokenizer;

    /**
     * Enum representing different types of tokens.
     */
    public enum TokenType {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
    }

    /**
     * Constructor: Opens the input .jack file / stream and gets ready to tokenize it.
     *
     * @param inputFile The path to the .jack file to be tokenized.
     * @throws IOException If an I/O error occurs.
     */
    public JackTokenizer(String inputFile) throws IOException {
        fileToScan = new File(inputFile);
        scanner = new Scanner(fileToScan);
    }

    /**
     * Checks if there are more tokens in the input.
     *
     * @return `true` if there are more tokens, `false` otherwise.
     */
    public boolean hasMoreTokens() {
        if (stringTokenizer != null && stringTokenizer.hasMoreTokens()) {
            return true;
        } else {
            while (scanner.hasNextLine()) {
                currentLine = scanner.nextLine().split("//")[0].trim();
                // Skip blank lines and comments
                if (currentLine.isBlank() ||
                        currentLine.startsWith("//") ||
                        currentLine.startsWith("/*") ||
                        currentLine.startsWith("*")) {
                    continue;
                } else {
                    stringTokenizer = new StringTokenizer(currentLine, "(){},;[]._\"+-*/&|<>=~ ", true);
                    if (stringTokenizer.hasMoreTokens()) {
                        return true;
                    }
                    continue;
                }
            }
        }

        return false;
    }

    /**
     * Gets the next token from the input and makes it the current token.
     */
    public void advance() {
        currentToken = stringTokenizer.nextToken().trim();
        while (currentToken.equals("")) {
            currentToken = stringTokenizer.nextToken().trim();
        }
    }

    /**
     * Handles string constants by advancing through the tokens until the closing quote is found.
     *
     * @return The string constant without the opening and closing double quotes.
     */
    private String strAdvance() {
        String str = "";
        currentToken = stringTokenizer.nextToken();
        while (!currentToken.equals("\"")) {
            str += currentToken;
            currentToken = stringTokenizer.nextToken();
        }
        return str;
    }

    /**
     * Returns the type of the current token, as a constant.
     *
     * @return The type of the current token.
     */
    public TokenType tokenType() {
        if (currentToken == null) {
            System.err.println("Error: currentToken is null");
            return null; // No current token
        }

        // Check if the current token is a keyword
        switch (currentToken) {
            case "class":
            case "constructor":
            case "function":
            case "method":
            case "field":
            case "static":
            case "var":
            case "int":
            case "char":
            case "boolean":
            case "void":
            case "true":
            case "false":
            case "null":
            case "this":
            case "let":
            case "do":
            case "if":
            case "else":
            case "while":
            case "return":
                return TokenType.KEYWORD;
        }

        // Check if the current token is a symbol
        if ("{}()[].,;+-*/&|<>=~".contains(currentToken)) {
            return TokenType.SYMBOL;
        }

        // Check if the current token is an integer constant
        try {
            int value = Integer.parseInt(currentToken);
            if (value >= 0 && value <= 32767) {
                return TokenType.INT_CONST;
            }
        } catch (NumberFormatException e) {
            // Not an integer
        }

        // Check if the current token is a string constant
        if (currentToken.startsWith("\"") && currentToken.endsWith("\"")) {
            return TokenType.STRING_CONST;
        }

        // If none of the above, assume it's an identifier
        if (currentToken.matches("[a-zA-Z_][a-zA-Z_0-9]*")) {
            return TokenType.IDENTIFIER;
        }

        // If no type matches, return null
        System.err.println("Error: Unknown token type: " + currentToken);
        return null;
    }

    /**
     * Returns the keyword which is the current token, as a constant.
     *
     * @return The current token if it is a keyword.
     */
    public String keyWord() {
        return currentToken;
    }

    /**
     * Returns the character which is the current token.
     *
     * @return The current token if it is a symbol.
     */
    public char symbol() {
        return currentToken.charAt(0);
    }

    /**
     * Returns the string which is the current token.
     *
     * @return The current token if it is an identifier.
     */
    public String identifier() {
        return currentToken;
    }

    /**
     * Returns the integer value of the current token.
     *
     * @return The current token if it is an integer constant.
     */
    public int intVal() {
        return Integer.parseInt(currentToken);
    }

    /**
     * Returns the string value of the current token without the opening and closing double quotes.
     *
     * @return The current token if it is a string constant.
     */
    public String stringVal() {
        return strAdvance();
    }
}