import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * AverageCalculator - Smart Score Analyzer
 *
 * A menu-driven program that lets the user:
 *  - add, view, edit and delete named scores (each with credit units)
 *  - see statistics: simple average, weighted average, GPA (5.0 scale),
 *    median, highest, lowest, standard deviation and pass rate
 *  - view a bar chart of all scores with the average marked
 *  - use a Target Planner: "What score do I need next to reach my target average?"
 *  - sort results, load sample data, or clear everything
 *
 * Grading scale: A 70-100, B 60-69, C 50-59, D 45-49, E 40-44, F below 40.
 */
public class AverageCalculator {

    private static final double PASS_MARK = 40;
    private static final Scanner scanner = new Scanner(System.in);
    private static final ArrayList<Entry> entries = new ArrayList<>();

    // ---------- One score entry ----------
    private static class Entry {
        String name;
        double score;
        int units;

        Entry(String name, double score, int units) {
            this.name = name;
            this.score = score;
            this.units = units;
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
            int choice = readInt("Choose an option (0-9): ", 0, 9);
            System.out.println();

            switch (choice) {
                case 1:
                    addEntry();
                    break;
                case 2:
                    viewEntries();
                    break;
                case 3:
                    editEntry();
                    break;
                case 4:
                    deleteEntry();
                    break;
                case 5:
                    showStatistics();
                    break;
                case 6:
                    showBarChart();
                    break;
                case 7:
                    targetPlanner();
                    break;
                case 8:
                    loadSampleData();
                    break;
                case 9:
                    clearAll();
                    break;
                case 0:
                    running = false;
                    break;
            }
        }

        System.out.println("\nThanks for using Smart Score Analyzer. Keep learning!");
        scanner.close();
    }

    // =====================================================
    //                      MENU ACTIONS
    // =====================================================

    private static void addEntry() {
        System.out.println("--- ADD SCORE ---");
        String name = readText("Subject / item name: ");

        if (findByName(name) != -1) {
            System.out.println("'" + name + "' already exists. Use option 3 to edit it.");
            return;
        }

        double score = readDouble("Score (0-100): ", 0, 100);
        int units = readInt("Credit units / weight (1-10): ", 1, 10);
        entries.add(new Entry(name, score, units));

        System.out.printf("%nSaved: %s -> %.1f (%s, %d unit%s)%n",
                name, score, grade(score), units, units == 1 ? "" : "s");
        System.out.println("Entries so far: " + entries.size());
    }

    private static void viewEntries() {
        System.out.println("--- ALL SCORES ---");
        if (isEmpty()) return;

        System.out.println("Sort by:  1) Order entered   2) Highest first   3) Lowest first   4) Name A-Z");
        int sortChoice = readInt("Choice (1-4): ", 1, 4);

        ArrayList<Entry> view = new ArrayList<>(entries);
        if (sortChoice == 2) {
            view.sort(Comparator.comparingDouble((Entry e) -> e.score).reversed());
        } else if (sortChoice == 3) {
            view.sort(Comparator.comparingDouble((Entry e) -> e.score));
        } else if (sortChoice == 4) {
            view.sort(Comparator.comparing((Entry e) -> e.name.toLowerCase()));
        }

        System.out.println();
        System.out.printf("%-4s %-20s %8s %7s %6s%n", "No.", "Subject", "Score", "Grade", "Units");
        System.out.println(line('-', 50));
        for (int i = 0; i < view.size(); i++) {
            Entry e = view.get(i);
            System.out.printf("%-4d %-20s %8.1f %7s %6d%n",
                    i + 1, shorten(e.name, 20), e.score, grade(e.score), e.units);
        }
        System.out.println(line('-', 50));
        System.out.printf("Total entries: %d   |   Average: %.2f%n", view.size(), simpleAverage());
    }

    private static void editEntry() {
        System.out.println("--- EDIT SCORE ---");
        if (isEmpty()) return;

        listShort();
        int index = readInt("Number of entry to edit: ", 1, entries.size()) - 1;
        Entry e = entries.get(index);

        System.out.printf("Editing %s (current score %.1f, %d units)%n", e.name, e.score, e.units);
        e.score = readDouble("New score (0-100): ", 0, 100);
        e.units = readInt("New credit units (1-10): ", 1, 10);
        System.out.printf("%nUpdated: %s -> %.1f (%s)%n", e.name, e.score, grade(e.score));
    }

