import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.io.BufferedReader;

class d01p2 {
    public static void main(String[] args) {
        int sum = 0;
        int topThree = 0;
        String currentLine;
        ArrayList<Integer> listOfElfs = new ArrayList<Integer>();

        try (BufferedReader textFile = new BufferedReader(new FileReader("input/d01.txt"))) {
            do {
                currentLine = textFile.readLine();
                if (Objects.equals(currentLine, "") || currentLine == null) {
                    listOfElfs.add(sum);
                    sum = 0;
                } else {
                    sum += Integer.parseInt(currentLine);
                }
            } while (currentLine != null);

            Collections.sort(listOfElfs);
            for (int i = 0; i < 3; i++) {
                topThree += listOfElfs.get(listOfElfs.size() - 1 - i);
            }
            System.out.println(topThree);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
