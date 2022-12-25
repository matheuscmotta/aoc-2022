package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class d10p2 {
    public static void main(String[] args) throws IOException {
        // This is more concise than the previous file reading code.
        List<String> instructions = Files.readAllLines(Paths.get("aoc/input/d10.txt"));

        // Run the simulation.
        CPU cpu = new CPU(instructions);
        cpu.run();

        // Print the CRT state.
        System.out.print(cpu.getCRT());
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
    private CRT crt;
    private Iterator<String> iterator;

    public CPU(List<String> instructions) {
        registerX = 1;
        currentCycle = 1;
        currentValue = null;
        currentState = State.READ;
        history = new History();
        crt = new CRT();
        iterator = instructions.iterator();
    }

    public History getHistory() {
        return history;
    }

    public CRT getCRT() {
        return crt;
    }

    public void run() {
        while (iterator.hasNext() || currentValue != null) {
            // The CPU state is logged at the start of each cycle.
            history.add(currentCycle, registerX);
            // Draw the pixel.
            crt.drawPixel(currentCycle, registerX);

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
}


class History {
    private HashMap<Integer, Integer> history;

    public History() {
        history = new HashMap<>();
    }

    public void add(int cycle, int value) {
        history.put(cycle, value);
    }

    public int get(int cycle) {
        return history.get(cycle);
    }

    public int getStrengthAt(int cycle) {
        return cycle * history.get(cycle);
    }

    public void printCycle(int cycle) {
        System.out.println("Cycle: " + cycle + " Value: " + history.get(cycle) + " Strength: "
                + getStrengthAt(cycle));
    }
}


class CRT {
    StringBuilder pixels;

    public CRT() {
        pixels = new StringBuilder();
    }

    public void drawPixel(int cycle, int registerX) {
        // Offset the cycle count to match the screen row.
        int cycleOffset = (cycle + 39) % 40;
        if (Math.abs(cycleOffset - registerX) <= 1) {
            pixels.append("#");
        } else {
            pixels.append(".");
        }
    }

    @Override
    public String toString() {
        // The `pixels` variable represents a sequence of pixels. To form the screen we need to add
        // a linebreak after each 40th character in that sequence.
        StringBuilder screen = new StringBuilder();
        int count = 0;
        for (int i = 0; i < pixels.length(); i++) {
            screen.append(pixels.charAt(i));
            count += 1;
            if (count == 40) {
                screen.append("\n");
                count = 0;
            }
        }
        return screen.toString();
    }
}
