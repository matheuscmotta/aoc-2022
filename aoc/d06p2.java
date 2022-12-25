package aoc;

import java.io.FileReader;
import java.util.HashSet;
import java.io.BufferedReader;

public class d06p2 {
    public static void main(String[] args) {
        // With a bigger frame the previous remark makes even more sense, but still not
        // worth the trouble.
        try (BufferedReader textFile = new BufferedReader(new FileReader("aoc/input/d6.txt"))) {
            String signal = textFile.readLine();
            for (int i = 0; i < signal.length() - 14; i++) {
                String frame = signal.substring(i, i + 14);
                char[] characters = frame.toCharArray();
                HashSet<Character> mixer = new HashSet<>(14);
                for (char character : characters) {
                    mixer.add(character);
                }
                if (mixer.size() == 14) {
                    System.out.println(i + 14);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
