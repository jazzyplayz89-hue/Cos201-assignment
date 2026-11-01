import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * ItemCost - Smart Shopping Cart & Receipt Generator
 *
 * An interactive console program that lets the user:
 *  - add items (name, unit price, quantity)
 *  - view the cart with live totals
 *  - change quantities or remove items
 *  - apply a promo code for a discount
 *  - check out, pay, get change and print a receipt (with 7.5% VAT)
 *
 * Valid promo codes: STUDENT10 (10% off), SAVE500 (NGN 500 off), JAVA20 (20% off)
 */
public class ItemCost {

    // ---------- Settings ----------
    private static final double VAT_RATE = 0.075;   // 7.5% VAT
    private static final String CURRENCY = "NGN ";
    private static final String STORE_NAME = "JAVA MART";

    // ---------- Program state ----------
    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<Item> cart = new ArrayList<>();
    private static String promoCode = "";
    private static double discountPercent = 0;
    private static double discountFlat = 0;

    // ---------- A single item in the cart ----------
    private static class Item {
        String name;
        double price;
        int quantity;

        Item(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        double lineTotal() {
            return price * quantity;
        }
    }

    // =====================================================
    //                        MAIN
    // =====================================================
    public static void main(String[] args) {
        printBanner();
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Choose an option (1-7): ", 1, 7);
            System.out.println();

            switch (choice) {
                case 1:
                    addItem();
                    break;
                case 2:
                    viewCart();
                    break;
                case 3:
                    changeQuantity();
                    break;
                case 4:
                    removeItem();
                    break;
                case 5:
                    applyPromo();
                    break;
                case 6:
                    checkout();
                    break;
                case 7:
                    running = !confirmExit();
                    break;
            }
        }

        System.out.println("\nThank you for shopping at " + STORE_NAME + ". Goodbye!");
        scanner.close();
    }

    // =====================================================
    //                      MENU ACTIONS
    // =====================================================

    private static void addItem() {
        System.out.println("--- ADD ITEM ---");
        String name = readText("Item name: ");
        double price = readPositiveDouble("Unit price (" + CURRENCY.trim() + "): ");
        int qty = readInt("Quantity (1-1000): ", 1, 1000);

        // If the item already exists, just increase its quantity
        for (Item item : cart) {
            if (item.name.equalsIgnoreCase(name) && item.price == price) {
                item.quantity += qty;
                System.out.println("\n'" + item.name + "' already in cart. Quantity updated to "
                        + item.quantity + ".");
                System.out.println("Line cost: " + money(item.lineTotal()));
                return;
            }
        }

        Item newItem = new Item(name, price, qty);
        cart.add(newItem);
        System.out.println("\nAdded: " + qty + " x " + name + " @ " + money(price));
        System.out.println("Line cost: " + money(newItem.lineTotal()));
        System.out.println("Cart subtotal now: " + money(getSubtotal()));
    }

    private static void viewCart() {
        System.out.println("--- YOUR CART ---");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty. Add some items first.");
            return;
        }

