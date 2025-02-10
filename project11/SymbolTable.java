import java.util.HashMap;

public class SymbolTable {
    private HashMap<String,Symbol> classtable;
    private HashMap<String,Symbol> subroutineTable;
    private HashMap<Symbol.KIND,Integer> variables;

    /**
     * Creates a new symbol table.
     */
    public SymbolTable() {
        classtable = new HashMap<String, Symbol>();
        subroutineTable = new HashMap<String, Symbol>();
        variables = new HashMap<Symbol.KIND,Integer>();
        variables.put(Symbol.KIND.ARG, 0);
        variables.put(Symbol.KIND.FIELD, 0);
        variables.put(Symbol.KIND.STATIC, 0);
        variables.put(Symbol.KIND.VAR, 0);
    }

    /**
     * Starts a new subroutine scope.
     * Resets the subroutine's symbol table.
     */
    public void startSubroutine() {
        subroutineTable.clear();

        variables.put(Symbol.KIND.VAR, 0);
        variables.put(Symbol.KIND.ARG, 0);
    }

    /*
     * Defines (adds to the table) a new variable of the given name, type and kind.
     * Assigns to it the index value of that kind and adds 1 to the index.
     */
    public void define (String name, String type, Symbol.KIND kind) {
        if (kind == Symbol.KIND.ARG || kind == Symbol.KIND.VAR) {
            int index = variables.get(kind);
            Symbol symbol = new Symbol(type, kind, index);
            variables.put(kind, index + 1);
            subroutineTable.put(name, symbol);
        }
        else if (kind.equals(Symbol.KIND.STATIC) || kind.equals(Symbol.KIND.FIELD)) {
            int index = variables.get(kind);
            Symbol symbol = new Symbol(type, kind, index);
            variables.put(kind, index + 1);
            classtable.put(name, symbol);
        }
    }

    /*
     * Returns the number of variables of the given kind already defined in the table.
     */
    public int varCount(Symbol.KIND kind) {
        return variables.get(kind);
    }

    /*
     * Returns the kind of the named identifier.
     * if the identifier is not found, returns NONE.
     */
    public Symbol.KIND kindOf (String name) {
        Symbol symbol = checkSymbolExists(name);

        if (symbol != null) {
            return symbol.getKind();
        }

        return Symbol.KIND.NONE;
    }

    /*
     * Returns the type of the named variable.
     */
     public String typeOf (String name) {
        Symbol symbol = checkSymbolExists(name);
        if (symbol != null) {
            return symbol.getType();
        }

        return "";
    }

    public int indexOf (String name) {
        Symbol symbol = checkSymbolExists(name);
        if (symbol != null) {
            return symbol.getIndex();
        }

        return -1;
    }

    private Symbol checkSymbolExists(String name){
        if (classtable.get(name) != null) {
            return classtable.get(name);
        }
        else if (subroutineTable.get(name) != null) {
            return subroutineTable.get(name);
        }
        else {
            return null;
        }
    }
}
