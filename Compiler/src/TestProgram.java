import slu.compiler.*;

public class TestProgram {

    public static void main(String[] args) {
        IToken tokenName;

        String expression = "((10 * 3) + (50 / 5)) * 2 / 4";

        try {
            ILexicalAnalyzer scanner = new LexicalAnalyzer(expression);

            do {
                tokenName = scanner.getToken();

                System.out.println("<" + tokenName.toString() + ">");

            } while (!tokenName.getName().equals("end_program"));

            System.out.println("");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}