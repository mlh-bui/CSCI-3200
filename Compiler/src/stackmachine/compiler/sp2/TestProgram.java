package stackmachine.compiler.sp2;

public class TestProgram {

    public static void main(String[] args) {

        try {

            IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();

            stackMachineCompiler.compile("program test array.txt", "svm test array.txt");

            System.out.println("'program test array.txt' compiled successfully!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}