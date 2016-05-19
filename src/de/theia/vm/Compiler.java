package de.theia.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import de.theia.vm.VM;
import static de.theia.vm.Token.*;

/**
 * A compiler to compile LOOP, WHILE and GOTO programs
 * 
 * @author maximilianstrauch
 */
public class Compiler {
    
    /**
     * The lexer object
     */
    private final Lexer lexer;
    
    /**
     * The current token
     */
    private Token lookahead;
    
    /**
     * The language selected for the current input
     */
    private final Language language;
    
    /**
     * Start and end position of the current token
     */
    private int start, end;
    
    /**
     * Maps for GOTO programs to adjust the GOTO addresses after the compilations
     */
    private final HashMap<Integer, Integer> labelMap, relocateMap;
    
    /**
     * Program instructions list
     */
    private final List<Integer> program;
    
    /**
     * Creates a new compiler to compile a LOOP, WHILE or GOTO program to
     * bytecode which can be executed by {@link VM}
     * 
     * @param lexer The lexer
     * @param language The language which is given as input
     */
    public Compiler(Lexer lexer, Language language) {
        this.lexer = lexer;
        this.language = language;
        labelMap = new HashMap<>();
        relocateMap = new HashMap<>();
        program = new ArrayList<>();
    }
    
    /**
     * Match the current token. If the current token is not the given token
     * a {@link RecognitionException} is thrown
     * 
     * @param token The token to match
     * @return The lexeme for the current token
     */
    private String match(Token token) {
        if (test(token)) {
            lookahead = null;
            
            System.out.println("MATCH: " + token + " @ " + lexer.getLexeme());
            
            return lexer.getLexeme();
        } else {
            throw new RecognitionException(
                    "Expected '" + token + "' but found '" + lookahead + "'", 
                    start, end
            );
        }
    }
    
    /**
     * Tests if the current token is the given token
     * 
     * @param token The token to compare the current to
     * @return <code>true</code> if the current token matches the given one
     * otherwise <code>false</code>
     */
    private boolean test(Token token) {
        if (lookahead == null) {
            start = lexer.getAbs() - 1;
            lookahead = lexer.next();
            end = lexer.getAbs() - 1;
        }
        return lookahead == token;
    }
    
    /**
     * Tests if the next token is one of the given tokens
     * 
     * @param tokens The tokens to test if the current is one of them
     * @return <code>true</code> if the current token matches the given one
     * otherwise <code>false</code>
     */
    private boolean test(Token...tokens) {
        if (lookahead == null) {
            lookahead = lexer.next();
        }
        for (Token token : tokens) {
            if (lookahead == token) {
                return true;
            }
        }
        return false;
    }
    
//    /**
//     * Skips the lexeme
//     * 
//     * @param token The token to skip
//     * @return <code>true</code> if the given token was found and skipped
//     * and otherwise <code>false</code>
//     */
//    private boolean skip(Token token) {
//        if (test(token)) {
//            match(token);
//            return true;
//        }
//        return false;
//    }
    
    /**
     * Skips the current Token
     * 
     * @return The lexeme for the skipped token
     */
    private String skip() {
        lookahead = null;
        return lexer.getLexeme();
    }
    
    /**
     * Compiles a LOOP, WHILE or GOTO program to bytecode which can be executed
     * using the {@link VM} or pretty printed using {@lonk PrettyPrint}
     * 
     * @return The compiled bytecode
     */
    public int[] compile() {
        
        if (language == Language.GOTO) {
            // Match GOTO statements. A GOTO statement consists of:
            //   <NUM> <:> stmt <;>
            // where NUM is the line number which is used in the GOTO-IF 
            // statements. Therefore the line numbers must be cached to 
            // calculate the program address to jump to
            Set<Integer> usedLineNumbers = new HashSet<>();
            
            while (true) {
                if (!test(NUM)) {
                    throw new RecognitionException(
                            "A GOTO statement needs a line number",
                            start, end
                    );
                }
                
                int num = Integer.parseInt(match(NUM));
                if (usedLineNumbers.contains(num)) {
                    throw new RecognitionException(
                            "Line number '" + num + "' already used",
                            start, end
                    );
                }
                usedLineNumbers.add(num);
                
                labelMap.put(num, program.size());
                match(COLON);
                
                // Match a normal statement
                consumeStmt();

                // If the end of the program is reached end compilation
                if (test(EOF)) {
                    break;
                }

                // Finish this line with a SEMICOLON
                match(SEMICOLON);
                
                // Issue #8: if a GOTO "line" is terminated with a semicolon
                // another line must follow
                if (test(EOF)) {
                    throw new RecognitionException(
                            "Program ends with semicolon, but this is not allowed",
                            start, end
                    );
                }
            }
            
            // Fill out all jump addresses which were not known during
            // compilation
            for (Integer pos : relocateMap.keySet()) {
                int addr = labelMap.get(relocateMap.get(pos));
                program.set(pos, addr);
            }
        } else {
            // Consume all statements of LOOP and WHILE
            consumeStmt();
        }
        
        // Convert to an array
        int[] bytecode = new int[program.size()];
        for (int i = 0; i < bytecode.length; i++) {
            bytecode[i] = program.get(i);
        }
        return bytecode;
    }
    
