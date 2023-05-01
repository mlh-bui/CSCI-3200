package stackmachine.compiler.sp3;

import slu.stackmachine.*;

public class TestProgram {

    public static void main(String[] args) {
        try {

            IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();

            String bin = "program binary search.txt";
            String o_bin = "v2 sm binary search.txt";

            String bin_bool = "program binary search boolean.txt";
            String o_bin_bool = "v2 sm binary search boolean.txt";

            String fac10 = "program factorial 10.txt";
            String o_fac10 = "v2 sm factorial 10.txt";

            String fac10_array = "program factorial 10 array.txt";
            String o_fac10_array = "v2 sm factorial 10 array.txt";

            String fib20 = "program fibonacci 20.txt";
            String o_fib20 = "v2 sm fibonacci 20.txt";

            String fib20_array = "program fibonacci 20 array.txt";
            String o_fib20_array = "v2 sm fibonacci 20 array.txt";

            String newton = "program Newton sqrt.txt";
            String o_newton = "v2 sm Newton sqrt.txt";

            String array = "program test arrays.txt";
            String o_array = "v2 sm test arrays.txt";

            String assign = "program test assignment.txt";
            String o_assign = "v2 sm test assignment.txt";


            stackMachineCompiler.compile(bin, o_bin);
            stackMachineCompiler.compile(bin_bool,o_bin_bool);
            stackMachineCompiler.compile(fac10,o_fac10);
            stackMachineCompiler.compile(fac10_array,o_fac10_array);
            stackMachineCompiler.compile(fib20, o_fib20);
            stackMachineCompiler.compile(fib20_array,o_fib20_array);
            stackMachineCompiler.compile(newton,o_newton);
            stackMachineCompiler.compile(array,o_array);
            stackMachineCompiler.compile(assign,o_assign);

            System.out.printf("'%s' compiled successfully!\n", o_bin);
            System.out.printf("'%s' compiled successfully!\n", o_bin_bool);
            System.out.printf("'%s' compiled successfully!\n", o_fac10);
            System.out.printf("'%s' compiled successfully!\n", o_fac10_array);
            System.out.printf("'%s' compiled successfully!\n", o_fib20);
            System.out.printf("'%s' compiled successfully!\n", o_fib20_array);
            System.out.printf("'%s' compiled successfully!\n", o_newton);
            System.out.printf("'%s' compiled successfully!\n", o_array);
            System.out.printf("'%s' compiled successfully!\n", o_assign);


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    } // method main
} // class TestProgram