package de.theia.vm;

/**
 * A simple Exception for compilation errors
 * 
 * @author maximilianstrauch
 */
public class RecognitionException extends RuntimeException {
    
    private int line, pos, abs, from, to;
    
    public RecognitionException(String message) {
        super(message);
    }
    
    public RecognitionException(String message, int line, int pos, int abs) {
        super(message);
        this.line = line;
        this.pos = pos;
        this.abs = abs;
    }
    
    public RecognitionException(String message, int from, int to) {
        super(message);
        this.from = from;
        this.to = to;
    }
    
    public RecognitionException(String message, int line, int pos, int from, int to) {
        super(message);
        this.line = line;
        this.pos = pos;
        this.from = from;
        this.to = to;
    }
    
    public boolean hasLineInfo() {
        return line > -1 && pos > -1;
    }
    
    public boolean hasSelectionInfo() {
        return abs > 0 || (from > -1 && to > -1);
    }
    
    public int getStart() {
        if (from > -1 && to > -1) {
            return from;
        } else {
            return abs - 1;
        }
    }
    
    public int getEnd() {
        if (from > -1 && to > -1) {
            return to;
        } else {
            return abs;
        }
    }
    
    public int getLine() {
        return line + 1;
    }
    
    public int getPos() {
        return pos;
    }
    
}
