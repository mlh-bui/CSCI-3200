package declarations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import slu.compiler.*;

/*
 *  Syntax-directed definition for variable declaration
 *
 *     program           ->  void main { declarations }
 *
 *     declarations      ->  declaration declarations  |
 *                           epsilon
 *
 *     declaration       ->  type { identifiers.val = type.val } identifiers ;
 *
 *     type              ->  int     { type.val = "int"     } |
 *                           float   { type.val = "float"   } |
 *                           boolean { type.val = "boolean" }
 *     // (above) at this point in the tree we know the type and the lexeme of the tree
 *
 *     identifiers       ->  id
 *                           { addSymbol(id.lexeme, identifiers.val); more-identifiers.val = identifiers.val }
 *                           more-identifiers
 *     // (above) at this point we add the variable name (id.lexeme) and type (identifiers.val) to the symbol table
 *     // then we pass the identifiers value to the next function which is more identifiers
 *
 *     more-identifiers  ->  , id
 *                           { addSymbol(id.lexeme, more-identifiers.val) }
 *                           more-identifiers |
 *                           epsilon
 *     // do the same for more-identifiers where id is added to the table with the more-identifiers val
 *
 */

public class SyntaxAnalyzer implements ISyntaxAnalyzer {
    private IToken token;
    private ILexicalAnalyzer scanner;
    private Map<String, IDataType> symbols;

    public SyntaxAnalyzer(LexicalAnalyzer lex) {
        this.scanner = lex;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
    }

    public String symbolTable() {
        String symbols = "";

        Set<Map.Entry<String, IDataType>> s = this.symbols.entrySet();

        // creates tokens for the symbol table
        for (Map.Entry<String, IDataType> m : s)
            symbols = symbols + "<'" + m.getKey() + "', " + m.getValue().toString() + "> \n";

        return symbols;
    }

    // call the function of the start symbol of the grammar aka program
    public void compile() throws Exception {
        program();
    }

    // Following declaration of line 11, call void main then { declaration }
    private void program() throws Exception {
        match("void"); // keywords, terminal symbols
        match("main");
        match("open_curly_bracket");

        declaractions();

        match("closed_curly_bracket");
    }

    // Doing line 13 - 30, implement declaration functions for the rest
    private void declaractions() throws Exception {
        String tokenName = this.token.getName();

        if(tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean")) {
            declaration();
            declaractions();
        }
    }

    private void declaration() throws Exception {
        identifiers(type());            // call type as the input of identifiers, the inherited value
        match("semicolon");   // match the semicolon
    }

    private String type() throws Exception {
        String type = this.token.getName();
        if(type.equals("int")) {
            match("int");   // remember match gets the next token, updates the value
        } else if (type.equals("float")) {
            match("float");
        } else {
            match("boolean");
        }
        return type;
    }

    private void identifiers(String type) throws Exception {
        // implementing the symbol table

        if(this.token.getName().equals("id")) {
            Identifier id = (Identifier) this.token;

            if(this.symbols.get(id.getLexeme()) == null) {
                this.symbols.put(id.getLexeme(), new PrimitiveType(type));
                // add to the symbol table a new primitive type with a string type
            } else {
                throw new Exception("\nError at line " + this.scanner.getLine()
                        + ": Identifier '" + id.getLexeme() + "' is already declared");
            }
            match("id");

            moreIdentifiers(type);
        }
    }

    private void moreIdentifiers(String type) throws Exception {
        if(this.token.getName().equals("comma")) {
            match("comma");

            Identifier id = (Identifier) this.token;
            if(this.symbols.get(id.getLexeme()) == null) {
                this.symbols.put(id.getLexeme(), new PrimitiveType(type));

            } else {
                throw new Exception("\nError at line " + this.scanner.getLine()
                        + ": Identifier '" + id.getLexeme() + "' is already declared");
            }
            match("id");

            moreIdentifiers(type);
        }
    }

    // code of the top-down recursive predictive parser

    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName))
            this.token = this.scanner.getToken();
        else
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " + this.scanner.getLexeme(tokenName) + " expected");
    }
}