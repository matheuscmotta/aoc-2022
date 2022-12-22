package aoc;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class d9p2 {
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

        IterableKnot head = new IterableKnot(0, 0, 10 - 1);
        for (String line : lines) {
            head.moveTo(line);

            // Uncomment the section bellow to inspect the sample data.
            // Grid grid = new Grid(6, 5);
            // for (Knot knot : head) {
            // int[] coordinates = knot.getPoint();
            // grid.add(coordinates[0], coordinates[1], 'x');
            // }
            // System.out.println(grid);
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
    private int[] lastDirection;
    private final static int[] HORIZONTAL = { 1, 0 };
    private final static int[] DIAGONAL = { 1, 1 };
    private static HashMap<String, Integer> directionMap = new HashMap<>(4);

    {
        directionMap.put("R", 0);
        directionMap.put("D", 1);
        directionMap.put("L", 2);
        directionMap.put("U", 3);
    }

    public Knot(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size + 1;
        if (size > 0) {
            knotBehind = new Knot(0, 0, size - 1);
        }
        lastDirection = new int[2];
        history = new LinkedHashSet<>();
        history.add(this.toString());
    }

    public Knot getKnotBehind() {
        return knotBehind;
    }

    public int[] getPoint() {
        return new int[] { x, y };
    }

    public int[] getLastDirection() {
        return lastDirection;
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
            lastDirection = direction;
            history.add(this.toString());
            if (knotBehind != null) {
                knotBehind.follow(this);
            }
        }
    }

    private void follow(Knot head) {
        double distance = Vector.getDistance(this.getPoint(), head.getPoint());
        if (distance > Math.sqrt(2)) {
            int[] direction = getDirection(head);
            move(direction, 1);
        }
    }

    private int[] getDirection(Knot head) throws Error {
        int[] vector = Vector.getVector(this.getPoint(), head.getPoint());
        if (Vector.isParallelToAxis(vector)) {
            return head.getLastDirection();
        }

        for (int i = 0; i < 4; i++) {
            int[] diagonal = Vector.rotate(DIAGONAL, i);
            // Find the appropriate diagonal vector for the movement.
            if (Vector.inSameQuadrant(diagonal, vector)) {
                return diagonal;
            }
        }
        // This path is impossible (?).
        throw new Error("Could not find a direction.");
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
    public IterableKnot(int x, int y, int size) {
        super(x, y, size);
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

        @Override
        public void remove() {
            throw new Error("Something wrong with the iterator...");
        }
    }
}

class Grid {
    private LinkedList<Character>[][] grid;

    public Grid(int rowCount, int columnCount) {
        // This is ok because the add method initializes the correct type during runtime.
        grid = new LinkedList[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                grid[i][j] = new LinkedList<Character>();
            }
        }
    }

    public void add(int x, int y, char tag) {
        grid[x][y].add(tag);
    }

    public Character getFirstAt(int x, int y) {
        return grid[x][y].getFirst();
    }

    public int numRows() {
        return grid.length;
    }

    public int numColumns() {
        return grid[0].length;
    }

    public void fill() {
        for (int i = 0; i < numRows(); i++) {
            for (int j = 0; j < numColumns(); j++) {
                if (grid[i][j].isEmpty()) {
                    add(i,j,'⋅');
                }
            }
        }
    }

    @Override
    public String toString() {
        fill();
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = numColumns() - 1; j >= 0; j--) {
            for (int i = 0; i < numRows(); i++) {
                stringBuilder.append(getFirstAt(i, j)).append(" ");
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

    public static boolean isParallelToAxis(int[] vector) {
        return vector[0] == 0 || vector[1] == 0;
    }

    public static double getDistance(int[] vector1, int[] vector2) {
        // Calculate the distance using the Pythagorean theorem.
        return Math.sqrt(Math.pow(vector2[0] - vector1[0], 2) + Math.pow(vector2[1] - vector1[1], 2));
    }

    public static int[] getVector(int[] point1, int[] point2) {
        return new int[] { point2[0] - point1[0], point2[1] - point1[1] };
    }

    public static boolean inSameQuadrant(int[] vector1, int[] vector2) {
        // Two vectors are in the same quadrant when their corresponding coordinates
        // have the same sign.
        boolean sameX = (vector1[0] > 0 && vector2[0] > 0) || (vector1[0] < 0 && vector2[0] < 0);
        boolean sameY = (vector1[1] > 0 && vector2[1] > 0) || (vector1[1] < 0 && vector2[1] < 0);
        return sameX && sameY;
    }
}
