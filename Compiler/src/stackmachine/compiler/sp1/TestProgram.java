package stackmachine.compiler.sp1;

public class TestProgram {

    public static void main(String[] args) {

        try {

            IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();

            stackMachineCompiler.compile("program test assignment.txt", "svm test assignment.txt");

            System.out.println("'program test assignment.txt' compiled successfully!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}