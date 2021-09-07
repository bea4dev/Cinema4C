package be4rjp.cinema4c.util;

public class IntegerRange {
    
    public static IntegerRange fromString(String line){
        String[] args = line.replace(" ", "").split("-");
        return new IntegerRange(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
    
    
    private final int min;
    
    private final int max;
    
    public IntegerRange(int var1, int var2){
        this.min = Math.min(var1, var2);
        this.max = Math.max(var1, var2);
    }
    
    public boolean isInRange(int var){
        return min <= var && var <= max;
    }
    
    @Override
    public String toString() {
        return min + " - " + max;
    }
}
