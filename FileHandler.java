package crossword;

import java.io.*;

public class FileHandler {

    public boolean save(Grid grid, ScoreManager sm, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println(grid.size);
            pw.println(sm.score);
            for (int r = 0; r < grid.size; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < grid.size; c++) sb.append(grid.cells[r][c]);
                pw.println(sb.toString());
            }
            for (int r = 0; r < grid.size; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < grid.size; c++) sb.append(grid.playerCells[r][c]);
                pw.println(sb.toString());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean load(Grid grid, ScoreManager sm, String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            int size  = Integer.parseInt(br.readLine().trim());
            int score = Integer.parseInt(br.readLine().trim());

            // Re-init grid to new size
            grid.size = size;
            grid.cells       = new char[size][size];
            grid.playerCells = new char[size][size];
            grid.clueNums    = new int[size][size];
            grid.init();

            sm.score = score;

            for (int r = 0; r < size; r++) {
                String row = br.readLine();
                if (row == null) break;
                for (int c = 0; c < size && c < row.length(); c++)
                    grid.cells[r][c] = row.charAt(c);
            }
            for (int r = 0; r < size; r++) {
                String row = br.readLine();
                if (row == null) break;
                for (int c = 0; c < size && c < row.length(); c++)
                    grid.playerCells[r][c] = row.charAt(c);
            }
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }
}
