import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.LinkedList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class d11p1 {
    public static void main(String[] args) throws IOException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // Parse the file.
        List<String> monkeys = Files.readAllLines(Path.of("input/d11.txt"));
        HashMap<Integer, Monkey> mapOfMonkeys = Monkey.parseMonkeys(monkeys);

        // Play the rounds.
        for (int i = 1; i <= 20; i++) {
            // A monkey plays its turns.
            for (int j = 0; j < mapOfMonkeys.size(); j++) {
                Monkey monkey = mapOfMonkeys.get(j);
                monkey.run(mapOfMonkeys);
            }

            // Print the state of the round.
            System.out.println("\nRound " + i + ":");
            for (int k = 0; k < mapOfMonkeys.size(); k++) {
                Monkey monkey = mapOfMonkeys.get(k);
                System.out.println(monkey);
            }
        }

        // Compute the level of monkey business and print it.
        ArrayList<Integer> inspectionList = new ArrayList<>();
        for (int i = 0; i < mapOfMonkeys.size(); i++) {
            inspectionList.add(mapOfMonkeys.get(i).getInspectionCount());
        }
        // Sort ascending and pick the last two items.
        Collections.sort(inspectionList);
        int businessLevel = inspectionList.get(inspectionList.size() - 1)
                * inspectionList.get(inspectionList.size() - 2);
        System.out.println("\nBusiness level: " + businessLevel);
    }
}


class Monkey {
    private int monkeyNumber;
    private LinkedList<Long> listOfItems;
    private Method operation;
    private final String operand;
    private int test;
    private int ifTrue;
    private int ifFalse;
    private int inspectionCount = 0;

    public Monkey(int monkeyNumber, LinkedList<Long> listOfItems, Method operation, String operand,
            int test, int ifTrue, int ifFalse) {
        this.monkeyNumber = monkeyNumber;
        this.listOfItems = listOfItems;
        this.operation = operation;
        this.operand = operand;
        this.test = test;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    private long newWorry(long old)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // Note to self: the invoke method returns an Object instead of the return value of the
        // method, hence the cast to int.
        return (long) operation.invoke(null, old, getOperand(old));
    }

    private long getOperand(long old) {
        // The operand can be either a reference to `oldWorry` or a constant.
        if (operand.equals("old")) {
            return old;
        }
        return Integer.parseInt(operand);
    }

    private void catchItem(long item) {
        listOfItems.addLast(item);
    }

    public void run(HashMap<Integer, Monkey> mapOfMonkeys)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // A monkey with no items to play with passes its turn.
        if (listOfItems.size() == 0)
            return;

        // Inspect, test and then throw each item in the list.
        Iterator<Long> iterator = listOfItems.iterator();
        while (iterator.hasNext()) {
            // Inspect: take the item, observe the new worry level, get bored of it (divide by
            // 3), then set the new worry level.
            long oldWorry = iterator.next();
            listOfItems.set(0, (long) Math.floor(newWorry(oldWorry) / 3));
            inspectionCount += 1;

            // Get the updated worry level.
            long newWorry = listOfItems.getFirst();

            // Test: depending on the new worry level, determine the monkey to throw to.
            int destinationMonkey = newWorry % test == 0 ? ifTrue : ifFalse;

            // Throw.
            Monkey targetMonkey = mapOfMonkeys.get(destinationMonkey);
            iterator.remove();
            targetMonkey.catchItem(newWorry);
        }
    }

    public int getInspectionCount() {
        return inspectionCount;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Monkey " + monkeyNumber + ": ");
        Iterator<Long> iterator = listOfItems.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next().toString());
            if (iterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    public static HashMap<Integer, Monkey> parseMonkeys(List<String> monkeys)
            throws NoSuchMethodException {
        Iterator<String> iterator = monkeys.iterator();
        HashMap<Integer, Monkey> monkeyList = new HashMap<>();
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.startsWith("Monkey")) {
                // "Monkey 0:" -> 0.
                String numberPart = line.split(" ")[1];
                int monkeyNumber =
                        Integer.parseInt(numberPart.substring(0, numberPart.length() - 1));

                // "Starting items: 79, 98" -> {79, 98}.
                LinkedList<Long> listOfItems = Arrays
                        .stream(iterator.next().split(":")[1].split(",")).map(String::trim)
                        .map(Long::parseLong).collect(Collectors.toCollection(LinkedList::new));

                // "Operation: new = old * 19" -> add | multiply.
                String[] operationLine = iterator.next().split(" ");
                Method operation = Operation.getOperation(operationLine[operationLine.length - 2]);
                // The right side operand in the notes can be either a number or a reference to
                // `old`, for example, new = old * old.
                String operand = operationLine[operationLine.length - 1];

                // "Test: divisible by 23" -> 23.
                String[] divisibleLine = iterator.next().split(" ");
                int test = Integer.parseInt(divisibleLine[divisibleLine.length - 1]);

                // "If true: throw to monkey 2" -> 2.
                String[] trueLine = iterator.next().split(" ");
                int ifTrue = Integer.parseInt(trueLine[trueLine.length - 1]);

                // "If false: throw to monkey 3" -> 3.
                String[] falseLine = iterator.next().split(" ");
                int ifFalse = Integer.parseInt(falseLine[falseLine.length - 1]);

                // Create a monkey and add it to the list.
                monkeyList.put(monkeyNumber, new Monkey(monkeyNumber, listOfItems, operation,
                        operand, test, ifTrue, ifFalse));
            }
        }
        return monkeyList;
    }
}


class Operation {
    private static final HashMap<String, String> symbolMap = new HashMap<>();
    static {
        symbolMap.put("+", "add");
        symbolMap.put("*", "multiply");
    }

    public static long add(long x, long y) {
        return x + y;
    }

    public static long multiply(long x, long y) {
        return x * y;
    }

    public static Method getOperation(String symbol) throws NoSuchMethodException {
        // Map the symbols + or * to their respective names.
        String symbolName = Operation.symbolMap.get(symbol);
        // Return the respective method.
        return Operation.class.getMethod(symbolName, long.class, long.class);
    }
}
