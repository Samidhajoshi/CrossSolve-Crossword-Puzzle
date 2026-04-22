package crossword;

import java.util.*;

public class ClueAssigner {
    public List<ClueEntry> acrossClues = new ArrayList<>();
    public List<ClueEntry> downClues   = new ArrayList<>();

    public void assign(Grid grid, List<PlacedWord> placed, Dictionary dict) {
        acrossClues.clear();
        downClues.clear();
        int num = 1;
        int size = grid.size;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid.cells[r][c] == ' ') continue;

                boolean startsA = (c == 0 || grid.cells[r][c - 1] == ' ')
                        && (c + 1 < size && grid.cells[r][c + 1] != ' ');
                boolean startsD = (r == 0 || grid.cells[r - 1][c] == ' ')
                        && (r + 1 < size && grid.cells[r + 1][c] != ' ');

                if (!startsA && !startsD) continue;
                grid.clueNums[r][c] = num;

                for (PlacedWord pw : placed) {
                    if (pw.row != r || pw.col != c) continue;
                    WordEntry we = dict.getEntry(pw.word);
                    ClueEntry ce = new ClueEntry();
                    ce.number = num;
                    ce.word   = pw.word;
                    ce.clue   = we.clue.isEmpty()  ? "No clue" : we.clue;
                    ce.hint   = we.hint.isEmpty()  ? "No hint" : we.hint;
                    ce.row    = r;
                    ce.col    = c;
                    ce.dir    = pw.dir;
                    ce.length = pw.word.length();
                    if (pw.dir == 'A') acrossClues.add(ce);
                    else               downClues.add(ce);
                }
                num++;
            }
        }
    }

    public String getWord(int num, char dir) {
        List<ClueEntry> list = (dir == 'A') ? acrossClues : downClues;
        for (ClueEntry e : list)
            if (e.number == num) return e.word;
        return "";
    }

    public String getClue(int num, char dir) {
        List<ClueEntry> list = (dir == 'A') ? acrossClues : downClues;
        for (ClueEntry e : list)
            if (e.number == num) return e.clue;
        return "";
    }

    public String getHint(int num, char dir) {
        List<ClueEntry> list = (dir == 'A') ? acrossClues : downClues;
        for (ClueEntry e : list)
            if (e.number == num) return e.hint;
        return "";
    }
}
