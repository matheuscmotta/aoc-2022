import java.io.FileReader;
import java.util.HashSet;
import java.io.BufferedReader;

public class d06p1 {
    public static void main(String[] args) {
        // This is a very simple solution to the puzzle, but far from ideal. A better
        // approach would involve skipping the frame ahead of duplicates instead of
        // scrolling one character at a time. For example, if the frame is: a[bcdd]e
        // then we could scroll it 3 characters at once: abcd[defe]g. And other
        // problems, but not worth the trouble in this case.
        try (BufferedReader textFile = new BufferedReader(new FileReader("input/d06.txt"))) {
            String signal = textFile.readLine();
            for (int i = 0; i < signal.length() - 4; i++) {
                String frame = signal.substring(i, i + 4);
                char[] characters = frame.toCharArray();
                HashSet<Character> mixer = new HashSet<>(4);
                for (char character : characters) {
                    mixer.add(character);
                }
                if (mixer.size() == 4) {
                    System.out.println(i + 4);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
