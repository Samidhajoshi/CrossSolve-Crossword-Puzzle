package crossword;

public class Grid {
    public int size;
    public char[][] cells;
    public char[][] playerCells;
    public int[][] clueNums;

    public Grid(int s) {
        this.size = s;
        cells = new char[size][size];
        playerCells = new char[size][size];
        clueNums = new int[size][size];
        init();
    }

    public void init() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                cells[r][c] = ' ';
                playerCells[r][c] = ' ';
                clueNums[r][c] = 0;
            }
    }

    public void reset() {
        init();
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }
}
