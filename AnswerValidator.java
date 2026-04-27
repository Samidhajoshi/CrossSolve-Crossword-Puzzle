package crossword;

public class AnswerValidator {

    private static final int BASE = 31;
    private static final int MOD = 1_000_000_007;

   // Rabin-Karp based answer check.
    public boolean check(String answer, String correct) {
        if (answer == null || correct == null) return false;
        if (answer.length() != correct.length()) return false;

        int n = answer.length();
        long hashAnswer = 0;
        long hashCorrect = 0;
        long power = 1;

        for (int i = 0; i < n; i++) {
            int a = Character.toUpperCase(answer.charAt(i)) - 'A' + 1;
            int b = Character.toUpperCase(correct.charAt(i)) - 'A' + 1;

            hashAnswer = (hashAnswer + (long) a * power) % MOD;
            hashCorrect = (hashCorrect + (long) b * power) % MOD;

            power = (power * BASE) % MOD;
        }

        // Hashes don't match  - definitely wrong
        if (hashAnswer != hashCorrect) return false;

        // Hashes match - verify char-by-char to rule out collision
        for (int i = 0; i < n; i++)
            if (Character.toUpperCase(answer.charAt(i)) != Character.toUpperCase(correct.charAt(i)))
                return false;

        return true;
    }

    public boolean isComplete(Grid grid) {
        for (int r = 0; r < grid.size; r++)
            for (int c = 0; c < grid.size; c++)
                if (grid.cells[r][c] != ' ' && grid.playerCells[r][c] == ' ')
                    return false;
        return true;
    }
}
