package core;

public class Condition {
    private String name;
    private double value;
    private double preValue; // to calculate diff between before and after post conditions for model updating.
    
    private static double INITIAL_VALUE = 0.5;
    
    public Condition(String _name) {
        name = _name;
        value = INITIAL_VALUE;
        preValue = INITIAL_VALUE;
    }
    
    public void setValue(double _value) {
        this.value = _value;
    }
    
    public void setPreValue(double _preValue) {
        this.preValue = _preValue;
    }
    
    public String getName() {
        return name;
    }
    
    public double getValue() {
        return value;
    }
    
    public double getPreValue() {
        return preValue;
    }
    
    public double getINITIALVALUE() {
        return Condition.INITIAL_VALUE;
    }
}
