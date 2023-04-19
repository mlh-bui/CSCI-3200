package stackmachine.compiler.sp3;

import slu.stackmachine.*;
import stackmachine.compiler.sp2.IStackMachineCompiler;
import stackmachine.compiler.sp2.StackMachineCompiler;

public class TestProgram {

    public static void main(String[] args) {
        try {

            IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();

            // Compile program files ---> output to a new file (make a new name)
            // compare new output file with sm files (correct if matches)

            // AKA 6 test files need to complete
            stackMachineCompiler.compile("program test arrays.txt", "sm test arrays v2.txt");

            System.out.println("'program test arrays.txt' compiled successfully!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    } // method main
} // class TestProgram