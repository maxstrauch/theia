package de.theia.vm;

/**
 * A virtual machine exception
 * 
 * @author maximilianstrauch
 */
public class VMException extends RuntimeException {
    
    public VMException(String message) {
        super(message);
    }
    
}
