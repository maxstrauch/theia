package de.theia.vm;

/**
 * Tokens for the lexer
 * 
 * @author maximilianstrauch
 */
public enum Token {
    COLON, // separate label and statement
    SEMICOLON, // separate statements
    ASSIGN, // assignment symbol
    NEQ, // not equal
    EQU, // equal
    LTE, // less than equal
    PLUS, // arithmetic expression
    MINUS, // arithmetic expression
    MULT, // arithmetic expression
    
    // Kewords
    LOOP,
    DO,
    END,
    WHILE,
    IF,
    THEN,
    ELSE,
    GOTO,
    
    // Symbols
    VAR,
    NUM,
    EOF;
    
}
