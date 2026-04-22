package crossword;

import java.util.*;

public class WordPlacer {
    public List<PlacedWord> placed = new ArrayList<>();

    public boolean solve(Grid grid, List<String> words) {
        placed.clear();
        // Sort descending by length - same as C++ sort
        words.sort((a, b) -> b.length() - a.length());

        for (int start = 0; start < words.size(); start++) {
            placed.clear();
            for (int r = 0; r < grid.size; r++)
                for (int c = 0; c < grid.size; c++)
                    grid.cells[r][c] = ' ';

            int mid = grid.size / 2;
            int startCol = mid - words.get(start).length() / 2;
            if (startCol < 0) continue;

            placeWord(grid, words.get(start), mid, startCol, 'A');
            placed.add(new PlacedWord(words.get(start), mid, startCol, 'A'));

            List<String> remaining = new ArrayList<>(words);
            remaining.remove(start);

            if (backtrack(grid, remaining, 0)) {
                System.out.println("All " + placed.size() + " words placed successfully.");
                return true;
            }
        }
        System.out.println("Placed " + placed.size() + "/" + words.size() + " words.");
        return !placed.isEmpty();
    }

    private boolean backtrack(Grid grid, List<String> words, int index) {
        if (index == words.size()) return true;
        String word = words.get(index);

        for (PlacedWord pw : placed) {
            for (int wi = 0; wi < word.length(); wi++) {
                for (int pi = 0; pi < pw.word.length(); pi++) {
                    if (word.charAt(wi) != pw.word.charAt(pi)) continue;
                    int r, c;
                    char dir;
                    if (pw.dir == 'A') {
                        dir = 'D';
                        r = pw.row - wi;
                        c = pw.col + pi;
                    } else {
                        dir = 'A';
                        r = pw.row + pi;
                        c = pw.col - wi;
                    }
                    if (r < 0 || c < 0) continue;
                    if (canPlace(grid, word, r, c, dir)) {
                        placeWord(grid, word, r, c, dir);
                        placed.add(new PlacedWord(word, r, c, dir));
                        if (backtrack(grid, words, index + 1)) return true;
                        placed.remove(placed.size() - 1);
                        removeWord(grid, word, r, c, dir);
                    }
                }
            }
        }
        return backtrack(grid, words, index + 1);
    }

    private boolean canPlace(Grid grid, String word, int r, int c, char dir) {
        int dr = dir == 'D' ? 1 : 0;
        int dc = dir == 'A' ? 1 : 0;
        int len = word.length();

        if (!grid.inBounds(r + dr * (len - 1), c + dc * (len - 1))) return false;
        if (r < 0 || c < 0) return false;

        if (grid.inBounds(r - dr, c - dc) && grid.cells[r - dr][c - dc] != ' ') return false;
        if (grid.inBounds(r + dr * len, c + dc * len) && grid.cells[r + dr * len][c + dc * len] != ' ') return false;

        boolean hasIntersection = false;
        for (int i = 0; i < len; i++) {
            int nr = r + dr * i;
            int nc = c + dc * i;
            char cell = grid.cells[nr][nc];
            if (cell != ' ') {
                if (cell != word.charAt(i)) return false;
                hasIntersection = true;
            } else {
                int adjR1 = nr + dc, adjC1 = nc + dr;
                int adjR2 = nr - dc, adjC2 = nc - dr;
                if (grid.inBounds(adjR1, adjC1) && grid.cells[adjR1][adjC1] != ' ') return false;
                if (grid.inBounds(adjR2, adjC2) && grid.cells[adjR2][adjC2] != ' ') return false;
            }
        }
        if (!placed.isEmpty() && !hasIntersection) return false;
        return true;
    }

    public void placeWord(Grid grid, String word, int r, int c, char dir) {
        int dr = dir == 'D' ? 1 : 0;
        int dc = dir == 'A' ? 1 : 0;
        for (int i = 0; i < word.length(); i++)
            grid.cells[r + dr * i][c + dc * i] = word.charAt(i);
    }

    private void removeWord(Grid grid, String word, int r, int c, char dir) {
        int dr = dir == 'D' ? 1 : 0;
        int dc = dir == 'A' ? 1 : 0;
        for (int i = 0; i < word.length(); i++) {
            int nr = r + dr * i;
            int nc = c + dc * i;
            boolean shared = false;
            for (PlacedWord pw : placed) {
                if (pw.word.equals(word) && pw.row == r && pw.col == c && pw.dir == dir) continue;
                int pdr = pw.dir == 'D' ? 1 : 0;
                int pdc = pw.dir == 'A' ? 1 : 0;
                for (int j = 0; j < pw.word.length(); j++) {
                    if (pw.row + pdr * j == nr && pw.col + pdc * j == nc) {
                        shared = true; break;
                    }
                }
                if (shared) break;
            }
            if (!shared) grid.cells[nr][nc] = ' ';
        }
    }
}
