package Management;

import Model.File;
import Model.OpenFileTableEntry;
import java.util.*;

// manages all currently open files in the file system
// tracks open file entries and assigns unique file descriptors
public class OpenFileTable {

    // storage for all open file entries
    // key: file descriptor, value: open file table entry
    private final Map<Integer, OpenFileTableEntry> openFiles;

    // counter for generating unique file descriptors
    private int nextFileDescriptor;

    // optional: maximum number of open files (set to -1 for unlimited)
    private static final int MAX_OPEN_FILES = -1; // -1 means no limit

    // constructor
    public OpenFileTable() {
        this.openFiles = new HashMap<>();
        this.nextFileDescriptor = 0;
    }

    // file opening ---------------------------------

    // opens a file and creates a new open file table entry
    // param file the file to open
    // param mode the access mode for opening the file
    // return the file descriptor for this open instance
    // throws IllegalStateException if max open files reached
    public int open(File file, OpenFileTableEntry.AccessMode mode) {
        // check if we've reached the max open files limit
        if (MAX_OPEN_FILES > 0 && openFiles.size() >= MAX_OPEN_FILES) {
            throw new IllegalStateException("Maximum number of open files reached: " + MAX_OPEN_FILES);
        }

        // generate new file descriptor
        int fileDescriptor = nextFileDescriptor++;

        // create new entry
        OpenFileTableEntry entry = new OpenFileTableEntry(fileDescriptor, file, mode);

        // add to table
        openFiles.put(fileDescriptor, entry);

        return fileDescriptor;
    }

    // file closing ---------------------------------

    // closes a file by its file descriptor
    // param fd the file descriptor to close
    // return true if file was closed, false if fd was invalid or already closed
    public boolean close(int fd) {
        OpenFileTableEntry entry = openFiles.get(fd);

        if (entry == null) {
            return false; // invalid file descriptor
        }

        // mark entry as closed
        entry.close();

        // remove from table
        openFiles.remove(fd);

        return true;
    }

    // closes all open files
    // return number of files that were closed
    public int closeAll() {
        int closedCount = 0;

        // create a copy of keys to avoid ConcurrentModificationException
        Set<Integer> fds = new HashSet<>(openFiles.keySet());

        for (int fd : fds) {
            if (close(fd)) {
                closedCount++;
            }
        }

        return closedCount;
    }

    // lookup methods ---------------------------------

    // retrieves an open file table entry by file descriptor
    // param fd the file descriptor
    // return the OpenFileTableEntry, or null if not found
    public OpenFileTableEntry get(int fd) {
        return openFiles.get(fd);
    }

    // checks if a file descriptor is currently open
    // param fd the file descriptor to check
    // return true if the file descriptor exists and is open
    public boolean isOpen(int fd) {
        OpenFileTableEntry entry = openFiles.get(fd);
        return entry != null && entry.isOpen();
    }

    // finds all open file descriptors for a specific file
    // param file the file to search for
    // return list of file descriptors that have this file open
    public List<Integer> getOpenFilesForFile(File file) {
        List<Integer> result = new ArrayList<>();

        for (Map.Entry<Integer, OpenFileTableEntry> entry : openFiles.entrySet()) {
            if (entry.getValue().getFile().equals(file)) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    // checks if a specific file has any open instances
    // param file the file to check
    // return true if the file is open at least once
    public boolean isFileOpen(File file) {
        return !getOpenFilesForFile(file).isEmpty();
    }

    // query methods ---------------------------------

    // returns the number of currently open files
    public int getOpenCount() {
        return openFiles.size();
    }

    // returns a list of all open file descriptors
    public List<Integer> getAllFileDescriptors() {
        return new ArrayList<>(openFiles.keySet());
    }

    // checks if the table is empty (no open files)
    public boolean isEmpty() {
        return openFiles.isEmpty();
    }

    // returns the next file descriptor that will be assigned
    public int getNextFileDescriptor() {
        return nextFileDescriptor;
    }

    // utility methods ---------------------------------

    @Override
    public String toString() {
        return String.format("OpenFileTable[openFiles=%d, nextFD=%d]",
                openFiles.size(),
                nextFileDescriptor);
    }

    // returns detailed information about all open files
    public String getTableInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Open File Table ===\n");
        sb.append("Total Open Files: ").append(openFiles.size()).append("\n");
        sb.append("Next File Descriptor: ").append(nextFileDescriptor).append("\n");

        if (openFiles.isEmpty()) {
            sb.append("No files currently open.\n");
        } else {
            sb.append("\nOpen Files:\n");
            for (Map.Entry<Integer, OpenFileTableEntry> entry : openFiles.entrySet()) {
                sb.append("  FD ").append(entry.getKey()).append(": ")
                        .append(entry.getValue()).append("\n");
            }
        }

        return sb.toString();
    }

    // returns a summary of open files grouped by file name
    public String getOpenFilesSummary() {
        StringBuilder sb = new StringBuilder();
        Map<String, Integer> fileCount = new HashMap<>();

        // count how many times each file is open
        for (OpenFileTableEntry entry : openFiles.values()) {
            String fileName = entry.getFile().getName();
            fileCount.put(fileName, fileCount.getOrDefault(fileName, 0) + 1);
        }

        sb.append("Open Files Summary:\n");
        for (Map.Entry<String, Integer> entry : fileCount.entrySet()) {
            sb.append("  ").append(entry.getKey())
                    .append(": ").append(entry.getValue())
                    .append(" instance(s)\n");
        }

        return sb.toString();
    }
}