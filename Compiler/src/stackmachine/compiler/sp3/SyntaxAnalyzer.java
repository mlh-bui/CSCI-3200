package stackmachine.compiler.sp3;

import slu.compiler.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Rules
 * program                ->  void main { declarations instructions }
 * declarations           ->  declaration declarations | ε
 * declaration            ->  type identifiers ;
 * type                   ->  int | float | boolean
 * identifiers            ->  id optional-declaration more-identifiers
 * more-identifiers       ->  , id optional-declaration more-identifiers | ε
 * optional-declaration   ->  = logic-expression | [num] | ε
 * instructions           ->  instruction instructions | ε
 * assignment             ->  optional-array = logic-expression |
 * optional-array         ->  [expression]    | ε
 * optional-array         ->  [expression]
 * expression             ->  expression + term |
 *                            expression - term |
 *                            term
 * term                   ->  term * factor |
 *                            term / factor |
 *                            term % factor |
 *                            factor
 * factor                 ->  (expression)      |
 *                            id optional-array |
 *                            num
 *
 * NEED THESE PARTS
 * instruction            ->  declaration                                        |
 *                            id assignment ;                                    |
 *                            if (logic-expression) instruction                  |
 *                            if (logic-expression) instruction else instruction |
 *                            while (logic-expression) instruction               |
 *                            do instruction while (logic-expression) ;          |
 *                            print (expression) ;                               |
 *                            { instructions }
 *
 * logic-expression       ->  logic-expression || logic-term |     // the '||' = the lexicon symbol, apply left recursion <--, if type = bool then true?
 *                            logic-term
 * logic-term             ->  logic-term && logic-factor |         // these && = the lexicon symbol
 *                            logic-factor
 *
 * logic-factor           ->  ! logic-factor | true | false |
 *                            relational-expression
 *
 * relational-expression  ->  expression relational-operator expression |
 *                            expression
 *
 * relational-operator    ->  < | <= | > | >= | == | !=
 * END OF PARTS NEED
 *
 * Convert to Right-recursive SDD for a top-down recursive predictive parser
 * logic-expression -> logic-term more-logic-expression
 *
 * more-logic-expression -> || logic-term more-logic-expression |
 *                             epsilon
 *
 *
 * logic-term -> logic-factor more-logic-term
 *
 * more-logic-term -> && logic factor more-logic-term |
 *                             epsilon
 *
 *
 * relational-expression -> expression more-relational-expression
 * more-relational-expression -> relational-operator expression relational-expression-prime |
 *                             epsilon
 */
public class SyntaxAnalyzer implements ISyntaxAnalyzer {
    private IToken token;
    private ILexicalAnalyzer scanner;
    private Map<String, IDataType> symbols;
    private List<String> code;

    private static int counter = 0;

    public SyntaxAnalyzer(ILexicalAnalyzer lex) {
        this.scanner = lex;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
        this.code = new ArrayList<String>();
    } // class SyntaxAnalyzer

    public String compile() throws Exception {
        program();

        // stack machine code

        String code = "";

        for (String instruction : this.code)
            code = code + instruction + "\n";

        return code;
    } // method compile

    private void program() throws Exception {
        match("void");
        match("main");
        match("open_curly_bracket");

        declarations();
        instructions();

        match("closed_curly_bracket");

        this.code.add("halt");
    } // method program

    private void declarations() throws Exception {
        if (this.token.getName().equals("int") || this.token.getName().equals("float") || this.token.getName().equals("boolean")) {
            declaration();
            declarations();
        }
    } // method declarations

    private void declaration() throws Exception {
        identifiers(type());
        match("semicolon");
    } // method declaration

    private String type() throws Exception {
        String type = this.token.getName();

        if (type.equals("int")) {
            match("int");
        } else if (type.equals("float")) {
            match("float");
        } else if (type.equals("boolean")) {
            match("boolean");
        }

        return type;
    } // method type

    private void identifiers(String type) throws Exception {
        if (this.token.getName().equals("id")) {
            Identifier id = (Identifier) this.token;

            if (this.symbols.get(id.getLexeme()) == null)
                this.symbols.put(id.getLexeme(), new PrimitiveType(type));
            else
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");

            match("id");

            optionalDeclaration(type, id);

            moreIdentifiers(type);
        }
    } // method identifiers

    private void moreIdentifiers(String type) throws Exception {
        if (this.token.getName().equals("comma")) {
            match("comma");

            Identifier id = (Identifier) this.token;

            if (this.symbols.get(id.getLexeme()) == null)
                this.symbols.put(id.getLexeme(), new PrimitiveType(type));
            else
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");

            match("id");

            optionalDeclaration(type, id);

            moreIdentifiers(type);
        }
    } // method moreIdentifiers

