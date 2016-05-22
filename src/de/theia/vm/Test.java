package de.theia.vm;

/**
 * Test playground
 * 
 * @author maximilianstrauch
 */
public class Test {
 
    public static void main(String[] args) {
        
        //String prog = "   1 : x1 := 1 ;\n   2 : x2 := 1";
        String prog = "1 : x1 := 1";
        //String prog = "1 : x1 := 1 ;\n 2 : x2 := 1";
        
        Lexer l = new Lexer(prog);
        while (l.hasNext()) {
            System.out.println(l.next() + ": " + l.getLexeme());
        }
        
        System.out.println("---");
        
        l = new Lexer(prog);
        Compiler c = new Compiler(l, Language.GOTO);
        int[] bytecode = c.compile();
        System.out.println(PrettyPrint.print(bytecode));
        
    }
    
}
