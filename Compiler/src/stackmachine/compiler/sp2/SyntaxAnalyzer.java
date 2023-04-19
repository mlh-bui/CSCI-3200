// 4/17 Given Code for Sprint 1

package stackmachine.compiler.sp2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import slu.compiler.*;

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
    }

    public String compile() throws Exception {
        program();

        // stack machine code

        String code = "";

        for (String instruction : this.code)
            code = code + instruction + "\n";

        return code;
    }

    private void program() throws Exception {
        match("void");
        match("main");
        match("open_curly_bracket");

        declarations();
        instructions();

        match("closed_curly_bracket");
    }

    private void declarations() throws Exception {
        if (this.token.getName().equals("int") || this.token.getName().equals("float") || this.token.getName().equals("boolean")) {
            declaration();
            declarations();
        }
    }

    private void declaration() throws Exception {
        identifiers(type());
        match("semicolon");
    }

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
    }

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
    }

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
    }

    private void optionalDeclaration(String type, Identifier id) throws Exception {
        if (this.token.getName().equals("assignment")) {
            match("assignment");

            // the token 'assignment' allows to assign  an initial value to a variable in the declaration

            this.code.add("push " + id.getLexeme());

            expression();

            this.code.add("store");

        } else if (this.token.getName().equals("open_square_bracket")) { // token declares an array of int, float, or boolean

            match("open_square_bracket");

            int size = 1;

            if(this.token.getName().equals("int")) { // of the size is an int
                IntegerNumber number = (IntegerNumber) this.token;
                size = number.getValue(); // get the value of the token

                this.code.add("array " + id.getLexeme() + " " + type + " " + size); // add code of the array to output of stack
            }

            match("int");

            match("closed_square_bracket");

            this.symbols.put(id.getLexeme(), new ArrayType(type,size)); // put the arrays into the symbol table (since you've declared values)
        }
    }

    private void instructions() throws Exception {
        String tokenName = this.token.getName();

        // check the tokens in FIRST(instruction)

        // this if should contain the FIRST of instruction <-- aka we need all the tokens for declarations
        // since instructions --> declarations
        // so when adding if, while, do-while <-- need in instructions !!
        if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean") || tokenName.equals("id") || tokenName.equals("print")) {
            instruction();
            instructions();
        }
    }

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
        }
    }


    private void assignment() throws Exception {
        Identifier id = (Identifier) this.token;

        // check if variable is declared
        if(this.symbols.get(id.getLexeme()) == null) {
            throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is not declared");
        }

        this.code.add("push " + id.getLexeme());

        match("id");

        optionalArray(id);

        match("assignment");

        expression();

        this.code.add("store");
    }

    // fac[1] = fac[2] // can assign the value of an array
    private void optionalArray(Identifier id) throws Exception {
       if(this.token.getName().equals("open_square_bracket")) {

           match("open_square_bracket");

           expression();

           match("closed_square_bracket");

           this.code.add("+"); // meaning of this plus = adding offset for the array
       }

    } // method optionalArray

    private void expression() throws Exception {
        term(); moreTerms();
    }

    private void term() throws Exception {
        factor(); moreFactors();
    }

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
    }

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

        } else if (this.token.getName().equals("id")) {

            Identifier id = (Identifier) this.token;

            if(this.symbols.get(id.getLexeme()) == null) {
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is not declared");
            }

            this.code.add("push " + id.getLexeme());

            match("id");

            optionalArray(id);

            this.code.add("load");

        } else {

            throw new Exception("\nError at line " + this.scanner.getLine() + ": invalid arithmetic expression: open parenthesis, int or identifier expected");

        }
    }

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
    }

    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName))
            this.token = this.scanner.getToken();
        else
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " + this.scanner.getLexeme(tokenName) + " expected");
    }
}
