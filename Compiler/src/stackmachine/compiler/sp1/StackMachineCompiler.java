package stackmachine.compiler.sp1;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import slu.compiler.*;

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
    }

}