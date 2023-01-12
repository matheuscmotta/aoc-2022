import java.io.FileReader;
import java.util.HashMap;
import java.io.BufferedReader;

public class d02p1 {
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
        points.put(new String("X"), 1);
        points.put(new String("Y"), 2);
        points.put(new String("Z"), 3);
        points.put(new String("lost"), 0);
        points.put(new String("draw"), 3);
        points.put(new String("won"), 6);

        // Win map. Rock beats scisor, etc.
        HashMap<String, String> winTable = new HashMap<String, String>();
        winTable.put("X", "Z");
        winTable.put("Y", "X");
        winTable.put("Z", "Y");

        // The string format is: A X, B Z, etc. we replace to make it easier to compare.
        String[] parts = lineString.split(" ");
        String enemy = parts[0].replace("A", "X").replace("B", "Y").replace("C", "Z");
        String me = parts[1];

        // Draw.
        if (enemy.equals(me))
            return points.get("draw") + points.get(me);

        // Won.
        if (winTable.get(me).equals(enemy))
            return points.get("won") + points.get(me);

        // Lost.
        return points.get("lost") + points.get(me);
    }
}
