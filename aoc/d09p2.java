package aoc;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class d09p2 {
    public static void main(String[] args) {
        ArrayList<String> lines = new ArrayList<>(2000);
        try (BufferedReader textFile = new BufferedReader(new FileReader("aoc/input/d9.txt"))) {
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

        IterableKnot head = new IterableKnot(0, 0, 9);
        for (String line : lines) {
            head.moveTo(line);

            // Comment/Uncomment to inspect the problem sample data.
            // Grid.printState(head, line, 18, 18);
        }
        for (Knot knot : head) {
            System.out.println(" Knot number: " + knot.size() + ", Count: " + knot.count());
        }
    }
}


class Knot {
    private int x;
    private int y;
    private int size;
    private Knot knotBehind = null;
    private LinkedHashSet<String> history;
    private final static int[] HORIZONTAL = {1, 0};
    private static HashMap<String, Integer> directionMap = new HashMap<>(4);

    {
        directionMap.put("R", 0);
        directionMap.put("D", 1);
        directionMap.put("L", 2);
        directionMap.put("U", 3);
    }

    public Knot(int x, int y, int numOfKnotsBehind) {
        this.x = x;
        this.y = y;
        this.size = numOfKnotsBehind + 1;
        if (numOfKnotsBehind > 0) {
            knotBehind = new Knot(0, 0, numOfKnotsBehind - 1);
        }
        history = new LinkedHashSet<>();
        history.add(this.toString());
    }

    public Knot getKnotBehind() {
        return knotBehind;
    }

    public int[] getCoordinates() {
        return new int[] {x, y};
    }

    public void moveTo(String operation) {
        String[] parts = operation.split(" ");
        int[] moveDiretion = Vector.rotate(HORIZONTAL, directionMap.get(parts[0]));
        move(moveDiretion, Integer.parseInt(parts[1]));
    }

    private void move(int[] direction, int steps) {
        for (int i = 0; i < steps; i++) {
            x += direction[0];
            y += direction[1];
            history.add(this.toString());
            if (knotBehind != null && isDisconnected()) {
                knotBehind.follow(this);
            }
        }
    }

    private boolean isDisconnected() {
        int[] vector = Vector.getVector(this.getCoordinates(), knotBehind.getCoordinates());
        // Instead of the Pythagorean theorem, it is simpler to just check if the vector
        // formed by the two knots has either of its coordinates greater than 1.
        if (Math.abs(vector[0]) > 1 || Math.abs(vector[1]) > 1) {
            return true;
        }
        return false;
    }

    private void follow(Knot head) {
        int[] direction = getDirection(head);
        move(direction, 1);
    }

    private int[] getDirection(Knot head) {
        // Form a vector representing the segment from `this` to `head`, then
        // get the closest vector which have either 0 or 1 as its coordinates.
        int[] vector = Vector.getVector(this.getCoordinates(), head.getCoordinates());
        return Vector.closestWithCoordinates1Or0(vector);
    }

    public int count() {
        return history.size();
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}


class IterableKnot extends Knot implements Iterable<Knot> {
    public IterableKnot(int x, int y, int numOfKnotsBehind) {
        super(x, y, numOfKnotsBehind);
    }

    @Override
    public Iterator<Knot> iterator() {
        return new KnotIterator();
    }

    private class KnotIterator implements Iterator<Knot> {
        private Knot currentKnot = IterableKnot.this;

        @Override
        public boolean hasNext() {
            return currentKnot != null;
        }

        @Override
        public Knot next() {
            Knot knot = currentKnot;
            currentKnot = currentKnot.getKnotBehind();
            return knot;
        }
    }
}


class Grid {
    private char[][] grid;
    private int xOffset;
    private int yOffset;

    public Grid(int rows, int columns) {
        this.xOffset = rows;
        this.yOffset = columns;
        // A translation of axes is used to support knots with negative coordinates.
        grid = new char[rows * 2][columns * 2];

        // Fill the grid with dots (using the dot product symbol).
        for (int i = 0; i < numRows(); i++) {
            for (int j = 0; j < numColumns(); j++) {
                grid[i][j] = '⋅';
            }
        }
    }

    public void add(int x, int y, char tag) {
        // Translation of axes.
        grid[x + xOffset][y + yOffset] = tag;
        // Keep the center marked.
        grid[xOffset][yOffset] = 's';
    }

    public int numRows() {
        return grid.length;
    }

    public int numColumns() {
        return grid[0].length;
    }

    public static void printState(IterableKnot head, String line, int xOffset, int yOffset) {
        Grid grid = new Grid(xOffset, yOffset);
        for (Knot knot : head) {
            int[] coordinates = knot.getCoordinates();
            grid.add(coordinates[0], coordinates[1], 'x');
        }
        System.out.println("== " + line + " ==");
        System.out.println(grid);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = numColumns() - 1; j >= 0; j--) {
            for (int i = 0; i < numRows(); i++) {
                stringBuilder.append(grid[i][j]);
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}


class Vector {
    public static int[] rotate(int[] direction, int n) {
        // This is the same method from the day 8 puzzle.
        int[] rotated = direction.clone();
        for (int i = 0; i < n; i++) {
            int x = rotated[0];
            rotated[0] = rotated[1];
            rotated[1] = -x;
        }
        return rotated;
    }

    public static int[] getVector(int[] point1, int[] point2) {
        return new int[] {point2[0] - point1[0], point2[1] - point1[1]};
    }

    public static int[] closestWithCoordinates1Or0(int[] vector) {
        int[] direction = new int[2];
        direction[0] = (int) Math.signum(vector[0]);
        direction[1] = (int) Math.signum(vector[1]);
        return direction;
    }
}
