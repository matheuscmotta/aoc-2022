package aoc;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class d10p1 {
    public static void main(String[] args) {
        ArrayList<String> instructions = new ArrayList<>();
        try (BufferedReader textFile = new BufferedReader(new FileReader("aoc/input/d10.txt"))) {
            String currentLine;
            while (true) {
                currentLine = textFile.readLine();
                if (currentLine == null)
                    break;
                instructions.add(currentLine);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        CPU cpu = new CPU(instructions);
        cpu.run();

        // Select the cycles of interest and print the results.
        List<Integer> cyclesOfInterest = IntStream.range(20, 220 + 1)
                .filter(x -> (x + 20) % 40 == 0).boxed().collect(Collectors.toList());
        History history = cpu.getHistory();
        int sumOfStrenghts = 0;
        for (int cycle : cyclesOfInterest) {
            history.printCycle(cycle);
            sumOfStrenghts += history.getStrengthAt(cycle);
        }
        System.out.println("Sum of signal strenghts: " + sumOfStrenghts);
    }
}


class CPU {
    private static enum State {
        READ, WRITE
    }

    private int registerX;
    private int currentCycle;
    private Integer currentValue;
    private State currentState;
    private History history;
    private Iterator<String> iterator;

    public CPU(ArrayList<String> instructions) {
        registerX = 1;
        currentCycle = 1;
        currentValue = null;
        currentState = State.READ;
        history = new History();
        iterator = instructions.iterator();
    }

    public void run() {
        while (iterator.hasNext() || currentValue != null) {
            // The CPU state is logged at the start of each cycle.
            history.add(currentCycle, registerX);

            if (currentState == State.READ) {
                // Read an instruction.
                String instruction = iterator.next();
                Integer value = parseInstruction(instruction);
                if (value != null) {
                    currentValue = value;
                    // Open the cpu to write to the register on the next cycle.
                    currentState = State.WRITE;
                }
            }
            // Verbose else for better context.
            else if (currentState == State.WRITE) {
                // Write to register x.
                registerX += currentValue;
                currentValue = null;
                currentState = State.READ;
            }

            // Increment the cycle number.
            currentCycle += 1;
        }
    }

    private Integer parseInstruction(String instruction) {
        String[] parts = instruction.split(" ");
        // A noop is represented by null and addx is represented by an int.
        Integer value;
        try {
            value = Integer.parseInt(parts[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            value = null;
        }
        return value;
    }

    public History getHistory() {
        return history;
    }
}


class History {
    private HashMap<Integer, Integer> history;

    public History() {
        history = new HashMap<>();
    }

    public void add(int cycle, int value) {
        history.put(cycle, value);
    }

    public int getStrengthAt(int cycle) {
        return cycle * history.get(cycle);
    }

    public void printCycle(int cycle) {
        System.out.println("Cycle: " + cycle + " Value: " + history.get(cycle) + " Strength: "
                + getStrengthAt(cycle));
    }
}