    /**
     * Consumes a statement
     */
    private void consumeStmt() {
        // In most cases this is triggered if a SEMICOLON is set at the end
        // of the last statement. But the sequence is defined as "P ; P" therefore
        // no ending SEMICOLON
        if (test(EOF)) {
            throw new RecognitionException("No statement provided", start, end);
        }
        
        // Find statements
        if (test(VAR)) {
            consumeAssign();
        }
        
        if (test(LOOP)) {
            if (language != Language.LOOP) {
                throw new RecognitionException(
                        "Illegal statement loop in lang " + language,
                        start, end
                );
            }
            consumeLoop();
        }
        
        if (test(WHILE)) {
            if (language != Language.WHILE) {
                throw new RecognitionException(
                        "Illegal statement while in lang " + language, 
                        start, end
                );
            }
            consumeWhile();
        }
        
        if (test(IF)) {
            consumeIf();
        } 
        
        // Issue #7: this is the sequence statement which only should active
        // for LOOP/WHILE programs since GOTO programs have their own line
        // termination semicolon match statement in compile()
        if (language != Language.GOTO && test(SEMICOLON)) {
            match(SEMICOLON);
            consumeStmt();
        }
    }
    
    /**
     * Consume an assign statement
     */
    private void consumeAssign() {
        int arg1, arg2 = -1, dst, func = -1;
        
        // Get the destination
        dst = asVar(match(VAR));
        match(ASSIGN);
        
        // Get the first argument
        arg1 = consumeExprArgument();
            
        // If an operator follows this is a binary expression
        if (test(PLUS, MINUS, MULT)) {
            // Get the operator
            String op = skip();
            switch (op) {
                case "+": func = 0; break;
                case "-": func = 1; break;
                case "*": func = 2; break;
                default:
                    throw new RecognitionException("Unkown operator '" + op + "'");
            }
            
            // Get the second 
            arg2 = consumeExprArgument();
        }
        
        if (func < 0) {
            // If no expression function was detected, this is a mov
            program.add(0x2d);
            program.add(arg1);
            program.add(dst);
        } else {
            // Arithmetic expression
            program.add(0x2a + func);
            program.add(arg1);
            program.add(arg2);
            program.add(dst);
        }
    }
    
    /**
     * Consume a loop
     */
    private void consumeLoop() {
        match(LOOP);
        String var = match(VAR);

        // Push the loop counter onto the stack
        program.add(0x10);
        program.add(asVar(var));

        // Address of the first instruction of the loop body
        int addr = program.size();
        
        // Branch out of the loop if condition matches
        program.add(0x13);
        int ifEndAddr = program.size();
        program.add(-1);
        
        // Consume the body of the loop
        match(DO);
        consumeStmt();
        match(END);
        
        // After body of the loop:
        // Decrement the value from stack (loop count)
        program.add(0x12);
        
        // Jump back to the beginning
        program.add(0x21);
        program.add(addr);
        
        // Set the address of the end
        program.set(ifEndAddr, program.size());
        
        // If the loop is finished: remove the value from the stack
        program.add(0x11);
    }   
    
    /**
     * Consumes an expression argument (VAR or NUM) and returns it
     * 
     * @return The consumed expression argument
     */
    private int consumeExprArgument() {
        int arg1;
        if (test(VAR)) {
            arg1 = asVar(match(VAR));
        } else if (test(NUM)) {
            arg1 = asNum(match(NUM));
        } else {
            throw new RecognitionException(
                    "A binary expression needs to have the form: arg op arg",
                    start, end
            );
        }
        return arg1;
    }
    
