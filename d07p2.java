import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.FileReader;
import java.io.BufferedReader;

public class d07p2 {
    public static void main(String[] args) {
        Registry registry = new Registry();
        Directory root = new Directory("/", null, registry);
        parseInput(root, registry);
        // Could've organized this better, but whatever.
        int usedSpace = root.size();
        int unnusedSpace = 70000000 - usedSpace;
        ArrayList<Directory> candidatesForDeletion = new ArrayList<>();
        for (Directory directory : registry) {
            if (directory.size() + unnusedSpace >= 30000000) {
                candidatesForDeletion.add(directory);
            }
        }
        Directory toDelete = candidatesForDeletion.get(0);
        for (Directory directory : candidatesForDeletion) {
            // Good thing we have a cache mechanism for the size.
            if (directory.size() < toDelete.size()) {
                toDelete = directory;
            }
        }
        System.out.println(toDelete.size());
    }

    static void parseInput(Directory root, Registry registry) {
        try (BufferedReader textFile = new BufferedReader(new FileReader("input/d07.txt"))) {
            Directory currentDirectory = null;

            while (true) {
                String currentLine = textFile.readLine();
                if (currentLine == null)
                    break;

                // A special case for the root directory which is the first line in the input.
                if (currentDirectory == null) {
                    currentDirectory = root;
                    continue;
                }

                // Either change directory or start adding files/directories to parent.
                if (currentLine.startsWith("$")) {
                    String command = currentLine.substring(2, 4);
                    if (command.equals("ls"))
                        continue;

                    String directoryName = currentLine.substring(5, currentLine.length());
                    if (directoryName.equals("..")) {
                        currentDirectory = currentDirectory.parent;
                        continue;
                    }

                    currentDirectory = currentDirectory.getSubDirectory(directoryName);
                    continue;
                }

                // Add directory.
                if (currentLine.startsWith("dir")) {
                    String newDirectoryName = currentLine.substring(4, currentLine.length());
                    currentDirectory.addDirectory(
                            new Directory(newDirectoryName, currentDirectory, registry));
                    continue;
                }
                // Add file.
                // This regex matches the white space character.
                String[] fileParts = currentLine.split("\\s+");
                int fileSize = Integer.parseInt(fileParts[0]);
                String fileName = fileParts[1];
                currentDirectory.addFile(new File(fileName, fileSize));
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}


/**
 * This class is meant to simplify traversing the filesystem tree. Whenever a directory is created,
 * its constructor will add itself to the list.
 */
class Registry implements Iterable<Directory> {
    private LinkedList<Directory> registries;

    public Registry() {
        registries = new LinkedList<>();
    }

    public void addRegistry(Directory directory) {
        registries.add(directory);
    }

    @Override
    public Iterator<Directory> iterator() {
        return registries.iterator();
    }
}


class Directory {
    public final String name;
    public final Directory parent;
    private ArrayList<Directory> directories;
    private ArrayList<File> files;
    private int sizeCache = -1;

    public Directory(String name, Directory parent, Registry registry) {
        this.name = name;
        this.parent = parent;
        directories = new ArrayList<>();
        files = new ArrayList<>();
        registry.addRegistry(this);
    }

    public void addDirectory(Directory directory) {
        directories.add(directory);
    }

    public void addFile(File file) {
        files.add(file);
    }

    public Directory getSubDirectory(String directoryName) throws Error {
        for (Directory directory : directories) {
            if (directory.name.equals(directoryName)) {
                return directory;
            }
        }
        throw new Error("Not found.");
    }

    public int size() {
        // Non-negative size means it has already been cached.
        if (sizeCache >= 0)
            return sizeCache;
        // Otherwise, compute the size and cache it.
        sizeCache = 0;
        for (File file : files) {
            sizeCache += file.size();
        }
        for (Directory directory : directories) {
            sizeCache += directory.size();
        }
        return sizeCache;
    }
}


class File {
    public final String name;
    private final int size;

    public File(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public int size() {
        return size;
    }
}
