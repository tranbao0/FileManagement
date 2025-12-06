package Management;

import Model.File;
import Model.FileVersion;
import Model.OpenFileTableEntry;
import Model.OpenFileTableEntry.AccessMode;
import Storage.Storage;
import java.util.*;

// main orchestrator of the file management system
// coordinates directory, open file table, recycle bin, and storage
// provides high-level API for all file operations
public class FileSystem {

    // core components
    private final Directory directory;
    private final OpenFileTable openFileTable;
    private final RecycleBin recycleBin;
    private final Storage storage;

    // statistics
    private int totalFilesCreated;
    private int totalOperations;

    // constructor
    public FileSystem() {
        this.directory = new Directory();
        this.openFileTable = new OpenFileTable();
        this.recycleBin = new RecycleBin();
        this.storage = new Storage(); // 512-byte blocks, 2048 blocks (1 MB)
        this.totalFilesCreated = 0;
        this.totalOperations = 0;
    }

    // constructor with custom storage
    public FileSystem(int blockSize, int numBlocks) {
        this.directory = new Directory();
        this.openFileTable = new OpenFileTable();
        this.recycleBin = new RecycleBin();
        this.storage = new Storage(blockSize, numBlocks);
        this.totalFilesCreated = 0;
        this.totalOperations = 0;
    }

    // file creation ---------------------------------

    // creates a new file with the given name
    // param name the name of the file to create
    // return true if created successfully, false if file already exists or name is invalid
    public boolean createFile(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // check if file already exists
        if (directory.containsFile(name)) {
            return false;
        }

        // create new file
        File file = new File(name);

        // add to directory
        boolean added = directory.addFile(file);

        if (added) {
            totalFilesCreated++;
            totalOperations++;
        }

        return added;
    }

    // file opening and closing ---------------------------------

    // opens a file for reading and/or writing
    // param name the name of the file to open
    // param mode the access mode (READ_ONLY, WRITE_ONLY, READ_WRITE)
    // return file descriptor if successful, -1 if file not found or is deleted
    public int openFile(String name, AccessMode mode) {
        if (name == null || mode == null) {
            return -1;
        }

        // get file from directory
        File file = directory.getFile(name);

        if (file == null) {
            return -1; // file not found
        }

        if (file.isDeleted()) {
            return -1; // cannot open deleted file
        }

        // open file in the open file table
        int fd = openFileTable.open(file, mode);
        totalOperations++;

        return fd;
    }

    // closes an open file and saves a version
    // param fd the file descriptor to close
    // return true if closed successfully, false if fd is invalid
    public boolean closeFile(int fd) {
        OpenFileTableEntry entry = openFileTable.get(fd);

        if (entry == null) {
            return false; // invalid file descriptor
        }

        // save version before closing
        File file = entry.getFile();
        file.saveVersion();

        // close the file
        boolean closed = openFileTable.close(fd);

        if (closed) {
            totalOperations++;
        }

        return closed;
    }

    // file reading ---------------------------------

    // reads a specified number of bytes from the current position
    // param fd the file descriptor
    // param numBytes number of bytes to read
    // return the content read, or null if error
    public String read(int fd, int numBytes) {
        OpenFileTableEntry entry = openFileTable.get(fd);

        if (entry == null) {
            return null; // invalid file descriptor
        }

        try {
            entry.validateRead();
        } catch (IllegalStateException e) {
            return null; // cannot read
        }

        File file = entry.getFile();
        String content = file.getContent();
        int position = entry.getCurrentPosition();

        // check bounds
        if (position >= content.length()) {
            return ""; // at end of file
        }

        // calculate end position
        int endPos = Math.min(position + numBytes, content.length());

        // extract substring
        String result = content.substring(position, endPos);

        // advance position
        entry.advance(endPos - position);

        totalOperations++;
        return result;
    }

    // reads the entire file from current position to end
    // param fd the file descriptor
    // return the content from current position, or null if error
    public String readAll(int fd) {
        OpenFileTableEntry entry = openFileTable.get(fd);

        if (entry == null) {
            return null;
        }

        try {
            entry.validateRead();
        } catch (IllegalStateException e) {
            return null;
        }

        File file = entry.getFile();
        String content = file.getContent();
        int position = entry.getCurrentPosition();

        // read from current position to end
        String result = content.substring(position);

        // move position to end
        entry.seek(content.length());

        totalOperations++;
        return result;
    }

    // convenience method: reads entire file content without opening/closing
    // param name the name of the file
    // return the file content, or null if file not found
    public String readFileContent(String name) {
        int fd = openFile(name, AccessMode.READ_ONLY);
        if (fd == -1) {
            return null;
        }

        String content = readAll(fd);
        closeFile(fd);

        return content;
    }

    // file writing ---------------------------------

