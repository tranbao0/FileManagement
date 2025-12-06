package Management;

import Model.File;
import java.util.*;
import java.util.stream.Collectors;

// manages soft-deleted files
// provides restoration and permanent deletion functionality
public class RecycleBin {

    // stores files that have been deleted
    private final Map<String, File> deletedFiles;

    // optional: maximum number of files in recycle bin (-1 for unlimited)
    private static final int MAX_FILES = -1;

    // constructor
    public RecycleBin() {
        this.deletedFiles = new HashMap<>();
    }

    // deletion methods ---------------------------------

    // adds a file to the recycle bin
    // param file the file to add
    // return true if added successfully, false if null or already exists
    public boolean addFile(File file) {
        if (file == null) {
            return false;
        }

        // check size limit
        if (MAX_FILES > 0 && deletedFiles.size() >= MAX_FILES) {
            return false; // bin is full
        }

        String key = file.getName().toLowerCase();

        // mark file as deleted
        file.markAsDeleted();

        // add to bin (overwrites if same name exists)
        deletedFiles.put(key, file);

        return true;
    }

    // empties the recycle bin (permanently deletes all files)
    // return number of files that were deleted
    public int empty() {
        int count = deletedFiles.size();
        deletedFiles.clear();
        return count;
    }

    // restoration methods ---------------------------------

    // restores a file from the recycle bin by name
    // param name the name of the file to restore
    // return the restored File object, or null if not found
    public File restore(String name) {
        if (name == null) {
            return null;
        }

        String key = name.toLowerCase();
        File file = deletedFiles.get(key);

        if (file == null) {
            return null; // file not in recycle bin
        }

        // remove from bin
        deletedFiles.remove(key);

        // mark as not deleted
        file.restore();

        return file;
    }

    // restores all files from the recycle bin
    // return list of all restored files
    public List<File> restoreAll() {
        List<File> restoredFiles = new ArrayList<>();

        // create copy of keys to avoid ConcurrentModificationException
        Set<String> keys = new HashSet<>(deletedFiles.keySet());

        for (String key : keys) {
            File file = restore(key);
            if (file != null) {
                restoredFiles.add(file);
            }
        }

        return restoredFiles;
    }

    // checks if a file with the given name is in the recycle bin
    // param name the name to check
    // return true if file is in the bin
    public boolean contains(String name) {
        if (name == null) {
            return false;
        }

        String key = name.toLowerCase();
        return deletedFiles.containsKey(key);
    }

    // permanent deletion methods ---------------------------------

    // permanently deletes a file from the recycle bin
    // param name the name of the file to permanently delete
    // return true if deleted, false if not found
    public boolean permanentDelete(String name) {
        if (name == null) {
            return false;
        }

        String key = name.toLowerCase();
        File removed = deletedFiles.remove(key);

        return removed != null;
    }

    // permanently deletes all files from the recycle bin
    // alias for empty()
    public int permanentDeleteAll() {
        return empty();
    }

    // listing methods ---------------------------------

    // returns all deleted files in the recycle bin
    // return list of all deleted File objects
    public List<File> getAllDeletedFiles() {
        return new ArrayList<>(deletedFiles.values());
    }

    // returns all deleted file names
    // return list of deleted file names
    public List<String> getDeletedFileNames() {
        return deletedFiles.values().stream()
                .map(File::getName)
                .collect(Collectors.toList());
    }

    // searches for deleted files by name (partial match)
    // param query the search query
    // return list of files whose names contain the query
    public List<File> searchDeleted(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerQuery = query.toLowerCase();

        return deletedFiles.values().stream()
                .filter(file -> file.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    // searches deleted files by tag
    // param tag the tag to search for
    // return list of deleted files with the specified tag
    public List<File> searchDeletedByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return deletedFiles.values().stream()
                .filter(file -> file.hasTag(tag))
                .collect(Collectors.toList());
    }

    // query methods ---------------------------------

    // returns the number of files in the recycle bin
    public int getDeletedCount() {
        return deletedFiles.size();
    }

    // checks if the recycle bin is empty
    public boolean isEmpty() {
        return deletedFiles.isEmpty();
    }

    // returns the total size of all deleted files in bytes
    public int getTotalSize() {
        return deletedFiles.values().stream()
                .mapToInt(File::getSize)
                .sum();
    }

    // returns the file with the given name without removing it
    // param name the name of the file
    // return the File object, or null if not found
    public File getFile(String name) {
        if (name == null) {
            return null;
        }

        String key = name.toLowerCase();
        return deletedFiles.get(key);
    }

    // utility methods ---------------------------------

    @Override
    public String toString() {
        return String.format("RecycleBin[files=%d, size=%d bytes]",
                getDeletedCount(),
                getTotalSize());
    }

    // returns detailed information about the recycle bin
    public String getRecycleBinInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Recycle Bin Information ===\n");
        sb.append("Total Files: ").append(getDeletedCount()).append("\n");
        sb.append("Total Size: ").append(getTotalSize()).append(" bytes");

        if (getTotalSize() >= 1024) {
            sb.append(" (").append(getTotalSize() / 1024).append(" KB)");
        }
        sb.append("\n");

        if (!deletedFiles.isEmpty()) {
            sb.append("\nDeleted Files:\n");
            for (File file : deletedFiles.values()) {
                sb.append("  - ").append(file.getName())
                        .append(" (").append(file.getSize()).append(" bytes)")
                        .append(" Modified: ").append(file.getModifiedTime())
                        .append("\n");
            }
        } else {
            sb.append("\nRecycle bin is empty.\n");
        }

        return sb.toString();
    }

    // returns a list of deleted files sorted by name
    public List<File> getFilesSortedByName() {
        return deletedFiles.values().stream()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }

    // returns a list of deleted files sorted by size (largest first)
    public List<File> getFilesSortedBySize() {
        return deletedFiles.values().stream()
                .sorted(Comparator.comparingInt(File::getSize).reversed())
                .collect(Collectors.toList());
    }

    // returns a list of deleted files sorted by deletion time (modified time, newest first)
    public List<File> getFilesSortedByDeletionTime() {
        return deletedFiles.values().stream()
                .sorted(Comparator.comparing(File::getModifiedTime).reversed())
                .collect(Collectors.toList());
    }
}