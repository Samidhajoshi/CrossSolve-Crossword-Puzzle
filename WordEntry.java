package crossword;

public class WordEntry {
    public String word;
    public String clue;
    public String hint;
    public String theme;

    public WordEntry(String word, String clue, String hint, String theme) {
        this.word = word;
        this.clue = clue;
        this.hint = hint;
        this.theme = theme;
    }

    public WordEntry() {
        this.word = "";
        this.clue = "";
        this.hint = "";
        this.theme = "";
    }
}