        System.out.printf("%-4s %-20s %5s %14s %16s%n", "No.", "Item", "Qty", "Unit Price", "Line Total");
        System.out.println(line('-', 63));
        for (int i = 0; i < cart.size(); i++) {
            Item it = cart.get(i);
            System.out.printf("%-4d %-20s %5d %14s %16s%n",
                    i + 1, shorten(it.name, 20), it.quantity,
                    money(it.price), money(it.lineTotal()));
        }
        System.out.println(line('-', 63));
        printTotals();
    }

    private static void changeQuantity() {
        System.out.println("--- CHANGE QUANTITY ---");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        listCartShort();
        int index = readInt("Item number to change: ", 1, cart.size()) - 1;
        int qty = readInt("New quantity (1-1000): ", 1, 1000);
        cart.get(index).quantity = qty;
        System.out.println("\nUpdated: " + cart.get(index).name + " x " + qty
                + " = " + money(cart.get(index).lineTotal()));
    }

    private static void removeItem() {
        System.out.println("--- REMOVE ITEM ---");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        listCartShort();
        int index = readInt("Item number to remove: ", 1, cart.size()) - 1;
        Item removed = cart.remove(index);
        System.out.println("\nRemoved '" + removed.name + "' from your cart.");
    }

    private static void applyPromo() {
        System.out.println("--- PROMO CODE ---");
        if (!promoCode.isEmpty()) {
            System.out.println("Promo '" + promoCode + "' is already applied.");
            return;
        }
        String code = readText("Enter promo code: ").toUpperCase();

        switch (code) {
            case "STUDENT10":
                discountPercent = 10;
                break;
            case "JAVA20":
                discountPercent = 20;
                break;
            case "SAVE500":
                discountFlat = 500;
                break;
            default:
                System.out.println("Sorry, '" + code + "' is not a valid promo code.");
                return;
        }
        promoCode = code;
        System.out.println("Promo code '" + code + "' applied successfully!");
    }

    private static void checkout() {
        System.out.println("--- CHECKOUT ---");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty. Nothing to check out.");
            return;
        }

        double total = getGrandTotal();
        System.out.println("Amount due: " + money(total));

        // Keep asking until the customer pays enough (or cancels with 0)
        double paid;
        while (true) {
            paid = readNonNegativeDouble("Enter amount paid (0 to cancel): ");
            if (paid == 0) {
                System.out.println("Checkout cancelled. Your cart is kept.");
                return;
            }
            if (paid < total) {
                System.out.println("Not enough! You still owe " + money(total - paid) + ".");
            } else {
                break;
            }
        }

        printReceipt(paid);

        // Reset everything for the next customer
        cart.clear();
        promoCode = "";
        discountPercent = 0;
        discountFlat = 0;
    }

    private static boolean confirmExit() {
        if (!cart.isEmpty()) {
            System.out.print("You still have items in your cart. Exit anyway? (y/n): ");
            String answer = scanner.nextLine().trim();
            return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
        }
        return true;
    }

    // =====================================================
    //                    CALCULATIONS
    // =====================================================

    private static double getSubtotal() {
        double sum = 0;
        for (Item item : cart) {
            sum += item.lineTotal();
        }
        return sum;
    }

    private static double getDiscount() {
        double subtotal = getSubtotal();
        double discount = (subtotal * discountPercent / 100.0) + discountFlat;
        return Math.min(discount, subtotal); // discount can never exceed the subtotal
    }

    private static double getVat() {
        return (getSubtotal() - getDiscount()) * VAT_RATE;
    }

    private static double getGrandTotal() {
        return getSubtotal() - getDiscount() + getVat();
    }

    private static int getTotalUnits() {
        int units = 0;
        for (Item item : cart) {
            units += item.quantity;
        }
        return units;
    }

    // =====================================================
    //                       DISPLAY
    // =====================================================

    private static void printBanner() {
        System.out.println(line('=', 44));
        System.out.println("      WELCOME TO " + STORE_NAME);
        System.out.println("   Smart Shopping Cart & Receipt Maker");
        System.out.println(line('=', 44));
        System.out.println("Tip: try promo codes STUDENT10, JAVA20 or SAVE500");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println(line('-', 44));
        System.out.println("  MAIN MENU   [Cart: " + getTotalUnits() + " item(s) | "
                + money(getGrandTotal()) + "]");
        System.out.println(line('-', 44));
        System.out.println("  1. Add item");
        System.out.println("  2. View cart");
        System.out.println("  3. Change quantity");
        System.out.println("  4. Remove item");
        System.out.println("  5. Apply promo code");
        System.out.println("  6. Checkout");
        System.out.println("  7. Exit");
        System.out.println(line('-', 44));
    }

    private static void listCartShort() {
        for (int i = 0; i < cart.size(); i++) {
            Item it = cart.get(i);
            System.out.println("  " + (i + 1) + ". " + it.name + " (x" + it.quantity + ")");
        }
    }

    private static void printTotals() {
        System.out.printf("%-30s %32s%n", "Subtotal:", money(getSubtotal()));
        if (getDiscount() > 0) {
            System.out.printf("%-30s %32s%n", "Discount (" + promoCode + "):", "-" + money(getDiscount()));
        }
        System.out.printf("%-30s %32s%n", "VAT (7.5%):", money(getVat()));
        System.out.println(line('=', 63));
        System.out.printf("%-30s %32s%n", "TOTAL TO PAY:", money(getGrandTotal()));
    }

    private static void printReceipt(double paid) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));
        int receiptNo = 1000 + (int) (Math.random() * 9000);

        System.out.println();
        System.out.println(line('=', 63));
        System.out.println(center(STORE_NAME, 63));
        System.out.println(center("OFFICIAL RECEIPT", 63));
        System.out.println(line('=', 63));
        System.out.println("Receipt No : #" + receiptNo);
        System.out.println("Date/Time  : " + time);
        System.out.println(line('-', 63));
        System.out.printf("%-22s %5s %14s %16s%n", "Item", "Qty", "Price", "Total");
        System.out.println(line('-', 63));
        for (Item it : cart) {
            System.out.printf("%-22s %5d %14s %16s%n",
                    shorten(it.name, 22), it.quantity, money(it.price), money(it.lineTotal()));
        }
        System.out.println(line('-', 63));
        printTotals();
        System.out.println(line('-', 63));
        System.out.printf("%-30s %32s%n", "Amount paid:", money(paid));
        System.out.printf("%-30s %32s%n", "Change:", money(paid - getGrandTotal()));
        System.out.println(line('=', 63));
        System.out.println(center("Thank you for shopping with us!", 63));
        System.out.println(line('=', 63));
    }

    // =====================================================
    //                  INPUT HELPER METHODS
    // =====================================================

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("Input cannot be empty. Try again.");
        }
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid whole number. Try again.");
            }
        }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim().replace(",", ""));
                if (value > 0) {
                    return value;
                }
                System.out.println("The value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid amount. Try again.");
            }
        }
    }

    private static double readNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim().replace(",", ""));
                if (value >= 0) {
                    return value;
                }
                System.out.println("The value cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid amount. Try again.");
            }
        }
    }

    // =====================================================
    //                  FORMATTING HELPERS
    // =====================================================

    private static String money(double amount) {
        return CURRENCY + String.format("%,.2f", amount);
    }

    private static String line(char ch, int length) {
        return String.valueOf(ch).repeat(length);
    }

    private static String shorten(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 1) + ".";
    }

    private static String center(String text, int width) {
        int padding = Math.max(0, (width - text.length()) / 2);
        return " ".repeat(padding) + text;
    }
}