    /**
     * Consumes an if condition statement
     */
    private void consumeIf() {
        int arg1, arg2, cmp;
        match(IF);
        
        // Consume the condition:
        // The first argument
        arg1 = consumeExprArgument();
        
        // Get the operator
        if (test(EQU)) {
            skip();
            cmp = 0;
        } else if (test(LTE)) {
            skip();
            cmp = 1;
        } else {
            throw new RecognitionException("Illegal compare operator", start, end);
        }
        
        // The second argument
        arg2 = consumeExprArgument();

        // If the current language is GOTO and we read a GOTO statement
        // this is not a regular if, it is a IF-GOTO statement, so consume it ...
        if (test(GOTO) && language == Language.GOTO) {
            if (cmp != 0) {
                throw new RecognitionException(
                        "Must use 'equal zero' comparison for GOTO-IF",
                        start, end
                );
            }
            
            match(GOTO);
            
            // If equal zero goto the desired address. Since we possibly don't
            // have consumed the entire program this address could point to
            // a line which will be parsed in the future so the address of
            // the line is not known. Besides that GOTO takes a line number
            // and this can't mapped 1:1 to instruction addresses. Therefore
            // memorize the location of the address and the desired line numer
            // and replace it at the end with the right address
            program.add(0x44);
            program.add(arg1);
            program.add(arg2);
            relocateMap.put(program.size(), Integer.parseInt(match(NUM)));
            program.add(-1);
            return;
        }
        
        // The jump-on-if instruction to jump to the right location
        int elseAddrPos;
        program.add(0x42 + cmp);
        program.add(arg1);
        program.add(arg2);
        elseAddrPos = program.size();
        program.add(-1);
        
        // Consume the THEN branch
        match(THEN);
        consumeStmt();
        
        // Skip the ELSE branch when the THEN branch was executed
        program.add(0x21);
        int endAddrPos = program.size();
        program.add(-1);
        // Set the address of the first statement of the ELSE branch
        program.set(elseAddrPos, program.size());
        
        // Consume the ELSE branch
        match(ELSE);
        consumeStmt();
        match(END);
        
        // Add a dummy instruction at the end to jump to
        // after executing the THEN branch in order to skip the ELSE branch
        program.set(endAddrPos, program.size());
        program.add(0x99);
    }
    
    /**
     * Consumes a while statement and generates the bytecode
     */
    private void consumeWhile() {
        match(WHILE);
        
        // A while condition must be a not equal zero test
        String var = match(VAR);
        
        if (!test(NEQ)) {
            throw new RecognitionException("While condition: operator must be 'not equal' (!=)");
        }
        
        match(NEQ);
        String num = match(NUM);
        
        if (!"0".equals(num)) {
            throw new RecognitionException("While condition: must test on not equal zero");
        }
        
        // Jump if register is equal zero to the next instruction
        // after this while loop (e.g. repeat while register not equal zero)
        int startAddrPos = program.size();
        program.add(0x44);
        program.add(asVar(var));
        program.add(0);
        int endAddrPos = program.size();
        program.add(-1); // Placeholder to be replaced with end address
        
        // Consume body of while loop
        match(DO);
        consumeStmt();
        match(END);
        
        // Goto the beginning of the loop
        program.add(0x21);
        program.add(startAddrPos);
        
        // Next instruction after the loop is a dummy instruction (nop)
        // Set the target address to jump to in the initial if test
        program.set(endAddrPos, program.size());
        program.add(0x99);
    }
    
    /**
     * Converts a {@link String} to an {@link Integer} with the information
     * encoded that it is a variable
     * 
     * @param var The numeric variable/register identifier
     * @return The encoded value
     */
    private static int asVar(String var) {
        int v = Integer.parseInt(var.substring(1));
        int arg1 = 0x80000000 | (0x7fffffff & v);
        return arg1;
    }
    
    /**
     * Converts a {@link String} to an {@link Integer} with the information
     * encoded that it is a number (immediate value)
     * 
     * @param num The number
     * @return The encoded value
     */
    private static int asNum(String num) {
        int v = Integer.parseInt(num);
        int arg1 = 0x7fffffff & v;
        return arg1;
    }
    
}
