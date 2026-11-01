import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * SimpleCalculator - Smart Calculator with Memory & History
 *
 * Features:
 *  - Basic operations: add, subtract, multiply, divide, modulus
 *  - Extras: power, square root, percentage-of, factorial
 *  - "ans" keyword reuses the last result as a number (chain calculations)
 *  - Memory keys: M+ (add), M- (subtract), MR (recall), MC (clear)
 *  - Shortcuts "pi" and "e" can be typed in place of a number
 *  - Calculation history you can view or clear
 *  - You can type menu numbers OR symbols (+, -, *, /, %, ^)
 */
public class SimpleCalculator {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<String> history = new ArrayList<>();
    private static double lastResult = 0;
    private static double memory = 0;

    // =====================================================
    //                        MAIN
    // =====================================================
    public static void main(String[] args) {
        printBanner();
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toUpperCase();
            System.out.println();

            switch (command) {
                case "1":
                case "+":
                    binaryOperation('+');
                    break;
                case "2":
                case "-":
                    binaryOperation('-');
                    break;
                case "3":
                case "*":
                case "X":
                    binaryOperation('*');
                    break;
                case "4":
                case "/":
                    binaryOperation('/');
                    break;
                case "5":
                case "%":
                    binaryOperation('%');
                    break;
                case "6":
                case "^":
                    binaryOperation('^');
                    break;
                case "7":
                    squareRoot();
                    break;
                case "8":
                    percentageOf();
                    break;
                case "9":
                    factorial();
                    break;
                case "M+":
                    memory += lastResult;
                    System.out.println("Added " + format(lastResult) + " to memory. Memory = " + format(memory));
                    break;
                case "M-":
                    memory -= lastResult;
                    System.out.println("Subtracted " + format(lastResult) + " from memory. Memory = " + format(memory));
                    break;
                case "MR":
                    System.out.println("Memory recall: " + format(memory));
                    System.out.println("(Type 'm' instead of a number to use it in a calculation.)");
                    break;
                case "MC":
                    memory = 0;
                    System.out.println("Memory cleared.");
                    break;
                case "H":
                    showHistory();
                    break;
                case "CH":
                    history.clear();
                    System.out.println("History cleared.");
                    break;
                case "Q":
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Unknown command '" + command + "'. Pick one from the menu.");
            }
        }

        System.out.println("\nYou made " + history.size() + " calculation(s) this session. Goodbye!");
        scanner.close();
    }

    // =====================================================
    //                      OPERATIONS
    // =====================================================

    private static void binaryOperation(char op) {
        System.out.println("--- " + operationName(op) + " ---");
        double a = readNumber("First number : ");
        double b = readNumber("Second number: ");
        double result;

        switch (op) {
            case '+':
                result = a + b;
                break;
            case '-':
                result = a - b;
                break;
            case '*':
                result = a * b;
                break;
            case '/':
                if (b == 0) {
                    System.out.println("Error: Cannot divide by zero.");
                    return;
                }
                result = a / b;
                break;
            case '%':
                if (b == 0) {
                    System.out.println("Error: Cannot find remainder when dividing by zero.");
                    return;
                }
                result = a % b;
                break;
            default: // '^'
                result = Math.pow(a, b);
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    System.out.println("Error: That power cannot be calculated.");
                    return;
                }
                break;
        }

        showResult(format(a) + " " + op + " " + format(b), result);
    }

    private static void squareRoot() {
        System.out.println("--- SQUARE ROOT ---");
        double a = readNumber("Number: ");
        if (a < 0) {
            System.out.println("Error: Cannot take the square root of a negative number.");
            return;
        }
        showResult("sqrt(" + format(a) + ")", Math.sqrt(a));
    }

    private static void percentageOf() {
        System.out.println("--- PERCENTAGE OF ---");
        double percent = readNumber("Percentage (e.g. 15 for 15%): ");
        double total = readNumber("Of what number?            : ");
        showResult(format(percent) + "% of " + format(total), percent / 100.0 * total);
    }

    private static void factorial() {
        System.out.println("--- FACTORIAL ---");
        double a = readNumber("Whole number (0-20): ");
        if (a < 0 || a != Math.floor(a) || a > 20) {
            System.out.println("Error: Enter a whole number from 0 to 20.");
            return;
        }
        long result = 1;
        for (int i = 2; i <= (int) a; i++) {
            result *= i;
        }
        showResult(format(a) + "!", (double) result);
    }

    // =====================================================
    //                   RESULT & HISTORY
    // =====================================================

    private static void showResult(String expression, double result) {
        lastResult = result;
        String record = expression + " = " + format(result);
        history.add(record);

        System.out.println();
        System.out.println("  " + record);
        System.out.println("  (Type 'ans' as a number to reuse " + format(result) + ")");
    }

    private static void showHistory() {
        System.out.println("--- HISTORY ---");
        if (history.isEmpty()) {
            System.out.println("No calculations yet.");
            return;
        }
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("%3d. %s%n", i + 1, history.get(i));
        }
        System.out.println("Total: " + history.size() + " calculation(s).");
    }

    // =====================================================
    //                        DISPLAY
    // =====================================================

    private static void printBanner() {
        System.out.println(line('=', 48));
        System.out.println("            SMART CALCULATOR");
        System.out.println("      Memory | History | Chain with 'ans'");
        System.out.println(line('=', 48));
    }

    private static void printMenu() {
        System.out.println();
        System.out.println(line('-', 48));
        System.out.println("  ans: " + format(lastResult) + "   |   memory: " + format(memory));
        System.out.println(line('-', 48));
        System.out.println("  1. Add (+)           6. Power (^)");
        System.out.println("  2. Subtract (-)      7. Square root");
        System.out.println("  3. Multiply (*)      8. Percentage of");
        System.out.println("  4. Divide (/)        9. Factorial");
        System.out.println("  5. Modulus (%)");
        System.out.println("  M+ M- MR MC : memory   H : history");
        System.out.println("  CH : clear history     Q : quit");
        System.out.println(line('-', 48));
    }

    // =====================================================
    //                    INPUT & FORMATTING
    // =====================================================

    /** Reads a number. Also accepts: ans, m (memory), pi, e. */
    private static double readNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim().toLowerCase().replace(",", "");

            switch (text) {
                case "ans":
                    return lastResult;
                case "m":
                    return memory;
                case "pi":
                    return Math.PI;
                case "e":
                    return Math.E;
                default:
                    break;
            }

            if (text.matches("[-+]?(\\d+\\.?\\d*|\\.\\d+)([eE][-+]?\\d+)?")) {
                try {
                    double value = Double.parseDouble(text);
                    if (!Double.isInfinite(value)) {
                        return value;
                    }
                } catch (NumberFormatException ignored) {
                    // fall through to the error message below
                }
            }
            System.out.println("Invalid input. Enter a number, or: ans, m, pi, e");
        }
    }

    /** Shows whole numbers without ".0" and trims useless decimals. */
    private static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return String.valueOf((long) value);
        }
        return new BigDecimal(value).setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    private static String operationName(char op) {
        switch (op) {
            case '+': return "ADDITION";
            case '-': return "SUBTRACTION";
            case '*': return "MULTIPLICATION";
            case '/': return "DIVISION";
            case '%': return "MODULUS (REMAINDER)";
            default:  return "POWER";
        }
    }

    private static String line(char ch, int length) {
        return String.valueOf(ch).repeat(length);
    }
}
