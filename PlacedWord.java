package crossword;

public class PlacedWord {
    public String word;
    public int row;
    public int col;
    public char dir; // 'A' for across, 'D' for down

    public PlacedWord(String word, int row, int col, char dir) {
        this.word = word;
        this.row = row;
        this.col = col;
        this.dir = dir;
    }
}