    // writes data to the file at the current position (overwrites existing content)
    // param fd the file descriptor
    // param data the data to write
    // return true if successful, false if error
    public boolean write(int fd, String data) {
        if (data == null) {
            return false;
        }

        OpenFileTableEntry entry = openFileTable.get(fd);

        if (entry == null) {
            return false;
        }

        try {
            entry.validateWrite();
        } catch (IllegalStateException e) {
            return false;
        }

        File file = entry.getFile();
        int position = entry.getCurrentPosition();
        String currentContent = file.getContent();

        // build new content
        String before = position <= currentContent.length() ? currentContent.substring(0, position) : currentContent;
        String after = position + data.length() < currentContent.length() ? currentContent.substring(position + data.length()) : "";

        String newContent = before + data + after;

        // update file content
        file.setContent(newContent);

        // advance position
        entry.advance(data.length());

        totalOperations++;
        return true;
    }

    // appends data to the end of the file
    // param fd the file descriptor
    // param data the data to append
    // return true if successful, false if error
    public boolean append(int fd, String data) {
        if (data == null) {
            return false;
        }

        OpenFileTableEntry entry = openFileTable.get(fd);

        if (entry == null) {
            return false;
        }

        try {
            entry.validateWrite();
        } catch (IllegalStateException e) {
            return false;
        }

        File file = entry.getFile();
        file.appendContent(data);

        // move position to end
        entry.seek(file.getSize());

        totalOperations++;
        return true;
    }

    // convenience method: writes content to file without opening/closing
    // param name the name of the file
    // param content the content to write
    // return true if successful
    public boolean writeFileContent(String name, String content) {
        int fd = openFile(name, AccessMode.READ_WRITE);
        if (fd == -1) {
            return false;
        }

        // clear existing content and write new content
        File file = directory.getFile(name);
        if (file != null) {
            file.setContent(content);
        }

        closeFile(fd);
        return true;
    }

    // file operations ---------------------------------

    // deletes a file (moves to recycle bin)
    // param name the name of the file to delete
    // return true if deleted successfully, false if file not found
    public boolean deleteFile(String name) {
        if (name == null) {
            return false;
        }

        // get file from directory
        File file = directory.getFile(name);

        if (file == null) {
            return false; // file not found
        }

        // check if file is open
        if (openFileTable.isFileOpen(file)) {
            // close all instances of this file
            List<Integer> fds = openFileTable.getOpenFilesForFile(file);
            for (int fd : fds) {
                closeFile(fd);
            }
        }

        // remove from directory
        directory.removeFile(name);

        // add to recycle bin
        recycleBin.addFile(file);

        totalOperations++;
        return true;
    }

    // renames a file
    // param oldName the current name of the file
    // param newName the new name for the file
    // return true if renamed successfully
    public boolean renameFile(String oldName, String newName) {
        if (oldName == null || newName == null) {
            return false;
        }

        boolean renamed = directory.renameFile(oldName, newName);

        if (renamed) {
            totalOperations++;
        }

        return renamed;
    }

    // creates a copy of a file with a new name
    // param sourceName the name of the file to copy
    // param destName the name for the copy
    // return true if copied successfully
    public boolean copyFile(String sourceName, String destName) {
        if (sourceName == null || destName == null) {
            return false;
        }

        // get source file
        File source = directory.getFile(sourceName);

        if (source == null || source.isDeleted()) {
            return false;
        }

        // check if destination already exists
        if (directory.containsFile(destName)) {
            return false;
        }

        // create new file
        File copy = new File(destName);
        copy.setContent(source.getContent());

        // copy tags
        for (String tag : source.getTags()) {
            copy.addTag(tag);
        }

        // add to directory
        boolean added = directory.addFile(copy);

        if (added) {
            totalFilesCreated++;
            totalOperations++;
        }

        return added;
    }

    // checks if a file exists
    // param name the name of the file
    // return true if file exists and is not deleted
    public boolean fileExists(String name) {
        File file = directory.getFile(name);
        return file != null && !file.isDeleted();
    }

    // tag management ---------------------------------

    // adds a tag to a file
    // param fileName the name of the file
    // param tag the tag to add
    // return true if added successfully
    public boolean addTag(String fileName, String tag) {
        File file = directory.getFile(fileName);

        if (file == null || file.isDeleted()) {
            return false;
        }

        return file.addTag(tag);
    }

    // removes a tag from a file
    // param fileName the name of the file
    // param tag the tag to remove
    // return true if removed successfully
    public boolean removeTag(String fileName, String tag) {
        File file = directory.getFile(fileName);

        if (file == null) {
            return false;
        }

        return file.removeTag(tag);
    }

    // searches for files with a specific tag
    // param tag the tag to search for
    // return list of files with the tag
    public List<File> searchByTag(String tag) {
        return directory.searchByTag(tag);
    }

    // version management ---------------------------------

    // lists all versions of a file
    // param fileName the name of the file
    // return list of file versions, or null if file not found
    public List<FileVersion> listVersions(String fileName) {
        File file = directory.getFile(fileName);

        if (file == null) {
            return null;
        }

        return file.getVersionHistory();
    }

