import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class d14p1 {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("input/d14.txt"));
        Cave cave = new Cave();
        cave.scan(lines);

        int count = 0;
        outer: while (true) {
            Point currentSand = cave.newSand();
            while (cave.moveSand(currentSand)) {
                if (currentSand.getY() > cave.getDeepestRock())
                    break outer;
            }
            count += 1;
        }
        System.out.println(cave);
        System.out.println(count);
    }
}


class Point {
    private int x;
    private int y;
    private boolean isRock;

    public Point(int x, int y, boolean isRock) {
        this.x = x;
        this.y = y;
        this.isRock = isRock;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isRock() {
        return isRock;
    }

    public void translate(int[] direction) {
        x = x + direction[0];
        y = y + direction[1];
    }

    public static Point createSand() {
        return new Point(500, 0, false);
    }

    /** Format: x,y */
    public static Point fromString(String point) {
        String[] parts = point.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        return new Point(x, y, true);
    }

    @Override
    public String toString() {
        return "(" + getX() + "," + getY() + ")";
    }
}


class Cave {
    private HashMap<String, Point> cave = new HashMap<>();
    private static final int[][] directions = {{0, 1}, {-1, 1}, {1, 1}};
    private int mostLeft = 500;
    private int mostRight = 500;
    private int deepestRock = 0;

    private void addPoint(Point point) {
        cave.put(point.toString(), point);
        // Dynamically update the cave boundaries.
        updateBoundaries(point);
    }

    private void updateBoundaries(Point point) {
        if (point.isRock()) {
            if (point.getY() > deepestRock) {
                deepestRock = point.getY();
            }
            if (point.getX() < mostLeft) {
                mostLeft = point.getX();
            }
            if (point.getX() > mostRight) {
                mostRight = point.getX();
            }
        }
    }

    public int getDeepestRock() {
        return deepestRock;
    }

    private Point getPoint(String coordinates) {
        return cave.get(coordinates);
    }

    /** Format: (x,y) */
    private boolean cellIsEmpty(String point) {
        return !cave.containsKey(point);
    }

    private static String getStringFmt(int x, int y) {
        return "(" + x + "," + y + ")";
    }

    public Point newSand() {
        Point point = Point.createSand();
        addPoint(point);
        return point;
    }

    public boolean moveSand(Point sand) {
        for (int i = 0; i < 3; i++) {
            int dx = sand.getX() + directions[i][0];
            int dy = sand.getY() + directions[i][1];
            String nextPos = getStringFmt(dx, dy);
            if (cellIsEmpty(nextPos)) {
                // Remove from the list before mutation, to avoid key issues.
                cave.remove(sand.toString());
                sand.translate(directions[i]);
                addPoint(sand);
                return true;
            }
        }
        return false;
    }

    public void scan(List<String> lines) {
        for (String line : lines) {
            String[] points = line.split(" -> ");
            for (int i = 0; i < points.length - 1; i++) {
                String headString = points[i + 1];
                String tailString = points[i];
                Point head = Point.fromString(headString);
                Point tail = Point.fromString(tailString);
                makeSegment(head, tail);
            }
        }
    }

    private Point makeSegment(Point head, Point tail) {
        addPoint(head);
        addPoint(tail);
        int dx = head.getX() - tail.getX();
        int dy = head.getY() - tail.getY();
        int taxicabDistance = Math.abs(dx) + Math.abs(dy);
        // If points are within the taxicab unit "circle", return.
        if (taxicabDistance <= 1)
            return tail;

        // Direction vectors.
        int i = (int) Math.signum(dx);
        int j = (int) Math.signum(dy);
        Point newTail = new Point(tail.getX() + i, tail.getY() + j, true);
        addPoint(newTail);
        return makeSegment(head, newTail);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j <= deepestRock; j++) {
            for (int i = mostLeft; i <= mostRight; i++) {
                if (i == 500 && j == 0) {
                    stringBuilder.append("+");
                    continue;
                }
                String currentCell = getStringFmt(i, j);
                if (!cellIsEmpty(currentCell)) {
                    Point point = getPoint(currentCell);
                    stringBuilder.append(point.isRock() ? "#" : "o");
                } else {
                    stringBuilder.append(".");
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
