package declarations;

import slu.compiler.*;

public class TestProgram {
    /*
    program                ->  void main { declarations }
    declarations           ->  declaration declarations | ε
    declaration            ->  type identifiers ;               <-- D
    type                   ->  int | float | boolean
    identifiers            ->  id more-identifiers              <-- L
    more-identifiers       ->  , id more-identifiers | ε        <-- L'
     */

    // Grammar = Slides 38, 39 similar but not completely the same
    // D --> T L ;
    // T --> int | float
    // L --> id L'
    // L' --> , id L' | epsilon

    // Example = int a , b ;    <-- Declares data types
    public static void main(String[] args) {
        IToken tokenName;
        boolean showTokens = true;

        // Want to: Parse input string which delcares variables
        String program = "void main { int a, b, c, d; float x, y, z; boolean halt; }";

        try {

            if (showTokens) {
                ILexicalAnalyzer scanner = new LexicalAnalyzer(program);

                do {
                    tokenName = scanner.getToken();

                    System.out.println("<" + tokenName.toString() + ">");

                } while (!tokenName.getName().equals("end_program"));

                System.out.println("");
            }

            SyntaxAnalyzer parser = new SyntaxAnalyzer( new LexicalAnalyzer(program) );

            parser.compile();

            System.out.println("The symbol table \n\n" + parser.symbolTable());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}