    private void optionalDeclaration(String type, Identifier id) throws Exception {
        if (this.token.getName().equals("assignment")) {

            match("assignment");

            // the token 'assignment' allows to assign  an initial value to a variable in the declaration

            this.code.add("push " + id.getLexeme());

            expression();

            this.code.add("store");

        } else if (this.token.getName().equals("open_square_bracket")) {

            // the token 'open_square_bracket' declares an array of int, float or boolean

            match("open_square_bracket");

            // array of a primitive data  type: int, float, boolean

            int size = 1;

            if (this.token.getName().equals("int")) {
                IntegerNumber number = (IntegerNumber) this.token;

                size = number.getValue();

                this.code.add("array " + id.getLexeme() + " " + type + " " + size);
            }

            match("int");
            match("closed_square_bracket");

            this.symbols.put(id.getLexeme(), new ArrayType(type, size));

        }
    } // method optionalDeclaration

    private void instructions() throws Exception {
        String tokenName = this.token.getName();

        // check the tokens in FIRST(instruction)

        // this if should contain the FIRST of instruction <-- aka we need all the tokens for declarations
        // since instructions --> declarations
        // so when adding if, while, do-while <-- need in instructions !!
        if (tokenName.equals("int")
                || tokenName.equals("float")
                || tokenName.equals("boolean")
                || tokenName.equals("id")
                || tokenName.equals("print")
                || tokenName.equals("if")
                || tokenName.equals("while")
                || tokenName.equals("do")) {
            instruction();
            instructions();
        }
    } // method instructions

