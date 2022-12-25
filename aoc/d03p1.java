package aoc;

import java.io.FileReader;
import java.io.BufferedReader;

public class d03p1 {
    public static void main(String[] args) {
        String currentLine;
        int total = 0;

        try (BufferedReader textFile = new BufferedReader(new FileReader("aoc/input/d3.txt"))) {
            while (true) {
                currentLine = textFile.readLine();
                if (currentLine == null)
                    break;

                total += getPriority(compare(split(currentLine)));
            }
            System.out.println(total);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    static String[] split(String rucksack) {
        int half = rucksack.length() / 2;
        return new String[] {rucksack.substring(0, half), rucksack.substring(half)};
    }

    static char compare(String[] parts) throws Exception {
        char tmp = '!';
        outer: for (int i = 0; i < parts[0].length(); i++) {
            for (int j = 0; j < parts[1].length(); j++) {
                if (parts[0].charAt(i) == parts[1].charAt(j)) {
                    tmp = parts[0].charAt(i);
                    break outer;
                }
            }
        }
        if (tmp == '!')
            throw new Exception("No match. Invalid input.");
        return tmp;
    }

    static int getPriority(char item) {
        int ascii = (int) item;
        if (Character.isLowerCase(item))
            return ascii - 96;

        return ascii - 38;
    }
}
