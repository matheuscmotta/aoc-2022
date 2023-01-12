import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class d13p1 {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("input/d13.txt"));
        ArrayList<Pair> listOfPairs = Pair.createListOfPairs(lines);
        int sumOfIndices = 0;
        for (Pair pair : listOfPairs) {
            if (pair.isOrdered()) {
                sumOfIndices += pair.getIndex();
            }
        }
        System.out.println(sumOfIndices);
    }
}


class Pair {
    private Packet left;
    private Packet right;
    private int index;

    public Pair(Packet left, Packet right, int index) {
        this.left = left;
        this.right = right;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean isOrdered() {
        if (left.compareTo(right) <= 0) {
            return true;
        }
        return false;
    }

    public static ArrayList<Pair> createListOfPairs(List<String> lines) {
        ArrayList<Pair> listOfPairs = new ArrayList<>();
        Iterator<String> iterator = lines.iterator();
        int index = 1;
        while (iterator.hasNext()) {
            String head = iterator.next();
            if (head.equals("")) {
                index += 1;
                continue;
            }
            // Packets come in pairs.
            String tail = iterator.next();
            Packet left = new Packet(head);
            Packet right = new Packet(tail);
            Pair pair = new Pair(left, right, index);
            listOfPairs.add(pair);
        }
        return listOfPairs;
    }

    @Override
    public String toString() {
        return "#" + index + ": " + left + " : " + right;
    }
}


class Packet implements Comparable<Packet> {
    private Node node;
    private String listString;

    public Packet(String listString) {
        this.listString = listString;
        node = toTree(listString);
    }

    public Node getNode() {
        return node;
    }

    private static Node toTree(String listString) {
        // Start with index 1 to avoid two "list nodes" at the top.
        return toTree(listString, 1);
    }

    private static Node toTree(String listString, int startIndex) {
        Node root = new Node(null);

        char[] tokens = listString.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        outer: for (int i = startIndex; i < tokens.length; i++) {
            char token = tokens[i];
            switch (token) {
                case '[':
                    Node innerRoot = toTree(listString, i + 1);
                    innerRoot.setParent(root);
                    i = innerRoot.getEndIndex();
                    break;
                case ',':
                    if (stringBuilder.length() > 0) {
                        root.createAndAddChild(stringBuilder.toString());
                        // Start a new builder for the next list item, if any.
                        stringBuilder = new StringBuilder();
                    }
                    break;
                case ']':
                    if (stringBuilder.length() > 0) {
                        root.createAndAddChild(stringBuilder.toString());
                    }
                    root.setEndIndex(i);
                    break outer;
                default:
                    stringBuilder.append(token);
                    break;
            }
        }
        return root;
    }

    @Override
    public int compareTo(Packet to) {
        Node leftTree = node;
        Node rightTree = to.getNode();
        return compareTrees(leftTree, rightTree);
    }

    private int compareTrees(Node left, Node right) {
        ArrayList<Node> leftChildren = left.getChildren();
        ArrayList<Node> rightChildren = right.getChildren();
        int leftSize = leftChildren.size();
        int rightSize = rightChildren.size();
        int minimum = Math.min(leftSize, rightSize);
        for (int i = 0; i < minimum; i++) {
            Node leftChild = leftChildren.get(i);
            Node rightChild = rightChildren.get(i);

            // If item vs list, then mutate and keep going.
            if ((leftChild.isValue() && rightChild.isList())
                    || leftChild.isList() && rightChild.isValue()) {
                if (leftChild.isValue()) {
                    leftChild.toList();
                } else {
                    rightChild.toList();
                }
            }

            // List vs list.
            if (leftChild.isList() && rightChild.isList()) {
                int recursiveComparison = compareTrees(leftChild, rightChild);
                if (recursiveComparison == -1 | recursiveComparison == 1) {
                    return recursiveComparison;
                }
            }

            // Item vs item.
            int valueComparison = leftChild.compareTo(rightChild);
            if (valueComparison == -1 | valueComparison == 1) {
                return valueComparison;
            }
        }

        // When nodes on both sides have no children or children of different sizes.
        if (leftSize < rightSize) {
            return -1;
        } else if (leftSize > rightSize) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return listString;
    }
}


final class Node implements Comparable<Node> {
    private Integer value;
    private Node parent;
    private ArrayList<Node> children = new ArrayList<>();

    // A helper field for the parser.
    private int endIndex;

    public Node(Node parent) {
        this.parent = parent;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
        parent.addChild(this);
    }

    public void toList() {
        createAndAddChild(Integer.toString(value));
        value = null;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    private void addChild(Node children) {
        this.children.add(children);
    }

    public void createAndAddChild(String value) {
        Node newChildren = new Node(this);
        newChildren.setValue(Integer.parseInt(value));
        addChild(newChildren);
    }

    public boolean isValue() {
        return value != null ? true : false;
    }

    public boolean isList() {
        return !isValue();
    }

    @Override
    public int compareTo(Node right) {
        if (isValue() && right.isValue()) {
            return (int) Math.signum(value - right.getValue());
        }
        return 0;
    }

    @Override
    public String toString() {
        return isValue() ? "Value: " + value : "List with " + children.size() + " children";
    }
}
