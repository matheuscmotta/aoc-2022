import java.io.FileReader;
import java.io.BufferedReader;

public class d04p2 {
    public static void main(String[] args) {
        String currentLine;
        int total = 0;

        try (BufferedReader textFile = new BufferedReader(new FileReader("input/d04.txt"))) {
            while (true) {
                currentLine = textFile.readLine();
                if (currentLine == null)
                    break;

                total += isOverlapped(parseRanges(currentLine)) ? 1 : 0;
            }
            System.out.println(total);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    static int[][] parseRanges(String ranges) {
        String[] stringRanges = ranges.split(",");

        String[] leftStringRange = stringRanges[0].split("-");
        String[] rightStringRange = stringRanges[1].split("-");

        int[] leftRange =
                {Integer.parseInt(leftStringRange[0]), Integer.parseInt(leftStringRange[1])};
        int[] rightRange =
                {Integer.parseInt(rightStringRange[0]), Integer.parseInt(rightStringRange[1])};

        return new int[][] {leftRange, rightRange};
    }

    static boolean isOverlapped(int[][] arrayRanges) {
        int leftMin = Integer.min(arrayRanges[0][0], arrayRanges[1][0]);
        int rightMax = Integer.max(arrayRanges[0][1], arrayRanges[1][1]);
        int unionLength = rightMax - leftMin;
        int leftLength = arrayRanges[0][1] - arrayRanges[0][0];
        int rightLength = arrayRanges[1][1] - arrayRanges[1][0];

        if (leftLength + rightLength >= unionLength)
            return true;

        return false;
    }
}