    // restores a file to a previous version
    // param fileName the name of the file
    // param versionNumber the version number to restore
    // return true if restored successfully
    public boolean restoreVersion(String fileName, int versionNumber) {
        File file = directory.getFile(fileName);

        if (file == null || file.isDeleted()) {
            return false;
        }

        // check if file is open
        if (openFileTable.isFileOpen(file)) {
            return false; // cannot restore while file is open
        }

        boolean restored = file.restoreVersion(versionNumber);

        if (restored) {
            totalOperations++;
        }

        return restored;
    }

    // recycle bin operations ---------------------------------

    // lists all deleted files in the recycle bin
    // return list of deleted files
    public List<File> listDeletedFiles() {
        return recycleBin.getAllDeletedFiles();
    }

    // restores a file from the recycle bin
    // param name the name of the file to restore
    // return true if restored successfully
    public boolean restoreFromBin(String name) {
        if (name == null) {
            return false;
        }

        // check if a file with this name already exists in directory
        if (directory.containsFile(name)) {
            return false; // cannot restore, name already taken
        }

        // restore from bin
        File file = recycleBin.restore(name);

        if (file == null) {
            return false; // file not in recycle bin
        }

        // add back to directory
        boolean added = directory.addFile(file);

        if (added) {
            totalOperations++;
        }

        return added;
    }

    // permanently deletes a file from the recycle bin
    // param name the name of the file to permanently delete
    // return true if deleted successfully
    public boolean permanentDelete(String name) {
        boolean deleted = recycleBin.permanentDelete(name);

        if (deleted) {
            totalOperations++;
        }

        return deleted;
    }

    // empties the recycle bin (permanently deletes all files)
    // return number of files deleted
    public int emptyRecycleBin() {
        int count = recycleBin.empty();

        if (count > 0) {
            totalOperations++;
        }

        return count;
    }

    // search operations ---------------------------------

    // searches for files by name (partial match)
    // param query the search query
    // return list of files matching the query
    public List<File> searchByName(String query) {
        return directory.searchByName(query);
    }

    // searches for files by content
    // param text the text to search for
    // return list of files containing the text
    public List<File> searchByContent(String text) {
        return directory.searchByContent(text);
    }

    // lists all active files
    // return list of all non-deleted files
    public List<File> listFiles() {
        return directory.getActiveFiles();
    }

    // lists all files (including deleted)
    // return list of all files
    public List<File> listAllFiles() {
        return directory.getAllFiles();
    }

    // query methods ---------------------------------

    // gets detailed information about a file
    // param name the name of the file
    // return file info string, or null if not found
    public String getFileInfo(String name) {
        File file = directory.getFile(name);

        if (file == null) {
            return null;
        }

        return file.getDetailedInfo();
    }

    // returns the number of active files
    public int getFileCount() {
        return directory.getActiveFileCount();
    }

    // returns the number of deleted files
    public int getDeletedFileCount() {
        return recycleBin.getDeletedCount();
    }

    // returns the number of currently open files
    public int getOpenFileCount() {
        return openFileTable.getOpenCount();
    }

    // returns total number of files created since system start
    public int getTotalFilesCreated() {
        return totalFilesCreated;
    }

    // returns total number of operations performed
    public int getTotalOperations() {
        return totalOperations;
    }

    // utility methods ---------------------------------

    @Override
    public String toString() {
        return String.format("FileSystem[files=%d, open=%d, deleted=%d, operations=%d]",
                getFileCount(),
                getOpenFileCount(),
                getDeletedFileCount(),
                totalOperations);
    }

    // returns comprehensive system information
    public String getSystemInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== File System Information ===\n\n");

        sb.append("--- Statistics ---\n");
        sb.append("Active Files: ").append(getFileCount()).append("\n");
        sb.append("Open Files: ").append(getOpenFileCount()).append("\n");
        sb.append("Deleted Files: ").append(getDeletedFileCount()).append("\n");
        sb.append("Total Files Created: ").append(totalFilesCreated).append("\n");
        sb.append("Total Operations: ").append(totalOperations).append("\n\n");

        sb.append("--- Directory ---\n");
        sb.append(directory.toString()).append("\n\n");

        sb.append("--- Open File Table ---\n");
        sb.append(openFileTable.toString()).append("\n\n");

        sb.append("--- Recycle Bin ---\n");
        sb.append(recycleBin.toString()).append("\n\n");

        sb.append("--- Storage ---\n");
        sb.append(storage.toString()).append("\n");

        return sb.toString();
    }

    // getters for components (for advanced use)

    public Directory getDirectory() {
        return directory;
    }

    public OpenFileTable getOpenFileTable() {
        return openFileTable;
    }

    public RecycleBin getRecycleBin() {
        return recycleBin;
    }

    public Storage getStorage() {
        return storage;
    }
}