import java.io.FileReader;
import java.util.Objects;
import java.io.BufferedReader;

class d01p1 {
    public static void main(String[] args) {
        int currentMax = 0;
        int sum = 0;
        String currentLine;

        try (BufferedReader textFile = new BufferedReader(new FileReader("input/d01.txt"))) {
            do {
                currentLine = textFile.readLine();
                if (Objects.equals(currentLine, "") || currentLine == null) {
                    if (sum > currentMax)
                        currentMax = sum;
                    sum = 0;
                } else {
                    sum += Integer.parseInt(currentLine);
                }
            } while (currentLine != null);
            System.out.println(currentMax);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
