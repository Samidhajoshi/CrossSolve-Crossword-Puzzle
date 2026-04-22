package crossword;

import java.util.Scanner;

public class UserInput {

    private final Scanner scanner = new Scanner(System.in);

    public int chooseGridSize() {
        System.out.println("\n=Choose Grid Size=");
        System.out.println("1. Small  (10 x 10)");
        System.out.println("2. Medium (15 x 15)");
        System.out.println("3. Large  (21 x 21)");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                return 10;
            case 3:
                return 21;
            case 2:
            default:
                System.out.println("Using default size (15 x 15).");
                return 15;
        }
    }

    public int getClueNumber() {
        System.out.print("\nEnter clue number: ");
        return scanner.nextInt();
    }

    public char getDirection() {
        System.out.print("Direction (Across/Down): ");
        String input = scanner.next().trim().toLowerCase();

        if (input.startsWith("a")) {
            return 'A';
        } else if (input.startsWith("d")) {
            return 'D';
        } else {
            System.out.println("Invalid input, defaulting to Across.");
            return 'A';
        }
    }

    public String getAnswer() {
        System.out.print("Your answer: ");
        String answer = scanner.next().trim();

        return answer.toUpperCase();
    }

    public int getMenuChoice() {
        System.out.println("\n= MENU =");
        System.out.println("1. Answer a clue");
        System.out.println("2. Get a hint");
        System.out.println("3. Show solution");
        System.out.println("4. Quit");
        System.out.print("Choose an option: ");

        return scanner.nextInt();
    }
}
