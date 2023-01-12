import java.io.FileReader;
import java.util.HashMap;
import java.io.BufferedReader;

public class d02p2 {
    public static void main(String[] args) {
        String currentLine;
        Integer totalPoints = 0;

        try (BufferedReader textFile = new BufferedReader(new FileReader("input/d02.txt"))) {
            while (true) {
                currentLine = textFile.readLine();
                if (currentLine == null)
                    break;
                totalPoints += calculateRound(currentLine);
            }
            System.out.println(totalPoints);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static Integer calculateRound(String lineString) {
        // Points map.
        HashMap<String, Integer> points = new HashMap<String, Integer>();
        points.put(new String("A"), 1);
        points.put(new String("B"), 2);
        points.put(new String("C"), 3);
        points.put(new String("lost"), 0);
        points.put(new String("draw"), 3);
        points.put(new String("won"), 6);

        // Win map. Rock beats scisor, etc.
        HashMap<String, String> winTable = new HashMap<String, String>();
        winTable.put("A", "C");
        winTable.put("B", "A");
        winTable.put("C", "B");

        // Lose map. Rock beats scisor, etc.
        HashMap<String, String> loseTable = new HashMap<String, String>();
        loseTable.put("A", "B");
        loseTable.put("B", "C");
        loseTable.put("C", "A");

        // The string format is: A X, B Z, etc. we replace to make it easier to compare.
        String[] parts = lineString.split(" ");
        String enemy = parts[0];
        String play = parts[1];

        // X: lose
        if (play.equals("X")) {
            return points.get("lost") + points.get(winTable.get(enemy));
        }

        // Y: draw
        if (play.equals("Y")) {
            return points.get("draw") + points.get(enemy);
        }

        // Z: win
        return points.get("won") + points.get(loseTable.get(enemy));
    }
}
