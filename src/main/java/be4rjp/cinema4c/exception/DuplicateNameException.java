package be4rjp.cinema4c.exception;

public class DuplicateNameException extends Exception{
    private static final long serialVersionUID = 1L;
    
    public DuplicateNameException(String message){
        super(message);
    }
}
