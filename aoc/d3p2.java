package aoc;

import java.io.FileReader;
import java.io.BufferedReader;

public class d3p2 {
    public static void main(String[] args) {
        String firstElf;
        String secondElf;
        String thirdElf;
        int total = 0;

        try (BufferedReader textFile = new BufferedReader(new FileReader("aoc/input/d3.txt"))) {
            while (true) {
                firstElf = textFile.readLine();
                secondElf = textFile.readLine();
                thirdElf = textFile.readLine();
                if (firstElf == null || secondElf == null || thirdElf == null)
                    break;

                total += getPriority(compare(firstElf, secondElf, thirdElf));
            }
            System.out.println(total);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    static char compare(String firstElf, String secondElf, String thirdElf) throws Exception {
        char tmp = '!';
        outer: for (int i = 0; i < firstElf.length(); i++) {
            for (int j = 0; j < secondElf.length(); j++) {
                for (int k = 0; k < thirdElf.length(); k++) {
                    if (firstElf.charAt(i) == secondElf.charAt(j) && firstElf.charAt(i) == thirdElf.charAt(k)) {
                        tmp = firstElf.charAt(i);
                        break outer;
                    }
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
