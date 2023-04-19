package stackmachine.compiler.sp1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import slu.compiler.*;

/*
 *  Syntax-directed definition for data type declaration
 *
 *     program              ->  void main { declarations instructions }
 *
 *     declarations         ->  declaration declarations  |
 *                              epsilon
 *
 *     declaration          ->  type { identifiers.type = type.value } identifiers ;
 *
 *     type                 ->  int     { type.value = "int"     } |
 *                              float   { type.value = "float"   } |
 *                              boolean { type.value = "boolean" }
 *
 *     identifiers          ->  id
 *                              { addSymbol(id.lexeme, identifiers.type); optional-declaration.id = identifiers.id; more-identifiers.type = identifiers.type }
 *                              optional-declaration
 *                              more-identifiers
 *
 *     more-identifiers     ->  , id
 *                              { addSymbol(id.lexeme, identifiers.type); optional-declaration.id = identifiers.id; more-identifiers.type = identifiers.type }
 *                              optional-declaration
 *                              more-identifiers |
 *                              epsilon
 *
 *     optional-declaration ->  = { print("push " + id.lexeme) } expression { print("store") } |
 *                              epsilon
 *
 *  Syntax-directed definition to translate infix arithmetic expressions into a stack machine code
 *
 *     instructions         ->  instruction instructions |
 *                              epsilon
 *
 *     instruction          ->  declaration |
 *                              assignment ;
 *
 *     assignment           -> id { print("push " + id.lexeme) } expression { print("store") }
 *
 *     expression           -> expression + term { print("+") } |
 *                             expression - term { print("-") }
 *                             term
 *
 *     term                 -> term * factor { print("*") } |
 *                             term / factor { print("/") } |
 *                             term % factor { print("%") } |
 *                             factor
 *
 *     factor               -> (expression) |
 *                              id  { print("push " + id.lexeme) } |
 *                              num { print("push " + num.value) }
 *
 *  Right-recursive SDD for a top-down recursive predictive parser
 *
 *     instruction          -> id { print("push " + id.lexeme) } expression { print("store") }
 *
 *     expression           -> term moreTerms
 *
 *     moreTerms            -> + term { print("+") } moreTerms |
 *                             - term { print("-") } moreTerms |
 *                             epsilon
 *
 *     term                 -> factor moreFactors
 *
 *     moreFactors          -> * factor { print("*") } moreFactors |
 *                             / factor { print("/") } moreFactors |
 *                             % factor { print("%") } moreFactors |
 *                             epsilon
 *
 *     factor               -> (expression) |
 *                             id  { print("push " + id.lexeme) } ; print("load") |
 *                             num { print("push " + num.value) }
 *
 */

public class SyntaxAnalyzer implements ISyntaxAnalyzer {
    private IToken token;
    private ILexicalAnalyzer scanner;
    private Map<String, IDataType> symbols;
    private List<String> code;

    public SyntaxAnalyzer(ILexicalAnalyzer lex) {
        this.scanner = lex;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
        this.code = new ArrayList<String>();
    } // constructor SyntaxAnalyzer

    // calls the function for the start symbol
    public String compile() throws Exception {
        program();

        // stack machine code (to be printed in the output file)

        String code = "";

        for (String instruction : this.code)
            code = code + instruction + "\n";

        return code;
    } // method compile

    // SDD for program -> void main { declarations instructions }
    private void program() throws Exception {
        match("void"); // match the keywords & terminal symbols
                                 // void ie needs to be seen first bc it's a keyword
        match("main");
        match("open_curly_bracket");

        declarations(); // declarations and instructions are 
        instructions();

        match("closed_curly_bracket");
    } // method program

    // SDD for declarations ->  declaration declarations  |  epsilon
    private void declarations() throws Exception {
        // checks if the next token is a type (to start the declaration)
        // little confused, why call recursively?
        if (this.token.getName().equals("int") || this.token.getName().equals("float") || this.token.getName().equals("boolean")) {
            declaration();
            declarations();
        }
    }

    // SDD for  declaration ->  type { identifiers.type = type.value } identifiers ;
    private void declaration() throws Exception {
        identifiers(type()); // declares the type of variable and puts it into the symbol table
        match("semicolon");
    }

    private String type() throws Exception {
        String type = this.token.getName();

        // declaration of the types which are attributes?
        if (type.equals("int")) {
            match("int");
        } else if (type.equals("float")) {
            match("float");
        } else if (type.equals("boolean")) {
            match("boolean");
        }

        return type;
    } // method type

