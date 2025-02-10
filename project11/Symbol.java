/**
 * In this class we create a Symbol type object that contains the info of each line in the SymbolTable.
 */
public class Symbol {

    public static enum KIND { STATIC, FIELD, ARG, VAR, NONE }; // variable kinds.
    private String type;
    private KIND kind;
    private int index;

    /**
     * Create a Symbol type object.
     */
    public Symbol(String type, KIND kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    /**
     * Get the type of the symbol.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the kind of the symbol.
     */
    public KIND getKind() {
        return kind;
    }

    /**
     * Get the number of the symbol.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Print the information of the symbol.
     */
    public String toString() {
        return "Symbol {" + "t ype= '" + type + '\'' + ", kind=" + kind + ", index=" + index + " }";
    }
}