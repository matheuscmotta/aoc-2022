package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class d12p1 {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("aoc/input/d12.txt"));
        Grid grid = new Grid(lines);
        Node end = grid.find();
        // System.out.println(grid);
        System.out.println(end.count());
    }
}


class Grid {
    private Node[][] grid;
    private LinkedList<Node> queue = new LinkedList<>();

    public Grid(List<String> lines) {
        grid = new Node[lines.size()][lines.get(0).length()];
        for (int i = 0; i < getWidth(); i++) {
            String line = lines.get(i);
            for (int j = 0; j < getHeight(); j++) {
                Node newNode = new Node(line.charAt(j), new int[] {i, j});
                grid[i][j] = newNode;
                if (newNode.isStart()) {
                    queue.add(newNode);
                }
            }
        }
    }

    public Node find() {
        queue.getFirst().setIsExplored();
        while (!queue.isEmpty()) {
            Node currentNode = queue.removeFirst();
            if (currentNode.isEnd()) {
                return currentNode;
            }
            for (Node neighbor : getNeighbors(currentNode)) {
                if (!neighbor.getIsExplored() && canReach(currentNode, neighbor)) {
                    neighbor.setIsExplored();
                    neighbor.setParent(currentNode);
                    queue.add(neighbor);
                }
            }
        }
        throw new Error("Could not find the end node.");
    }

    private ArrayList<Node> getNeighbors(Node node) {
        ArrayList<Node> neighborList = new ArrayList<>(4);
        int[] nodeCoordinates = node.getCoordinates();
        for (int[] direction : Vector.cardinalDirections) {
            int x = nodeCoordinates[0] + direction[0];
            int y = nodeCoordinates[1] + direction[1];
            if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
                neighborList.add(grid[x][y]);
            }
        }
        return neighborList;
    }

    private static boolean canReach(Node from, Node to) {
        return to.getName() - from.getName() <= 1;
    }

    private int getWidth() {
        return grid.length;
    }

    private int getHeight() {
        return grid[0].length;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                stringBuilder.append(grid[i][j]);
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}


class Node implements Iterable<Node> {
    private char name;
    private int[] coordinates;
    private boolean isStart = false;
    private boolean isEnd = false;
    private Node parent = null;
    private boolean isExplored = false;

    public Node(char name, int[] coordinates) {
        switch (name) {
            case 'S':
                this.name = 'a';
                isStart = true;
                break;
            case 'E':
                this.name = 'z';
                isEnd = true;
                break;
            default:
                this.name = name;
        }
        this.coordinates = coordinates;
    }

    public char getName() {
        return name;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public void setParent(Node node) {
        parent = node;
    }

    public boolean isStart() {
        return isStart;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public boolean getIsExplored() {
        return isExplored;
    }

    public void setIsExplored() {
        isExplored = true;
    }

    public int count() {
        int count = 0;
        Iterator<Node> iterator = this.iterator();
        while (iterator.hasNext()) {
            count += 1;
            iterator.next();
        }
        return count;
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeIterator();
    }

    private class NodeIterator implements Iterator<Node> {
        private Node current = Node.this;

        @Override
        public boolean hasNext() {
            return current.parent != null;
        }

        @Override
        public Node next() {
            Node node = current;
            current = current.parent;
            return node;
        }
    }

    @Override
    public String toString() {
        return Character.toString(name);
    }
}


class Vector {
    public static int[][] cardinalDirections = new int[4][2];
    static {
        for (int i = 0; i < 4; i++) {
            cardinalDirections[i] = Vector.rotate(new int[] {1, 0}, i);
        }
    }

    public static int[] rotate(int[] direction, int n) {
        int[] rotated = direction.clone();
        for (int i = 0; i < n; i++) {
            int x = rotated[0];
            rotated[0] = rotated[1];
            rotated[1] = -x;
        }
        return rotated;
    }

    public static int[] getPoint(int[] from, int[] direction) {
        return new int[] {from[0] + direction[0], from[1] + direction[1]};
    }
}
