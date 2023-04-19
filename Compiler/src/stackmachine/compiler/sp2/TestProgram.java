package stackmachine.compiler.sp2;

public class TestProgram {

    public static void main(String[] args) {

        try {

            IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();

            stackMachineCompiler.compile("program test arrays.txt", "sm test arrays.txt");

            System.out.println("'program test arrays.txt' compiled successfully!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}