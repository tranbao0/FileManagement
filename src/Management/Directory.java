package Management;

import Model.File;
import java.util.*;
import java.util.stream.Collectors;

// maps file names to File objects
// manages the collection of files in the file system
public class Directory {

    // stores mapping from file name (lowercase) to File object
    private final Map<String, File> files;

    // constructor
    public Directory() {
        this.files = new HashMap<>();
    }

    // file management ---------------------------------

    // adds a file to the directory
    // param file the file to add
    // return true if added successfully, false if file with same name already exists
    public boolean addFile(File file) {
        if (file == null) {
            return false;
        }

        String key = file.getName().toLowerCase();

        // check for duplicate name
        if (files.containsKey(key)) {
            return false; // file with this name already exists
        }

        files.put(key, file);
        return true;
    }

    // removes a file from the directory by name
    // param name the name of the file to remove
    // return the removed File object, or null if not found
    public File removeFile(String name) {
        if (name == null) {
            return null;
        }

        String key = name.toLowerCase();
        return files.remove(key);
    }

    // renames a file in the directory
    // param oldName the current name of the file
    // param newName the new name for the file
    // return true if renamed successfully, false if old name not found or new name already exists
    public boolean renameFile(String oldName, String newName) {
        if (oldName == null || newName == null) {
            return false;
        }

        String oldKey = oldName.toLowerCase();
        String newKey = newName.toLowerCase();

        // check if old file exists
        File file = files.get(oldKey);
        if (file == null) {
            return false; // old name doesn't exist
        }

        // check if new name already exists (and it's not the same file)
        if (!oldKey.equals(newKey) && files.containsKey(newKey)) {
            return false; // new name already taken
        }

        // remove old entry
        files.remove(oldKey);

        // update file name
        file.setName(newName);

        // add with new name
        files.put(newKey, file);

        return true;
    }

    // removes all files from the directory
    public void clear() {
        files.clear();
    }

    // lookup methods ---------------------------------

    // retrieves a file by name (case-insensitive)
    // param name the name of the file
    // return the File object, or null if not found
    public File getFile(String name) {
        if (name == null) {
            return null;
        }

        String key = name.toLowerCase();
        return files.get(key);
    }

    // checks if a file with the given name exists
    // param name the name to check
    // return true if file exists
    public boolean containsFile(String name) {
        if (name == null) {
            return false;
        }

        String key = name.toLowerCase();
        return files.containsKey(key);
    }

    // alias for containsFile
    public boolean fileExists(String name) {
        return containsFile(name);
    }

    // listing methods ---------------------------------

    // returns all files in the directory
    // return list of all File objects
    public List<File> getAllFiles() {
        return new ArrayList<>(files.values());
    }

    // returns all file names
    // return list of file names
    public List<String> getFileNames() {
        return files.values().stream()
                .map(File::getName)
                .collect(Collectors.toList());
    }

    // returns only active (non-deleted) files
    // return list of active File objects
    public List<File> getActiveFiles() {
        return files.values().stream()
                .filter(file -> !file.isDeleted())
                .collect(Collectors.toList());
    }

    // returns only deleted files
    // return list of deleted File objects
    public List<File> getDeletedFiles() {
        return files.values().stream()
                .filter(File::isDeleted)
                .collect(Collectors.toList());
    }

    // search methods ---------------------------------

    // searches for files by name (case-insensitive partial match)
    // param query the search query
    // return list of files whose names contain the query
    public List<File> searchByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerQuery = query.toLowerCase();

        return files.values().stream()
                .filter(file -> file.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    // searches for files by tag
    // param tag the tag to search for
    // return list of files that have the specified tag
    public List<File> searchByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return files.values().stream()
                .filter(file -> file.hasTag(tag))
                .collect(Collectors.toList());
    }

    // searches for files by multiple tags (files must have ALL tags)
    // param tags the tags to search for
    // return list of files that have all specified tags
    public List<File> searchByTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        return files.values().stream()
                .filter(file -> {
                    for (String tag : tags) {
                        if (!file.hasTag(tag)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    // searches for files containing specific text in their content
    // param text the text to search for
    // return list of files whose content contains the text
    public List<File> searchByContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerText = text.toLowerCase();

        return files.values().stream()
                .filter(file -> file.getContent().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }

    // query methods ---------------------------------

    // returns the total number of files in the directory
    public int getFileCount() {
        return files.size();
    }

    // returns the number of active (non-deleted) files
    public int getActiveFileCount() {
        return (int) files.values().stream()
                .filter(file -> !file.isDeleted())
                .count();
    }

    // returns the number of deleted files
    public int getDeletedFileCount() {
        return (int) files.values().stream()
                .filter(File::isDeleted)
                .count();
    }

    // checks if the directory is empty
    public boolean isEmpty() {
        return files.isEmpty();
    }

    // utility methods ---------------------------------

    @Override
    public String toString() {
        return String.format("Directory[files=%d, active=%d, deleted=%d]",
                getFileCount(),
                getActiveFileCount(),
                getDeletedFileCount());
    }

    // returns detailed information about the directory
    public String getDirectoryInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Directory Information ===\n");
        sb.append("Total Files: ").append(getFileCount()).append("\n");
        sb.append("Active Files: ").append(getActiveFileCount()).append("\n");
        sb.append("Deleted Files: ").append(getDeletedFileCount()).append("\n");

        if (!files.isEmpty()) {
            sb.append("\nFiles:\n");
            for (File file : files.values()) {
                sb.append("  - ").append(file.getName());
                if (file.isDeleted()) {
                    sb.append(" [DELETED]");
                }
                sb.append(" (").append(file.getSize()).append(" bytes)");
                if (!file.getTags().isEmpty()) {
                    sb.append(" Tags: ").append(file.getTags());
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    // returns a list of files sorted by name
    public List<File> getFilesSortedByName() {
        return files.values().stream()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }

    // returns a list of files sorted by size (largest first)
    public List<File> getFilesSortedBySize() {
        return files.values().stream()
                .sorted(Comparator.comparingInt(File::getSize).reversed())
                .collect(Collectors.toList());
    }

    // returns a list of files sorted by modified time (newest first)
    public List<File> getFilesSortedByModifiedTime() {
        return files.values().stream()
                .sorted(Comparator.comparing(File::getModifiedTime).reversed())
                .collect(Collectors.toList());
    }
}