    // identifiers  -> id
    //                    { addSymbol(id.lexeme, identifiers.type);
    //                       optional-declaration.id = identifiers.id;
    //                       more-identifiers.type = identifiers.type }
    //                 optional-declaration
    //                 more-identifiers
    private void identifiers(String type) throws Exception {
        if (this.token.getName().equals("id")) {
            Identifier id = (Identifier) this.token;

            if (this.symbols.get(id.getLexeme()) == null) // believe to be a part of compiler
                this.symbols.put(id.getLexeme(), new PrimitiveType(type));
            else
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");

            match("id"); // match terminal symbol

            optionalDeclaration(type, id);  // call optional declaration where we assign a value to id
                                            // note: parameters = the id and it's type so "int a" part

            moreIdentifiers(type);          // more identifiers for same type
                                            // ex: int a ", b, c" <-- moreIdentifiers prt
        }
    } // method identifiers

    // " , id, id" part
    // more-identifiers     ->  , id
    //      { addSymbol(id.lexeme, identifiers.type); optional-declaration.id = identifiers.id; more-identifiers.type = identifiers.type
    //      optional-declaration
    //      more-identifiers |
    //      epsilon
    private void moreIdentifiers(String type) throws Exception {
        if (this.token.getName().equals("comma")) {
            match("comma"); // match the comma

            Identifier id = (Identifier) this.token; // create the identifier (the variable)

            if (this.symbols.get(id.getLexeme()) == null)
                this.symbols.put(id.getLexeme(), new PrimitiveType(type));
            else
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");

            match("id");    // after id is created match it

            optionalDeclaration(type, id);

            moreIdentifiers(type);
        }
    } // method moreIdentifiers

    // optional-declaration ->  = { print("push " + id.lexeme) } expression { print("store") } |
    //                             epsilon
    private void optionalDeclaration(String type, Identifier id) throws Exception {
        if (this.token.getName().equals("assignment")) {
            match("assignment"); // create a token to assign a value to the id in parameter

            // the token 'assignment' allows to assign  an initial value to a variable in the declaration

            this.code.add("push " + id.getLexeme()); // part of stack machine step

            expression();

            this.code.add("store");
        }
    } // method optionalDeclaration

    /** Parts to translate infix arithmetic expressions into stack machine code */
    // instructions         ->  instruction instructions |
    //                          epsilon
    private void instructions() throws Exception {
        String tokenName = this.token.getName();

        // check the tokens in FIRST(instruction)

        if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean") || tokenName.equals("id")) {
            instruction();
            instructions();
        }
    } // method instructions

    private void instruction() throws Exception {
        String tokenName = this.token.getName();

        // check the tokens in FIRST(instruction)

        if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean")) {

            declaration();

        } else if (tokenName.equals("id")) {

            assignment();
            match("semicolon");

        }
    } // method instruction

    // assignment  -> id { print("push " + id.lexeme) } expression { print("store") }
    private void assignment() throws Exception {
        Identifier id = (Identifier) this.token; // create identifier for current token

        this.code.add("push " + id.getLexeme());    // push id

        match("assignment");    // match the assignment to allow a value for the id

        expression();

        this.code.add("store");     // store token & it's value
    } // method assignment

    private void expression() throws Exception {
        term(); moreTerms();
    }

    private void term() throws Exception {
        factor(); moreFactors();
    }

    // Arithmetic operations for stack
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


    // factor   -> (expression) |
    //              id  { print("push " + id.lexeme) } ; print("load") |
    //              num { print("push " + num.value) }
    private void factor() throws Exception {
        if (this.token.getName().equals("open_parenthesis")) {      // match non-terminal

            match("open_parenthesis");

            expression();       // call expression

            match("closed_parenthesis");    // match non-terminal

        } else if (this.token.getName().equals("int")) {  // only numbers accepted as tokens = positive integers

            IntegerNumber number = (IntegerNumber) this.token;

            this.code.add("push " + number.getValue());

            match("int");

        } else if (this.token.getName().equals("id")) {

            Identifier id = (Identifier) this.token; // create token for id

            this.code.add("push " + id.getLexeme());    // push the id

            match("id");

            this.code.add("load");      // load it onto the stack

        } else {

            throw new Exception("\nError at line " + this.scanner.getLine() + ": invalid arithmetic expression: open parenthesis, int or identifier expected");

        }
    } // method factor

    // moreFactors   ->  factor { print("*") } moreFactors |
    //                   / factor { print("/") } moreFactors |
    //                   % factor { print("%") } moreFactors |
    //                   epsilon
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

} // class SyntaxAnalyzer

    /*
    private void optionalArray() throws Exception {
        if (this.token.getName().equals("id")) {

            Identifier id = (Identifier) this.token;

            if (this.symbols.get(id.getLexeme()) == null) {
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is not declared");
            }

            match("id");

            if (this.token.getName().equals("open_square_bracket")) {

                match("open_square_bracket");

                expression();

                match("closed_square_bracket");

                this.code.add("+");

            }

        } else {

            throw new Exception("\nError at line " + this.scanner.getLine() + ": a variable is expected");

        }
    }

     */