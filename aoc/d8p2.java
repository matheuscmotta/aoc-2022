package aoc;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;

public class d8p2 {
    public static void main(String[] args) {
        ArrayList<String> lines = new ArrayList<>(99);
        try (BufferedReader textFile = new BufferedReader(new FileReader("aoc/input/d8.txt"))) {
            String currentLine;
            while (true) {
                currentLine = textFile.readLine();
                if (currentLine == null)
                    break;
                lines.add(currentLine);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        Grid grid = new Grid(lines.size(), lines.get(0).length());
        grid.setFromArrayList(lines);
        System.out.println(grid.maxScenicScore());
    }
}

class Grid {
    private Tree[][] grid;

    public Grid(int rows, int columns) {
        grid = new Tree[rows][columns];
    }

    public Tree get(int x, int y) {
        return grid[x][y];
    }

    public void set(int x, int y, int height) {
        grid[x][y] = new Tree(x, y, height, this);
    }

    public void setFromArrayList(ArrayList<String> lines) {
        for (int i = 0; i < numRows(); i++) {
            String line = lines.get(i);
            for (int j = 0; j < numColumns(); j++) {
                set(i, j, Character.getNumericValue(line.charAt(j)));
            }
        }
    }

    public int numRows() {
        return grid.length;
    }

    public int numColumns() {
        return grid[0].length;
    }

    public int countVisibleTrees() {
        int count = 0;
        for (int i = 0; i < numRows(); i++) {
            for (int j = 0; j < numColumns(); j++) {
                Tree tree = this.get(i, j);
                if (tree.isVisible()) {
                    count += 1;
                }
            }
        }
        return count;
    }

    public boolean containsCoordinates(int x, int y) {
        if (x >= 0 && x < numRows() && y >= 0 && y < numColumns())
            return true;
        return false;
    }

    public int maxScenicScore() {
        int maxScenicScore = 0;
        for (int i = 0; i < numRows(); i++) {
            for (int j = 0; j < numColumns(); j++) {
                Tree tree = this.get(i, j);
                int currentScore = tree.scenicScore();
                if (currentScore > maxScenicScore) {
                    maxScenicScore = currentScore;
                }
            }
        }
        return maxScenicScore;
    }
}

class Tree {
    private int x;
    private int y;
    private int height;
    private Grid grid;

    public Tree(int x, int y, int height, Grid grid) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.grid = grid;
    }

    public boolean isVisible() {
        final int[] direction = { 1, 0 };
        for (int i = 0; i < 4; i++) {
            int[] currentDirection = rotate(direction, i);
            if (this.height > tallestAhead(currentDirection)) {
                return true;
            }
        }
        return false;
    }

    private int tallestAhead(int[] direction) {
        // Would be a good idea to memoize this method.
        int tallest;
        int[] nextInDirection = { x + direction[0], y + direction[1] };
        if (grid.containsCoordinates(nextInDirection[0], nextInDirection[1])) {
            Tree nextTree = grid.get(nextInDirection[0], nextInDirection[1]);
            tallest = Math.max(nextTree.height, nextTree.tallestAhead(direction));
        } else {
            tallest = -1;
        }
        return tallest;
    }

    private static int[] rotate(int[] direction, int n) {
        // Rotate the vector 90 degrees n times clockwise, without mutation.
        int[] rotated = direction.clone();
        for (int i = 0; i < n; i++) {
            int x = rotated[0];
            rotated[0] = rotated[1];
            rotated[1] = -x;
        }
        return rotated;
    }

    public int scenicScore() {
        final int[] direction = { 1, 0 };
        int scenicScore = 1;
        for (int i = 0; i < 4; i++) {
            int[] currentDirection = rotate(direction, i);
            scenicScore *= viewingDistance(currentDirection);
        }
        return scenicScore;
    }

    private int viewingDistance(int[] direction) {
        int distance = 0;
        int[] next = { x + direction[0], y + direction[1] };
        while (grid.containsCoordinates(next[0], next[1])) {
            if (grid.get(next[0], next[1]).height >= height) {
                distance += 1;
                break;
            }
            distance += 1;
            next[0] += direction[0];
            next[1] += direction[1];
        }
        return distance;
    }
}
