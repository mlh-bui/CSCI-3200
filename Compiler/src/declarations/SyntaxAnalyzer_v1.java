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
 *
 *     identifiers       ->  id
 *                           { addSymbol(id.lexeme, identifiers.val); more-identifiers.val = identifiers.val }
 *                           more-identifiers
 *
 *     more-identifiers  ->  , id
 *                           { addSymbol(id.lexeme, more-identifiers.val) }
 *                           more-identifiers |
 *                           epsilon
 *
 */

public class SyntaxAnalyzer_v1 implements ISyntaxAnalyzer {
    private IToken token;
    private ILexicalAnalyzer scanner;
    private Map<String, IDataType> symbols;

    public SyntaxAnalyzer_v1(LexicalAnalyzer lex) {
        this.scanner = lex;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
    }

    public String symbolTable() {
        String symbols = "";

        Set<Map.Entry<String, IDataType>> s = this.symbols.entrySet();

        for (Map.Entry<String, IDataType> m : s)
            symbols = symbols + "<'" + m.getKey() + "', " + m.getValue().toString() + "> \n";

        return symbols;
    }

    public void compile() throws Exception {
        program();
    }

    private void program() throws Exception {
        match("void");
        match("main");
        match("open_curly_bracket");

        declarations();

        match("closed_curly_bracket");
    }

    private void declarations() throws Exception {
        String tokenName = this.token.getName();

        if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean")) {
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
        }
        else if (type.equals("float")) {
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
                throw new Exception("\nError at line " + this.scanner.getLine() +
                        ": identifier '" + id.getLexeme() + "' is already declared");

            match("id");

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

            moreIdentifiers(type);
        }
    }

    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName))
            this.token = this.scanner.getToken();
        else
            throw new Exception("\nError at line " + this.scanner.getLine() + ", " + this.scanner.getLexeme(tokenName) + " expected");
    }
}
