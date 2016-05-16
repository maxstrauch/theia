package de.theia.vm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static de.theia.vm.Token.*;

/**
 * Lexer for lexing source code of the LOOP, WHILE and GOTO language
 * 
 * @author maximilianstrauch
 */
public class Lexer implements Iterator<Token> {
    
    /**
     * Keyword to token mapping
     */
    private static final Map<String,Token> KEYWORDS;

    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("loop", LOOP);
        KEYWORDS.put("do", DO);
        KEYWORDS.put("end", END);
        KEYWORDS.put("while", WHILE);
        KEYWORDS.put("if", IF);
        KEYWORDS.put("then", THEN);
        KEYWORDS.put("else", ELSE);
        KEYWORDS.put("goto", GOTO);
    }

    /**
     * The input stream reader
     */
    private final Reader reader;
    
    /**
     * The current lexeme
     */
    private StringBuffer lexeme = new StringBuffer();
    
    /**
     * The current character
     */
    private int lookahead;
    
    /**
     * The current token
     */
    private Token token = null;
    
    /**
     * Indicates if the end of file is reached
     */
    private boolean eof = false;
    
    /**
     * Stream positions for debugging
     */
    private int line = 0, pos = 0, abs = 0;
    
    /**
     * Creates a new lexer
     * 
     * @param program The program to lex
     */
    public Lexer(String program) {
        reader = new BufferedReader(new StringReader(program));
    }

    /**
     * Returns the current lexeme
     * 
     * @return The current lexeme
     */
    public String getLexeme() {
        return lexeme.toString();
    }

    /**
     * Current line
     * 
     * @return Line starting from zero
     */
    public int getLine() {
        return line;
    }

    /**
     * Current character relative to the current line
     * 
     * @return Character in line starting from zero
     */
    public int getPos() {
        return pos;
    }

    /**
     * The current absolute stream position
     * 
     * @return Index of the current character in the entire stream
     */
    public int getAbs() {
        return abs;
    }
    
    /**
     * Resets the lexeme and token
     */
    private void reset() throws IOException {
        if (eof) {
            throw new IllegalStateException();
        }
        lexeme.setLength(0);
        token = null;
        if (lookahead == 0) {
            read();
        }
    }
    
    /**
     * Reads the next character, appends it to the lexeme and moves it onto
     * the lookahead variable
     */
    private void read() throws IOException {
        if (eof) {
            throw new IllegalStateException();
        }
        if (lookahead != 0) {
            lexeme.append((char) lookahead);
        }
        lookahead = reader.read();
        
        if (lookahead == '\n') {
            line++;
            abs++;
            pos = 0;
        } else {
            pos++;
            abs++;
        }
    }
    
    /**
     * Recognizes the next token
     */
    private void lex() throws IOException, RecognitionException {
        reset();
        
        // Skip all whitespace characters
        while (Character.isWhitespace(lookahead)) {
            read();
        }
        reset();
        
        switch (lookahead) {
            
            case -1: /* EOF reached */
                eof = true;
                token = EOF;
                return;
                
            case ':': /* Either label separation or assign */
                token = COLON;
                read();
                if (lookahead == '=') {
                    token = ASSIGN;
                    read();
                }
                return;
               
            case ';': /* Statement sequence separator */
                token = SEMICOLON;
                read();
                return;
                
            case '=': /* Equality test */
                token = EQU;
                read();
                return;
                
            case '+': /* Math operator */
                token = PLUS;
                read();
                return;
                
            case '-': /* Math operator */
                token = MINUS;
                read();
                return;
                
            case '*': /* Math operator */
                token = MULT;
                read();
                return;
                
            case '!': /* The start of the not equals operator */
                read();
                if (lookahead == '=') {
                    token = NEQ;
                    read();
                    return;
                }
                break;
                
            case '<': /* Comparison operator */
                read();
                if (lookahead == '=') {
                    token = LTE;
                    read();
                    return;
                }
        }
        
        // Lex a variable
        if (lookahead == 'x' || lookahead == 'X') {
            boolean isDigit = true;
            read();
            do {
                isDigit &= Character.isDigit(lookahead);
                read();
            } while (lookahead > -1 && Character.isDigit(lookahead));

            if (isDigit) {
                token = VAR;
                return;
            }

            throw new RecognitionException(
                    "Variable expected but found: " + (char) lookahead,
                    line, 
                    pos, 
                    abs
            );
        }
        
        // Recognize keywords
        if (Character.isLetter(lookahead)) {
            do {
                read();
            } while (Character.isLetter(lookahead));
            
            String lexeme = getLexeme();
            if (KEYWORDS.containsKey(lexeme)) {
                token = KEYWORDS.get(lexeme);
                return;
            }

            throw new RecognitionException(
                    "Unkown keyword: '" + lexeme + "'", 
                    line, 
                    pos, 
                    abs - lexeme.length() - 1, 
                    abs - 1
            );
        }
        
        // Recognize numbers
        if (Character.isDigit(lookahead)) {
            do {
                read();
            } while (Character.isDigit(lookahead));
            token = NUM;
            return;
        }
        
        throw new RecognitionException(
                "Unkown character: '" + (char) lookahead + "'", 
                line, 
                pos, 
                abs
        );
    }
    
    @Override
    public boolean hasNext() {
        if (token != null) {
            return true;
        }
        
        if (eof) {
            return false;
        }
        
        try {
            lex();
        } catch (IOException e) {
            System.err.println("Lexer error: " + e);
        }
        
        return true;
    }

    @Override
    public Token next() {
        if (hasNext()) {
            Token retval = token;
            token = null;
            return retval;
        }
        
        throw new IllegalStateException();
    }
    
}
