package be4rjp.cinema4c.exception;

public class DifferentVersionException extends Exception{
    private static final long serialVersionUID = 1L;
    
    public DifferentVersionException(String message){
        super(message);
    }
}
