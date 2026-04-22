package crossword;

public class AnswerValidator {

    public boolean check(String answer, String correct) {
        if (answer.length() != correct.length()) return false;
        for (int i = 0; i < answer.length(); i++)
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
