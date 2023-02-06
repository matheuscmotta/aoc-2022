import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/** First time trying Java concurrency. The fork-join framework looks appropriate here. */
public class d15p2v2 {
    public static void main(String[] args)
            throws IOException, InterruptedException, ExecutionException {
        List<String> lines = Files.readAllLines(Path.of("input/d15.txt"));
        Cave cave = Cave.caveFactory(lines);

        // Using sample or full data?
        int y;
        if (lines.size() >= 25)
            y = 4000000;
        else
            y = 20;

        Point distress = cave.scan(y);
        if (distress.x >= 0 && distress.y >= 0) {
            // Cast to long and use methods that throw on overflow.
            long result = Math.addExact(Math.multiplyExact(4000000L, distress.x), distress.y);
            System.out.println(result);
        } else {
            System.out.println("Not found.");
        }
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
}


/** The circle in taxicab geometry has a diamond/square shape, hence the name. */
final class Diamond {
    public final Point sensor;
    public final Point beacon;
    public final Point nw;
    public final Point se;
    public final int radius;

    public Diamond(Point sensor, Point beacon) {
        this.sensor = sensor;
        this.beacon = beacon;
        // Cache the radius.
        radius = sensor.distance(beacon);

        // NW and SE vertices of the rectangle containing the diamond.
        nw = new Point(sensor.x - radius, sensor.y - radius);
        se = new Point(sensor.x + radius, sensor.y + radius);
    }
}


final class Segment {
    public final int left;
    public final int right;

    public Segment(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public static List<Segment> findGaps(List<Segment> segments, int width) {
        List<Segment> gaps = new ArrayList<>();
        // Sort by the left edge.
        segments.sort(Comparator.comparing(segment -> segment.left));
        int currentRight = 0;
        for (Segment segment : segments) {
            if (segment.left > currentRight) {
                gaps.add(new Segment(currentRight, segment.left - 1));
            }
            currentRight = Math.max(currentRight, segment.right + 1);
        }
        if (currentRight <= width) {
            gaps.add(new Segment(currentRight, width));
        }
        return gaps;
    }
}


final class Cave {
    /** List of diamond-shaped areas formed by each sensor/beacon pairs. */
    private List<Diamond> diamonds = new ArrayList<>(25);
    private final Point SENTINEL = new Point(-1, -1);
    /** Used to prevent starting new threads if the result has already been found. */
    private boolean isFound = false;

    private synchronized void requestStop() {
        isFound = true;
    }

    private synchronized boolean isStopRequested() {
        return isFound;
    }

    public Point scan(int width) throws InterruptedException, ExecutionException {
        RecursiveScan task = new RecursiveScan(0, width, width);
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(task);
    }

    public Point scan(int start, int end, int width) {
        // Start from the top line towards the bottom.
        Point result = SENTINEL;
        for (int y = start; y <= end; y++) {
            List<Segment> segments = new ArrayList<>();
            // Compute the intersections of the line with each diamond.
            for (Diamond diamond : diamonds) {
                if (diamond.nw.y <= y && diamond.se.y >= y) {
                    int y0y1 = Math.abs(y - diamond.sensor.y);
                    int x1 = diamond.sensor.x;
                    int left = -diamond.radius + y0y1 + x1;
                    int right = diamond.radius - y0y1 + x1;
                    segments.add(new Segment(left, right));
                }
            }
            List<Segment> gaps = Segment.findGaps(segments, width);
            if (gaps.size() == 1) {
                Segment segment = gaps.get(0);
                result = new Point(segment.left, y);
                // According to the puzzle statement, the position is unique in the range.
                break;
            }
        }
        return result;
    }

    private void put(Point sensor, Point beacon) {
        Diamond diamond = new Diamond(sensor, beacon);
        diamonds.add(diamond);
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

    private final class RecursiveScan extends RecursiveTask<Point> {
        private int start;
        private int end;
        private int width;
        private static final int SIZE = 5000;

        public RecursiveScan(int start, int end, int width) {
            this.start = start;
            this.end = end;
            this.width = width;
        }

        @Override
        protected Point compute() {
            if (isStopRequested())
                return SENTINEL;
            if (end - start <= SIZE) {
                Point result = scan(start, end, width);
                if (result.equals(SENTINEL))
                    return result;
                requestStop();
                return result;
            }
            // Split the region in half and process each side concurrently in a different thread.
            int mid = (start + end) / 2;
            RecursiveScan firstHalf = new RecursiveScan(start, mid - 1, width);
            firstHalf.fork();
            RecursiveScan secondHalf = new RecursiveScan(mid, end, width);
            Point rightResult = secondHalf.compute();
            if (!rightResult.equals(SENTINEL))
                return rightResult;
            Point leftResult = firstHalf.join();
            if (!leftResult.equals(SENTINEL))
                return leftResult;
            return SENTINEL;
        }
    }
}
