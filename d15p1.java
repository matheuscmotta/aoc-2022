import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A low effort implementation: check each point for an intersection with each diamond.
 */
public class d15p1 {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("input/d15.txt"));
        Cave cave = Cave.caveFactory(lines);

        // Using sample or full data?
        int y;
        if (lines.size() >= 25)
            y = 2000000;
        else
            y = 10;

        // Run the simulation.
        int count = 0;
        for (int i = cave.getMostLeft(); i <= cave.getMostRight(); i++) {
            Point candidateBeacon = new Point(i, y);
            if (!cave.canBeBeacon(candidateBeacon))
                count++;
        }
        System.out.println(count);
    }
}


final class Point {
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Taxicab metric. */
    public int distance(Point to) {
        return Math.abs(x - to.x) + Math.abs(y - to.y);
    }

    public static Point fromList(List<Integer> list) {
        return new Point(list.get(0), list.get(1));
    }

    @Override
    public boolean equals(Object other) {
        // No need to test object reference/null/class for the current use case.
        Point otherPoint = (Point) other;
        return x == otherPoint.x && y == otherPoint.y;
    }
}


/** The circle in taxicab geometry has a diamond/square shape, hence the name. */
class Diamond {
    public final Point sensor;
    public final Point beacon;
    public final int radius;

    public Diamond(Point sensor, Point beacon) {
        this.sensor = sensor;
        this.beacon = beacon;
        // Cache the radius.
        radius = sensor.distance(beacon);
    }
}


final class Cave {
    /** List of diamond-shaped areas formed by each sensor/beacon pairs. */
    private List<Diamond> diamonds = new ArrayList<>(25);

    // Scene boundaries.
    private int mostLeft = Integer.MAX_VALUE;
    private int mostRight = Integer.MIN_VALUE;
    private int deepest = 0;

    public int getMostLeft() {
        return mostLeft;
    }

    public int getMostRight() {
        return mostRight;
    }

    private void updateBoundaries(Point point) {
        if (point.y > deepest) {
            deepest = point.y;
        }
        if (point.x < mostLeft) {
            mostLeft = point.x;
        }
        if (point.x > mostRight) {
            mostRight = point.x;
        }
    }

    /** Update the cave boundaries to contain the diamond formed by the sensor/beacon pair. */
    private void updateBoundaries(Point sensor, Point beacon) {
        int x = sensor.x;
        int y = sensor.y;
        int h = Math.abs(x - beacon.x);
        int v = Math.abs(y - beacon.y);
        // Use the NW and SE points of the rectangle containing the diamond.
        for (int i : new int[] {-1, 1}) {
            Point point = new Point(x + i * h, y + i * v);
            updateBoundaries(point);
        }
    }

    private void put(Point sensor, Point beacon) {
        updateBoundaries(sensor, beacon);
        Diamond diamond = new Diamond(sensor, beacon);
        diamonds.add(diamond);
    }

    public boolean canBeBeacon(Point candidateBeacon) {
        for (Diamond diamond : diamonds) {
            Point sensor = diamond.sensor;
            Point beacon = diamond.beacon;

            if (candidateBeacon.equals(beacon)) {
                return true;
            }

            int radius = diamond.radius;
            int candidateRadius = sensor.distance(candidateBeacon);
            if (candidateRadius <= radius) {
                return false;
            }
        }
        return true;
    }

    public static Cave caveFactory(List<String> lines) {
        Cave cave = new Cave();
        for (String line : lines) {
            String[] parts = line.split("[^-\\d+]");
            List<Integer> coordinates = new ArrayList<>(4);
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].length() > 0)
                    coordinates.add(Integer.parseInt(parts[i]));
            }
            Point sensor = Point.fromList(coordinates.subList(0, 2));
            Point beacon = Point.fromList(coordinates.subList(2, 4));
            cave.put(sensor, beacon);
        }
        return cave;
    }
}