    private static void deleteEntry() {
        System.out.println("--- DELETE SCORE ---");
        if (isEmpty()) return;

        listShort();
        int index = readInt("Number of entry to delete: ", 1, entries.size()) - 1;
        Entry removed = entries.get(index);

        if (confirm("Delete '" + removed.name + "'? (y/n): ")) {
            entries.remove(index);
            System.out.println("Deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    private static void showStatistics() {
        System.out.println("--- STATISTICS ---");
        if (isEmpty()) return;

        double sum = 0;
        double weightedSum = 0;
        double pointsSum = 0;
        int totalUnits = 0;
        int passed = 0;
        Entry high = entries.get(0);
        Entry low = entries.get(0);
        ArrayList<Double> values = new ArrayList<>();

        for (Entry e : entries) {
            sum += e.score;
            weightedSum += e.score * e.units;
            pointsSum += points(e.score) * e.units;
            totalUnits += e.units;
            if (e.score >= PASS_MARK) passed++;
            if (e.score > high.score) high = e;
            if (e.score < low.score) low = e;
            values.add(e.score);
        }

        int n = entries.size();
        double average = sum / n;
        double weightedAverage = weightedSum / totalUnits;
        double gpa = pointsSum / totalUnits;

        // Median
        Collections.sort(values);
        double median = (n % 2 == 1)
                ? values.get(n / 2)
                : (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0;

        // Standard deviation
        double squareSum = 0;
        for (double v : values) {
            squareSum += (v - average) * (v - average);
        }
        double stdDev = Math.sqrt(squareSum / n);

        System.out.println(line('=', 46));
        System.out.printf("%-24s %s%n", "Entries:", n);
        System.out.printf("%-24s %s%n", "Total credit units:", totalUnits);
        System.out.printf("%-24s %.1f%n", "Sum of scores:", sum);
        System.out.println(line('-', 46));
        System.out.printf("%-24s %.2f  (%s)%n", "Simple average:", average, grade(average));
        System.out.printf("%-24s %.2f  (%s)%n", "Weighted average:", weightedAverage, grade(weightedAverage));
        System.out.printf("%-24s %.2f / 5.00%n", "GPA:", gpa);
        System.out.printf("%-24s %.1f%n", "Median:", median);
        System.out.println(line('-', 46));
        System.out.printf("%-24s %s (%.1f)%n", "Highest:", high.name, high.score);
        System.out.printf("%-24s %s (%.1f)%n", "Lowest:", low.name, low.score);
        System.out.printf("%-24s %.2f  [%s]%n", "Std deviation:", stdDev, consistency(stdDev));
        System.out.printf("%-24s %d of %d (%.0f%%)%n", "Passed (>= 40):", passed, n, 100.0 * passed / n);
        System.out.println(line('-', 46));
        System.out.println("Remark: " + remark(weightedAverage));
        System.out.println(line('=', 46));
    }

    private static void showBarChart() {
        System.out.println("--- BAR CHART (each # = 2 marks) ---");
        if (isEmpty()) return;

        for (Entry e : entries) {
            System.out.printf("%-14s |%-50s| %5.1f%n",
                    shorten(e.name, 14), "#".repeat((int) Math.round(e.score / 2)), e.score);
        }

        double average = simpleAverage();
        System.out.println(line('-', 74));
        System.out.printf("%-14s |%-50s| %5.1f%n",
                "AVERAGE", "=".repeat((int) Math.round(average / 2)), average);
    }

    private static void targetPlanner() {
        System.out.println("--- TARGET PLANNER ---");
        System.out.println("Find out what you need in your NEXT subject to hit a target average.");
        if (isEmpty()) return;

        double weightedSum = 0;
        int totalUnits = 0;
        for (Entry e : entries) {
            weightedSum += e.score * e.units;
            totalUnits += e.units;
        }
        double currentAverage = weightedSum / totalUnits;
        System.out.printf("Your current weighted average: %.2f%n%n", currentAverage);

        double target = readDouble("Target weighted average (0-100): ", 0, 100);
        int nextUnits = readInt("Credit units of the next subject (1-10): ", 1, 10);

        double needed = (target * (totalUnits + nextUnits) - weightedSum) / nextUnits;
        double bestPossible = (weightedSum + 100.0 * nextUnits) / (totalUnits + nextUnits);

        System.out.println();
        if (needed <= 0) {
            System.out.println("Great news! You will stay above " + target + " even with a score of 0.");
        } else if (needed > 100) {
            System.out.printf("Not reachable in one subject. Even a perfect 100 only lifts you to %.2f.%n",
                    bestPossible);
            System.out.println("Try adding more subjects or choose a lower target.");
        } else {
            System.out.printf("You need at least %.1f (%s) in the next subject to reach %.1f.%n",
                    needed, grade(needed), target);
        }
    }

    private static void loadSampleData() {
        System.out.println("--- LOAD SAMPLE DATA ---");
        if (!entries.isEmpty() && !confirm("This will replace your current entries. Continue? (y/n): ")) {
            System.out.println("Cancelled.");
            return;
        }
        entries.clear();
        entries.add(new Entry("Mathematics", 78, 4));
        entries.add(new Entry("English", 65, 3));
        entries.add(new Entry("Physics", 54, 3));
        entries.add(new Entry("Programming", 88, 4));
        entries.add(new Entry("Chemistry", 42, 2));
        System.out.println("Loaded 5 sample entries. Try options 2, 5, 6 and 7!");
    }

    private static void clearAll() {
        System.out.println("--- CLEAR ALL ---");
        if (isEmpty()) return;
        if (confirm("Delete all " + entries.size() + " entries? (y/n): ")) {
            entries.clear();
            System.out.println("All entries cleared.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    // =====================================================
    //                    CALCULATION HELPERS
    // =====================================================

    private static double simpleAverage() {
        if (entries.isEmpty()) return 0;
        double sum = 0;
        for (Entry e : entries) {
            sum += e.score;
        }
        return sum / entries.size();
    }

    private static String grade(double score) {
        if (score >= 70) return "A";
        if (score >= 60) return "B";
        if (score >= 50) return "C";
        if (score >= 45) return "D";
        if (score >= 40) return "E";
        return "F";
    }

    private static int points(double score) {
        if (score >= 70) return 5;
        if (score >= 60) return 4;
        if (score >= 50) return 3;
        if (score >= 45) return 2;
        if (score >= 40) return 1;
        return 0;
    }

    private static String remark(double average) {
        if (average >= 70) return "Excellent work! You are at the top of your game.";
        if (average >= 60) return "Very good. A little push will take you to the top.";
        if (average >= 50) return "Good, but you can aim higher.";
        if (average >= 40) return "Fair. More practice is needed.";
        return "Needs improvement. Don't give up, review and try again!";
    }

    private static String consistency(double stdDev) {
        if (stdDev < 5) return "Very consistent";
        if (stdDev < 10) return "Fairly consistent";
        return "Varies a lot";
    }

    private static int findByName(String name) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    // =====================================================
    //                        DISPLAY
    // =====================================================

    private static void printBanner() {
        System.out.println(line('=', 46));
        System.out.println("        SMART SCORE ANALYZER");
        System.out.println("   Averages, GPA, charts & target planner");
        System.out.println(line('=', 46));
        System.out.println("Tip: choose option 8 to load sample data and explore.");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println(line('-', 46));
        System.out.printf("  MAIN MENU   [Entries: %d | Average: %.2f]%n", entries.size(), simpleAverage());
        System.out.println(line('-', 46));
        System.out.println("  1. Add score");
        System.out.println("  2. View all scores (with sorting)");
        System.out.println("  3. Edit a score");
        System.out.println("  4. Delete a score");
        System.out.println("  5. Statistics (average, GPA, median...)");
        System.out.println("  6. Bar chart");
        System.out.println("  7. Target planner");
        System.out.println("  8. Load sample data");
        System.out.println("  9. Clear all");
        System.out.println("  0. Exit");
        System.out.println(line('-', 46));
    }

    private static void listShort() {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            System.out.printf("  %d. %s (%.1f)%n", i + 1, e.name, e.score);
        }
    }

    private static boolean isEmpty() {
        if (entries.isEmpty()) {
            System.out.println("No scores yet. Use option 1 to add some, or option 8 for sample data.");
            return true;
        }
        return false;
    }

    // =====================================================
    //                   INPUT HELPER METHODS
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
                System.out.println("Please enter a whole number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid whole number. Try again.");
            }
        }
    }

    private static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid number. Try again.");
            }
        }
    }

    private static boolean confirm(String prompt) {
        System.out.print(prompt);
        String answer = scanner.nextLine().trim();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    // =====================================================
    //                  FORMATTING HELPERS
    // =====================================================

    private static String line(char ch, int length) {
        return String.valueOf(ch).repeat(length);
    }

    private static String shorten(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 1) + ".";
    }
}
