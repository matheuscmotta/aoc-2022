package aoc;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.BufferedReader;

public class d05p1 {
    public static void main(String[] args) {
        Crates crates = new Crates();
        crates.execute();
        System.out.println(crates.read());
    }
}


class Crates {
    private ArrayList<LinkedList<String>> crates;
    private ArrayList<ArrayList<Integer>> operations;

    public Crates() {
        // The crates in the input data are in a 9 by 7 grid.
        crates = new ArrayList<>(9 * 7);
        // The input data has 503 crane operations.
        operations = new ArrayList<>(503);

        // Open the file once and parse it, storing the data in a custom format
        // represented by this type. Some repetition, but convenience...
        try (BufferedReader textFile = new BufferedReader(new FileReader("aoc/input/d5.txt"))) {
            String currentLine;

            // Read the crates. Store it as a list of linked lists, heads being top of
            // stack.
            int line = 0;
            outer: while (true) {
                currentLine = textFile.readLine();
                if (currentLine == null)
                    break;

                // Iterates the line as a block of 4 columns: crate + space "[A] ".
                for (int column = 0; column < currentLine.length(); column = column + 4) {
                    // Fix for last column of the line not being a space.
                    if (column + 3 > currentLine.length())
                        break;

                    // Declare the linked lists when processing the first line.
                    if (line == 0)
                        crates.add(new LinkedList<String>());

                    // Read the crate.
                    String currentCrate = currentLine.substring(column, column + 3);

                    // Break when done with the crates.
                    if (currentCrate.trim().equals("1"))
                        break outer;

                    // Filter empty slots.
                    if (currentCrate.contains("[")) {
                        // Offset the column to store in the array.
                        crates.get(column / 4).add(currentCrate);
                    }
                }
                line += 1;
            }

            // Read the crane operations. Store it as a list of lists: [quant., from, to].
            line = 0;
            while (true) {
                currentLine = textFile.readLine();
                if (currentLine == null)
                    break;

                // Skip the blank line between the crates part and the operations part.
                if (currentLine.equals(""))
                    continue;

                // Initialize the list of operations.
                operations.add(new ArrayList<Integer>());

                // Match the complement of digits to split the string.
                String[] regexArray = currentLine.split("[^\\d+]");

                // Filter and store the operations as a list of integers.
                for (int i = 0; i < regexArray.length; i++) {
                    if (regexArray[i].length() > 0)
                        operations.get(line).add(Integer.parseInt(regexArray[i]));
                }
                line += 1;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void printCrates() {
        System.out.println(crates);
    }

    public void printOperations() {
        System.out.println(operations);
    }

    public void execute() {
        for (ArrayList<Integer> operation : operations) {
            for (int i = 1; i <= operation.get(0); i++) {
                String pickCrate = crates.get(operation.get(1) - 1).removeFirst();
                crates.get(operation.get(2) - 1).addFirst(pickCrate);
            }
        }
    }

    public String read() {
        String heads = "";
        for (LinkedList<String> head : crates) {
            heads += head.getFirst();
        }
        return heads.replace("[", "").replace("]", "");
    }
}
