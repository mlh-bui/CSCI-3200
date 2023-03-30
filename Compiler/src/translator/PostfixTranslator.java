package translator;

import java.util.Stack;
import slu.compiler.*;

/*
 *  Syntax-directed definition to translate infix arithmetic expressions into postfix notation
 *
 *     translator  -> { postfix = "" } expression { print(postfix) }
 *
 *     expression  -> expression + term { postfix = postfix + "+" } |
 *                    expression - term { postfix = postfix + "-" }
 *                    term
 *
 *     term        -> term * factor { postfix = postfix + "*" } |
 *                    term / factor { postfix = postfix + "/" } |
 *                    term % factor { postfix = postfix + "%" } |
 *                    factor
 *
 *     factor      -> (expression) |
 *                    num { postfix = postfix + num.value }
 *
 *  Right-recursive SDD for a top-down recursive predictive parser WHAT WE WANT <--
 *
 *     translator  -> { postfix = "" } expression { print(postfix) }
 *
 *     expression  -> term moreTerms (think E --> TE' where E' = moreTerms)
 *
 *     moreTerms   -> + term { postfix = postfix + "+" } moreTerms |  <-- Semantic action = a part of moreTerms
 *                    - term { postfix = postfix + "-" } moreTerms |
 *                    epsilon
 *
 *     term        -> factor moreFactors
 *
 *     moreFactors -> * factor { postfix = postfix + "*" } moreFactors |
 *                    / factor { postfix = postfix + "/" } moreFactors |
 *                    % factor { postfix = postfix + "%" } moreFactors |
 *                    epsilon
 *
 *     factor      -> (expression) |
 *                    num { postfix = postfix + num.value }
 *
 *
 *  The expression 9 - 5 + 2 * 3 is translated into 9 5 - 2 3 * +
 *
 */

public class PostfixTranslator implements IPostfixTranslator {
    private IToken token;
    private ILexicalAnalyzer lexicalAnalyzer;
    private Stack<Integer> stack;
    private String postfix;

    // gets lexical analyzer objects as input, helper method which gets tokens as a scanner
    public PostfixTranslator(ILexicalAnalyzer lex) {
        this.lexicalAnalyzer = lex;
        this.token = this.lexicalAnalyzer.getToken();
        this.stack = new Stack<Integer>();
    }

    public String translate() throws Exception {
        this.postfix = "";

        expression();

        return this.postfix;
    }

    // rule = F --> ( E ) | int
    // at this point in the syntax analysis we must / expect a factor ie the rule
    private void factor() throws Exception {
        // if the next token is an add, begin the syntax rule with + or epsilon
        if(this.token.getName().equals("open_parenthesis")) {

            match("open_parenthesis");  // apply to terminal symbols for match

            expression(); // nonterminal symbols

            match("closed_parenthesis");

        } else if(this.token.getName().equals("int")) {

            IntegerNumber number = (IntegerNumber) this.token;

            this.postfix = this.postfix + ((this.postfix.length() == 0) ? "" : " ") + number.getValue() + " ";
            this.stack.push(number.getValue());

            match("int");
        } else {
            // throws exception if there is a lexical error ie token not expected only options = () or int
            throw new Exception("\n Error at line " + this.lexicalAnalyzer.getLine()
                    + ", invalid arithmetic expression: open parenthesis or int expected");
        }
    }

    public int evaluate() throws Exception {
        if (this.stack.empty())
            throw new Exception("\nError in expression, it cannot be evaluated!");

        return this.stack.pop();
    }

    private void expression() throws Exception {
        term(); moreTerms();
        // rule: Term --> Term and moreTerms
        // aka: T -> TE'
    }

    // T --> FT'
    private void term() throws Exception {
        factor(); moreFactors();
    }

    // moreTerms = E'
    // May be nothing, a plus or a minut
    // rule: E' --> +TE' | -TE' | epsilon
    private void moreTerms() throws Exception {
        if (this.token.getName().equals("add")) {
            match("add");

            term();

            this.postfix = this.postfix + " + ";

            int x = this.stack.pop();
            int y = this.stack.pop();

            this.stack.push(y + x);

        } else if(this.token.getName().equals("subtract")) {
            match("subtract");

            term();

            this.postfix = this.postfix + " - ";

            int x = this.stack.pop();
            int y = this.stack.pop();

            this.stack.push(y - x);
        } // epsilon = since it's an if and if else so option for nothing available, if/else = the other opt
    } // why no exception? --> epsilon = option, if there isn't an option for an epsilon need to throw

    // for T' --> *f | /f | %f | epsilon
    private void moreFactors() throws Exception {
        if (this.token.getName().equals("multiply")) {
            match("multiply");

            factor();

            this.postfix = this.postfix + " * ";

            int x = this.stack.pop();
            int y = this.stack.pop();

            this.stack.push(y * x);

            moreFactors();

        } else if(this.token.getName().equals("divide")) {
            match("divide");

            factor();

            this.postfix = this.postfix + " / ";

            int x = this.stack.pop();
            int y = this.stack.pop();

            this.stack.push(y / x);

            moreFactors();

        } else if (this.token.getName().equals("remainder")) {
            match("remainder");

            factor();

            this.postfix = this.postfix + " % ";

            int x = this.stack.pop();
            int y = this.stack.pop();

            this.stack.push(y % x);

            moreFactors();
        }
    }

    // code of the top-down recursive predictive parser

    private void match(String tokenName) throws Exception {
        // gets as input a token, checks if the input token matches current expected token
        if (this.token.getName().equals(tokenName))
            this.token = this.lexicalAnalyzer.getToken();
        else
            throw new Exception("\nError at line " + this.lexicalAnalyzer.getLine() + ", " + this.lexicalAnalyzer.getLexeme(tokenName) + " expected");
    }

}