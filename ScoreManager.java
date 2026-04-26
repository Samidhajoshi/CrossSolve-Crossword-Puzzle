package crossword;

public class ScoreManager {
    public int score = 0;

    public void correct()  { score += 10; }
    public void wrong()    { score -= 5;  }
    public void usedHint() { score -= 3;  }
    public void reset()    { score = 0;   }
}
