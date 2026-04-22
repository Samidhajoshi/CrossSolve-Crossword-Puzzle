package crossword;

import java.io.*;
import java.util.*;

public class Dictionary {
    private List<WordEntry> entries = new ArrayList<>();

    public void loadFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            loadFromReader(br);
        } catch (IOException ex) {
            System.out.println("Error: cannot open " + filename);
        }
    }

    public void loadFromStream(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            loadFromReader(br);
        } catch (IOException ex) {
            System.out.println("Error reading stream: " + ex.getMessage());
        }
    }

    private void loadFromReader(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("\\|", -1);
            if (parts.length < 2) continue;
            WordEntry e = new WordEntry();
            e.word  = parts[0].trim();
            e.clue  = parts[1].trim();
            e.hint  = parts.length > 2 ? parts[2].trim() : "";
            e.theme = parts.length > 3 ? parts[3].trim().toLowerCase() : "general";
            if (e.word.isEmpty() || e.clue.isEmpty()) continue;
            entries.add(e);
        }
    }

    public WordEntry getEntry(String word) {
        for (WordEntry e : entries)
            if (e.word.equalsIgnoreCase(word)) return e;
        return new WordEntry("", "", "", "");
    }

    public List<String> getAllWords() {
        List<String> words = new ArrayList<>();
        for (WordEntry e : entries) words.add(e.word);
        return words;
    }

    public List<String> getWordsByTheme(String theme) {
        if (theme == null || theme.isBlank() || theme.equalsIgnoreCase("general")) {
            return getAllWords();
        }
        List<String> words = new ArrayList<>();
        for (WordEntry e : entries)
            if (e.theme.equalsIgnoreCase(theme)) words.add(e.word);
        return words;
    }

    public List<String> getAvailableThemes() {
        Set<String> themes = new TreeSet<>();
        for (WordEntry e : entries) {
            if (e.theme != null && !e.theme.isBlank() && !e.theme.equalsIgnoreCase("general"))
                themes.add(e.theme.toLowerCase());
        }
        return new ArrayList<>(themes);
    }

    public int size() { return entries.size(); }
}
