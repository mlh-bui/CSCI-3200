package translator;

import slu.compiler.*;

public class TestProgram {

    public static void main(String[] args) {
        IToken tokenName;
        boolean showTokens = false;

        String expression = "((10 * 3) + (50 / 5)) * 2 / 4";

        try {

            ILexicalAnalyzer scanner = new LexicalAnalyzer(expression);

            if (showTokens) {
                do {
                    tokenName = scanner.getToken();

                    System.out.println("<" + tokenName.toString() + ">");

                } while (!tokenName.getName().equals("end_program"));

                System.out.println("");
            }

            IPostfixTranslator postfix = new PostfixTranslator( new LexicalAnalyzer(expression) );

            System.out.println("Infix expression   " + expression);
            System.out.println("Postfix expression " + postfix.translate());
            System.out.println("The expression value is " + postfix.evaluate() + "\n");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}