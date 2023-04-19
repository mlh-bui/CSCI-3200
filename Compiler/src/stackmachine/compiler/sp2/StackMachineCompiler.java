package stackmachine.compiler.sp2;

import slu.compiler.LexicalAnalyzer;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class StackMachineCompiler implements IStackMachineCompiler {
    private ISyntaxAnalyzer parser;

    @Override
    public void compile(String program, String fileName) throws Exception {
        String code;

        try {

            this.parser = new SyntaxAnalyzer( new LexicalAnalyzer(program, StandardCharsets.UTF_8) );

            code = this.parser.compile();

            PrintWriter outputFile = new PrintWriter(fileName);

            outputFile.print(code);
            outputFile.close();

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    } // method compile

}