    // FIRST(instruction) = { int, float, and boolean, id, print open_curly_bracket }
    // why types included in first? bc it's the first of type which is the first of declaration
    private void instruction() throws Exception {
        String tokenName = this.token.getName();

        // check the tokens in FIRST(instruction)

        if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean")) {

            declaration();

        } else if (tokenName.equals("id")) {

            assignment();
            match("semicolon");

        } else if (tokenName.equals("print")) {
            match("print");
            match("open_parenthesis");

            expression();

            this.code.add("print");

            match("closed_parenthesis");
            match("semicolon");

        } else if (tokenName.equals("if")) {

            match("if");
            match("open_parenthesis");

            logicExpression();

            String out = newLabel();

            this.code.add("gofalse label " + out);

            match("closed_parenthesis");

            instruction();

            if(this.token.getName().equals("else")) {
                optionalElse(out);
            } else {

                this.code.add(out + ":");
            }

        } else if (tokenName.equals("while")) {
            match("while");
            match("open_parenthesis");

            String test = newLabel();
            this.code.add("label " + test + ":");

            logicExpression();

            String out = newLabel();

            this.code.add("gofalse label " + out);

            match("closed_parenthesis");

            instruction();

            this.code.add("goto label " + test);
            this.code.add("label " + out + ":");

        } else if (tokenName.equals("do")) {
            match("do");
            match("open_curly_bracket");

            instruction();

            String test = newLabel();
            this.code.add("label " + test + ":");

            match("closed_curly_bracket");

            if(tokenName.equals("while")) {

                match("open_parenthesis");

                logicExpression();

                String out = newLabel();

                this.code.add("gofalse label " + out);

                match("closed_parenthesis");

                this.code.add("goto label " + test);
                this.code.add("label " + out + ":");
            }

        } else if (tokenName.equals("open_curly_bracket")) {
            match("open_curly_bracket");

            instructions();

            match("closed_curly_bracket");
        }

    } // method instruction

    private void optionalElse(String else1) throws Exception {
        String out = newLabel();

        this.code.add("goto label " + out);
        this.code.add("label " + else1 + ":");

        if(this.token.getName().equals("else")) {
            match("else");

            instruction();

            this.code.add("label " + out + ":");
        }
    }

    // assignment             ->  optional-array = logic-expression |
    private void assignment() throws Exception {
        Identifier id = (Identifier) this.token;

        if (this.symbols.get(id.getLexeme()) == null) {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is not declared");
        }

        this.code.add("push " + id.getLexeme());

        match("id");

        optionalArray(id);

        match("assignment");

        expression();

        this.code.add("store");
    } // method assignment

    // logic-factor           ->  ! logic-factor | true | false | relational-expression
    private void logicFactor() throws Exception {
        String tokenName = this.token.getName();

        if(tokenName.equals("not")) {

            match("not");

            logicFactor();

            this.code.add("!");

        } else if (tokenName.equals("true")) {

            match("true");

        } else if (tokenName.equals("false")) {

            match("false");

        } else {

            relationalExpression();

        }

    } // method logicFactor

    // relational-expression  ->  expression relational-operator expression |
    //                            expression
    private void relationalExpression() throws Exception {
        expression(); moreRelationalExpression();
    } // method relationalExpression

    private void moreRelationalExpression() throws Exception {
        String operator = this.token.getName();
        if (operator.equals("greater_than")) {

            match("greater_than");

            relationalOperator();
            expression();

            this.code.add(">");

            moreRelationalExpression();

        } else if (operator.equals("greater_equals")) {

            match("greater_equals");

            relationalOperator();
            expression();

            this.code.add(">=");

            moreRelationalExpression();

        } else if (operator.equals("less_than")) {

            match("less_than");

            relationalOperator();
            expression();

            this.code.add("<");

            moreRelationalExpression();

        } else if (operator.equals("less_equals")) {

            match("less_equals");

            relationalOperator();
            expression();

            this.code.add("<=");

            moreRelationalExpression();

        } else if (operator.equals("equals")) {

            match("equals");

            relationalOperator();
            expression();

            this.code.add("==");

            moreRelationalExpression();

        } else if (operator.equals("not_equals")) {

            match("not_equals");

            relationalOperator();
            expression();

            this.code.add("!=");

            moreRelationalExpression();
        }
    }

    // relational-operator    ->  < | <= | > | >= | == | !=
    private String relationalOperator() throws Exception {
        String operator = this.token.getName();

        if (operator.equals("greater_than")) {

            match("greater_than");

        } else if (operator.equals("greater_equals")) {

            match("greater_equals");

        } else if (operator.equals("less_than")) {

            match("less_than");

        } else if(operator.equals("less_equals")) {

            match("less_equals");

        }

        return operator;
    } // method relationalOperator

    // fac[1] = fac[2] // can assign the value of an array
    private void optionalArray(Identifier id) throws Exception {
        if (this.token.getName().equals("open_square_bracket")) {

            match("open_square_bracket");

            expression();

            match("closed_square_bracket");

            // the operator + is used to calculate the address of the index of the array defined by expression
            // the value of expression is the offset added to the base address of the array

            this.code.add("+");

        }
    } // method optionalArray

    // logic-expression       ->  logic-expression || logic-term |
    //                            logic-term
    private void logicExpression() throws Exception {
        logicTerm(); moreLogicExpression();

    } // method logicExpression

    private void moreLogicExpression() throws Exception {
        if (this.token.getName().equals("or")) {
            match("or");

            logicTerm();

            this.code.add("||");

            moreLogicExpression();
        }
    }

    // logic-term             ->  logic-term && logic-factor |
    //                            logic-factor
    private void logicTerm() throws Exception {
        logicFactor(); moreLogicTerm();
    } // method logicTerm

    private void moreLogicTerm() throws Exception {
        if (this.token.getName().equals("and")) {

            match("and");

            logicFactor();

            this.code.add("&&");

            moreLogicTerm();
        }
    } // method moreLogicTerm


    // expression             ->  expression + term |
    //                        expression - term |
    //                        term
    private void expression() throws Exception {
        term(); moreTerms();
    } // method expression


    // term               >  term * factor |
    //                       term / factor |
    //                       term % factor |
    //                       factor

    // term --> factor moreFactor
    // factor --> check id
    private void term() throws Exception {
        factor(); moreFactors();
    } // method term

    private void moreTerms() throws Exception {
        if (this.token.getName().equals("add")) {

            match("add");

            term();

            this.code.add("+");

            moreTerms();

        } else if (this.token.getName().equals("subtract")) {

            match("subtract");

            term();

            this.code.add("-");

            moreTerms();

        }
    } // method moreTerms

    // FIRST(factor) = { ( id num }
    private void factor() throws Exception {
        if (this.token.getName().equals("open_parenthesis")) {

            match("open_parenthesis");

            expression();

            match("closed_parenthesis");

        } else if (this.token.getName().equals("int")) {

            IntegerNumber number = (IntegerNumber) this.token;

            this.code.add("push " + number.getValue());

            match("int");

        } else if (this.token.getName().equals("float")) {
            RealNumber number = (RealNumber) this.token;

            this.code.add("push " + number.getValue());

            match("float");

        } else if (this.token.getName().equals("false")) {
            String bool = this.token.toString();

            this.code.add("push 0");

            match("false");

        } else if (this.token.getName().equals("true")) {
            String bool = this.token.toString();

            this.code.add("push 1");

            match("true");

        } else if (this.token.getName().equals("id")) {

            Identifier id = (Identifier) this.token;

            if (this.symbols.get(id.getLexeme()) == null) {
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is not declared");
            }

            this.code.add("push " + id.getLexeme());

            match("id");

            optionalArray(id);

            this.code.add("load");

        } else {

            throw new Exception("\nError at line " + this.scanner.getLine() + ": invalid arithmetic expression: open parenthesis, int or identifier expected");

        }
    } // method factor

    private void moreFactors() throws Exception {
        if (this.token.getName().equals("multiply")) {

            match("multiply");

            factor();

            this.code.add("*");

            moreFactors();

        } else if (this.token.getName().equals("divide")) {

            match("divide");

            factor();

            this.code.add("/");

            moreFactors();

        } else if (this.token.getName().equals("remainder")) {

            match("remainder");

            factor();

            this.code.add("%");

            moreFactors();

        }
    } // method moreFactors

    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName))
            this.token = this.scanner.getToken();
        else
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " + this.scanner.getLexeme(tokenName) + " expected");
    } // method match

    private String newLabel() {
        return counter++ + "";
    }

} // class SyntaxAnalyzer
