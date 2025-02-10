import java.util.HashMap;

public class SymbolTable extends HashMap<String, Integer> {

    public SymbolTable() {
        super();
    }

    public void addEntry(String symbol, int address) {
        this.put(symbol, address);
    }

    public boolean contains(String symbol) {
        return this.containsKey(symbol);
    }

    public int getAddress(String symbol) {
        return this.get(symbol);
    }
}