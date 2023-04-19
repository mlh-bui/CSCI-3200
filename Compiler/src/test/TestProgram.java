package test;

import slu.stackmachine.*;
public class TestProgram {

    public static void main(String[] args) {

        try {

            IStackMachine stackMachine = new StackMachine();

            System.out.print("sm factorial(10) is ");

            stackMachine.run("sm factorial 10.txt");

            System.out.print("sm factorial(10) using an array is ");

            stackMachine.run("sm factorial 10 array.txt");

            System.out.print("sm fibonacci (20) is ");

            stackMachine.run("sm fibonacci 20.txt");

            System.out.print("sm fibonacci (20) using an array is ");

            stackMachine.run("sm fibonacci 20 array.txt");

            System.out.print("sm srqt(2) using Newton is ");

            stackMachine.run("sm Newton sqrt.txt");

            System.out.print("sm Newton sqrt 2");

            stackMachine.run("sm Newton sqrt 2.txt");

            System.out.print("sm test arrays.txt ");

            stackMachine.run("sm test arrays.txt");


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}