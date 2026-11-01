import java.util.Scanner;

/**
 * SimpleCalculator
 * A console-based calculator that performs addition, subtraction,
 * multiplication, division, and modulus on two numbers.
 */
public class SimpleCalculator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        char again;

        System.out.println("=== Simple Calculator ===");

        do {
            // Get the two numbers
            System.out.print("Enter the first number: ");
            double num1 = scanner.nextDouble();

            System.out.print("Enter the second number: ");
            double num2 = scanner.nextDouble();

            // Get the operation
            System.out.print("Choose an operation (+, -, *, /, %): ");
            char operator = scanner.next().charAt(0);

            // Calculate and display the result
            switch (operator) {
                case '+':
                    System.out.println("Result: " + num1 + " + " + num2 + " = " + (num1 + num2));
                    break;
                case '-':
                    System.out.println("Result: " + num1 + " - " + num2 + " = " + (num1 - num2));
                    break;
                case '*':
                    System.out.println("Result: " + num1 + " * " + num2 + " = " + (num1 * num2));
                    break;
                case '/':
                    if (num2 == 0) {
                        System.out.println("Error: Cannot divide by zero.");
                    } else {
                        System.out.println("Result: " + num1 + " / " + num2 + " = " + (num1 / num2));
                    }
                    break;
                case '%':
                    if (num2 == 0) {
                        System.out.println("Error: Cannot divide by zero.");
                    } else {
                        System.out.println("Result: " + num1 + " % " + num2 + " = " + (num1 % num2));
                    }
                    break;
                default:
                    System.out.println("Error: Invalid operator. Use +, -, *, / or %.");
            }

            // Ask whether to continue
            System.out.print("\nDo you want to calculate again? (y/n): ");
            again = scanner.next().charAt(0);
            System.out.println();

        } while (again == 'y' || again == 'Y');

        System.out.println("Thank you for using Simple Calculator. Goodbye!");
        scanner.close();
    }
}
