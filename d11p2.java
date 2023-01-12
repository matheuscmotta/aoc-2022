import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.LinkedList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class d11p2 {
    public static void main(String[] args) throws IOException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<String> monkeys = Files.readAllLines(Path.of("input/d11.txt"));
        HashMap<Integer, Monkey> mapOfMonkeys = Monkey.parseMonkeys(monkeys);

        // Get the least common multiple of of each monkey test number, which in this case
        // is just the product of each test number, since they are all prime numbers.
        int lcmOfTests = 1;
        for (int i = 0; i < mapOfMonkeys.size(); i++) {
            lcmOfTests *= mapOfMonkeys.get(i).getTest();
        }

        // Rounds.
        int rounds = 10000;
        for (int i = 1; i <= rounds; i++) {
            // Turns.
            for (int j = 0; j < mapOfMonkeys.size(); j++) {
                Monkey monkey = mapOfMonkeys.get(j);
                monkey.run(mapOfMonkeys, lcmOfTests);
            }

            // Print the state of the last round.
            if (i == rounds) {
                System.out.println("\nRound " + i + ":");
                for (int k = 0; k < mapOfMonkeys.size(); k++) {
                    Monkey monkey = mapOfMonkeys.get(k);
                    System.out.println(monkey);
                }
            }
        }

        // Compute the level of monkey business.
        ArrayList<Integer> inspectionList = new ArrayList<>();
        for (int i = 0; i < mapOfMonkeys.size(); i++) {
            inspectionList.add(mapOfMonkeys.get(i).getInspectionCount());
        }
        // Get the two highest values of the list and print the data.
        int[] maxOfInspection = maxOfList(inspectionList);
        int max1 = maxOfInspection[0];
        int max2 = maxOfInspection[1];
        System.out.println("\nHighest inspection number: " + max1);
        System.out.println("\nSecond highest inspection number: " + max2);
        System.out.println("\nBusiness level: " + (long) max1 * max2);
    }

    public static int[] maxOfList(List<Integer> list) {
        int max1 = 0;
        int max2 = 0;
        for (int i : list) {
            if (i > max1) {
                max2 = max1;
                max1 = i;
            } else if (i > max2) {
                max2 = i;
            }
        }
        return new int[] {max1, max2};
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

    public void run(HashMap<Integer, Monkey> mapOfMonkeys, int lcmOfTests)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // A monkey with no items to play with passes its turn.
        if (listOfItems.size() == 0)
            return;

        // Inspect, test and then throw each item in the list.
        Iterator<Long> iterator = listOfItems.iterator();
        while (iterator.hasNext()) {
            long oldWorry = iterator.next();

            /*
             * To understand this problem, it helps to observe the life cycle of an item as it is
             * being thrown by the monkeys. An item I1 is inspected and then determined to have a
             * worry level of w1. The monkey applies his test and throws it to another monkey. The
             * item is then inspected by its new monkey to have a worry level of w2 and thrown
             * again. This process happens once per round.
             * 
             * Because the inspection operation is either a product or a sum of positive integers,
             * the j-th worry level, wj, becomes a very large number which overflows the `long` type
             * capacity at some round j. One solution is to use the `BigInteger` type to allow for
             * arbitrary precision operations, but the puzzle clearly states that the point is to
             * find another way to keep the worry levels manageable.
             * 
             * Notice that the worry level of an item can be replaced by another number as long as
             * it has the same remainder when divided by the monkey test number, since the remainder
             * is all that matters for the monkey's test. In mathematical terms, a number x1 can
             * substitute w1 when it has the same reminder as w1 when divided by t1, where t1 is the
             * monkey test number. In symbols: x1 ≡ w1 (mod t1).
             * 
             * But, when I1 is sent to the next monkey and it applies its inspection to obtain X1 =
             * newWorry(x1), this number might not be congruent with w2. For example, if w1 = 50 and
             * t1 = 2, then x1 = 0. The next monkey, with t2 = 3 and operation new = old + 6 would
             * have w2 = 56 thus failing the test while with X1 = 6 the test would pass, making the
             * monkey throw it to the wrong monkey.
             * 
             * To fix this issue, we need to pick x1 in a way that X1 ≡ w2 (mod t2). These numbers
             * have been derived from x1 and w1 by the current monkey operation: X1 = x1 + c or X1 =
             * x1 * c, for some arbritrary integer c. Let's analyze each case:
             * 
             * Sum: x1 + c ≡ w1 + c (mod t2) ⇔ x1 ⇔ w1 (mod t2), by the additive cancelation law of
             * congruences.
             * 
             * Product: to use the multiplicative cancelation law of congruences there is the
             * constraint to have gcd(c, t2) = 1 and this is true because every t_i is a prime
             * number, hence we have x1 * c ≡ w1 * c (mod t2) ⇔ x1 ≡ w1 (mod t2).
             * 
             * Thus, choosing x1 such that x1 ≡ w1 (mod t1) and x1 ≡ w1 (mod t2) solves the problem
             * mentioned above with X1. To satisfy such conditions we can use a property of
             * congruences which stablishes a relationship between multiple congruences of equal
             * operands and distinct modulus:
             * 
             * x_i ≡ w_i (mod t_i), i = ⇔ x_i ≡ w_i (mod lcm(t1, ..., t_n)).
             * 
             * Refer to Arithmetic books for proof. For instance, Abramo Hefez p.115.
             * 
             * Consequently, if we take x1 such that x1 ≡ w1 (mod lcm(t1, t2)), then X1 will be
             * congruent with w2. Do that for each subsequent round to get the solution and upper
             * bound: newWorry(oldWorry) (mod lmc(t1, ..., tn)).
             */
            listOfItems.set(0, newWorry(oldWorry) % lcmOfTests);
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

    public int getTest() {
        return test;
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
        stringBuilder.append(" [ inspected " + inspectionCount + " items]");
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
