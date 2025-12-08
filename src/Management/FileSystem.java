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
// NOW INTEGRATED WITH STORAGE FOR BLOCK-BASED FILE STORAGE
public class FileSystem {

    // core components
    private final Directory directory;
    private final OpenFileTable openFileTable;
    private final RecycleBin recycleBin;
    private final Storage storage;

    // statistics
    private int totalFilesCreated;
    private int totalOperations;

    // constructor with default storage (512-byte blocks, 2048 blocks = 1MB)
    public FileSystem() {
        this.directory = new Directory();
        this.openFileTable = new OpenFileTable();
        this.recycleBin = new RecycleBin();
        this.storage = new Storage(512, 2048);  // 1 MB virtual disk
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
    public boolean createFile(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (directory.containsFile(name)) {
            return false;
        }

        File file = new File(name);
        boolean added = directory.addFile(file);

        if (added) {
            totalFilesCreated++;
            totalOperations++;
        }

        return added;
    }

    // file opening and closing ---------------------------------

    public int openFile(String name, AccessMode mode) {
        if (name == null || mode == null) {
            return -1;
        }

        File file = directory.getFile(name);

        if (file == null || file.isDeleted()) {
            return -1;
        }

        int fd = openFileTable.open(file, mode);
        totalOperations++;

        return fd;
    }

    public boolean closeFile(int fd) {
        OpenFileTableEntry entry = openFileTable.get(fd);

        if (entry == null) {
            return false;
        }

        File file = entry.getFile();
        file.saveVersion();

        boolean closed = openFileTable.close(fd);

        if (closed) {
            totalOperations++;
        }

        return closed;
    }

    // file reading ---------------------------------

    public String read(int fd, int numBytes) {
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

        if (position >= content.length()) {
            return "";
        }

        int endPos = Math.min(position + numBytes, content.length());
        String result = content.substring(position, endPos);
        entry.advance(endPos - position);

        totalOperations++;
        return result;
    }

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

        String result = content.substring(position);
        entry.seek(content.length());

        totalOperations++;
        return result;
    }

    public String readFileContent(String name) {
        int fd = openFile(name, AccessMode.READ_ONLY);
        if (fd == -1) {
            return null;
        }

        String content = readAll(fd);
        closeFile(fd);

        return content;
    }

    // file writing - NOW USES STORAGE ---------------------------------

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
        String currentContent = file.getContentRaw();

        String before = position <= currentContent.length() ? currentContent.substring(0, position) : currentContent;
        String after = position + data.length() < currentContent.length() ? currentContent.substring(position + data.length()) : "";

        String newContent = before + data + after;

        // Update file content and storage
        updateFileStorage(file, newContent);

        entry.advance(data.length());

        totalOperations++;
        return true;
    }

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
        String newContent = file.getContentRaw() + data;

        // Update file content and storage
        updateFileStorage(file, newContent);

        entry.seek(file.getSize());

        totalOperations++;
        return true;
    }

    public boolean writeFileContent(String name, String content) {
        int fd = openFile(name, AccessMode.READ_WRITE);
        if (fd == -1) {
            return false;
        }

        File file = directory.getFile(name);
        if (file != null) {
            updateFileStorage(file, content);
        }

        closeFile(fd);
        return true;
    }

    // STORAGE INTEGRATION METHODS ---------------------------------

    // Updates file content and allocates/deallocates storage blocks
    private void updateFileStorage(File file, String newContent) {
        // Deallocate old blocks
        int[] oldBlocks = file.getAllocatedBlocks();
        if (oldBlocks != null) {
            storage.deallocateBlocks(oldBlocks);
        }

        // Calculate how many blocks we need
        byte[] contentBytes = newContent.getBytes();
        int blocksNeeded = (int) Math.ceil((double) contentBytes.length / storage.getBlockSize());

        if (blocksNeeded > 0) {
            // Allocate new blocks
            int[] newBlocks = storage.allocateBlocks(blocksNeeded);

            if (newBlocks != null) {
                // Write content to storage blocks
                storage.writeMultiBlock(newBlocks, contentBytes);
                file.setAllocatedBlocks(newBlocks);
            } else {
                // Not enough space - this is a problem, but we'll still update content
                System.err.println("Warning: Not enough storage space for file: " + file.getName());
                file.setAllocatedBlocks(null);
            }
        } else {
            file.setAllocatedBlocks(null);
        }

        // Update file content (cached)
        file.setContent(newContent);
    }

    // Reads content from storage blocks (verifies storage integrity)
    public String readFromStorage(File file) {
        int[] blocks = file.getAllocatedBlocks();
        if (blocks == null || blocks.length == 0) {
            return "";
        }

        byte[] data = storage.readMultiBlock(blocks);
        if (data == null) {
            return file.getContentRaw();  // fallback to cached
        }

        // Trim to actual file size (blocks may have padding)
        int actualSize = file.getSize();
        if (actualSize < data.length) {
            data = Arrays.copyOf(data, actualSize);
        }

        return new String(data);
    }

    // file operations ---------------------------------

    public boolean deleteFile(String name) {
        if (name == null) {
            return false;
        }

        File file = directory.getFile(name);

        if (file == null) {
            return false;
        }

        if (openFileTable.isFileOpen(file)) {
            List<Integer> fds = openFileTable.getOpenFilesForFile(file);
            for (int fd : fds) {
                closeFile(fd);
            }
        }

        // Note: We keep the storage blocks allocated for deleted files
        // so they can be restored. Permanent delete will free them.

        directory.removeFile(name);
        recycleBin.addFile(file);

        totalOperations++;
        return true;
    }

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

    public boolean copyFile(String sourceName, String destName) {
        if (sourceName == null || destName == null) {
            return false;
        }

        File source = directory.getFile(sourceName);

        if (source == null || source.isDeleted()) {
            return false;
        }

        if (directory.containsFile(destName)) {
            return false;
        }

        File copy = new File(destName);

        // Copy content and allocate new storage blocks
        updateFileStorage(copy, source.getContentRaw());

        for (String tag : source.getTags()) {
            copy.addTag(tag);
        }

        boolean added = directory.addFile(copy);

        if (added) {
            totalFilesCreated++;
            totalOperations++;
        }

        return added;
    }

    public boolean fileExists(String name) {
        File file = directory.getFile(name);
        return file != null && !file.isDeleted();
    }

    // tag management ---------------------------------

    public boolean addTag(String fileName, String tag) {
        File file = directory.getFile(fileName);

        if (file == null || file.isDeleted()) {
            return false;
        }

        return file.addTag(tag);
    }

    public boolean removeTag(String fileName, String tag) {
        File file = directory.getFile(fileName);

        if (file == null) {
            return false;
        }

        return file.removeTag(tag);
    }

    public List<File> searchByTag(String tag) {
        return directory.searchByTag(tag);
    }

    // version management ---------------------------------

    public List<FileVersion> listVersions(String fileName) {
        File file = directory.getFile(fileName);

        if (file == null) {
            return null;
        }

        return file.getVersionHistory();
    }

    public boolean restoreVersion(String fileName, int versionNumber) {
        File file = directory.getFile(fileName);

        if (file == null || file.isDeleted()) {
            return false;
        }

        if (openFileTable.isFileOpen(file)) {
            return false;
        }

        boolean restored = file.restoreVersion(versionNumber);

        if (restored) {
            // Update storage with restored content
            updateFileStorage(file, file.getContentRaw());
            totalOperations++;
        }

        return restored;
    }

    // recycle bin operations ---------------------------------

    public List<File> listDeletedFiles() {
        return recycleBin.getAllDeletedFiles();
    }

    public boolean restoreFromBin(String name) {
        if (name == null) {
            return false;
        }

        if (directory.containsFile(name)) {
            return false;
        }

        File file = recycleBin.restore(name);

        if (file == null) {
            return false;
        }

        boolean added = directory.addFile(file);

        if (added) {
            totalOperations++;
        }

        return added;
    }

    public boolean permanentDelete(String name) {
        // First get the file to free its storage blocks
        File file = recycleBin.getFile(name);
        if (file != null) {
            int[] blocks = file.getAllocatedBlocks();
            if (blocks != null) {
                storage.deallocateBlocks(blocks);
            }
        }

        boolean deleted = recycleBin.permanentDelete(name);

        if (deleted) {
            totalOperations++;
        }

        return deleted;
    }

    public int emptyRecycleBin() {
        // Free all storage blocks from deleted files
        List<File> deletedFiles = recycleBin.getAllDeletedFiles();
        for (File file : deletedFiles) {
            int[] blocks = file.getAllocatedBlocks();
            if (blocks != null) {
                storage.deallocateBlocks(blocks);
            }
        }

        int count = recycleBin.empty();

        if (count > 0) {
            totalOperations++;
        }

        return count;
    }

    // search operations ---------------------------------

    public List<File> searchByName(String query) {
        return directory.searchByName(query);
    }

    public List<File> searchByContent(String text) {
        return directory.searchByContent(text);
    }

    public List<File> listFiles() {
        return directory.getActiveFiles();
    }

    public List<File> listAllFiles() {
        return directory.getAllFiles();
    }

    // query methods ---------------------------------

    public String getFileInfo(String name) {
        File file = directory.getFile(name);

        if (file == null) {
            return null;
        }

        return file.getDetailedInfo();
    }

    public int getFileCount() {
        return directory.getActiveFileCount();
    }

    public int getDeletedFileCount() {
        return recycleBin.getDeletedCount();
    }

    public int getOpenFileCount() {
        return openFileTable.getOpenCount();
    }

    public int getTotalFilesCreated() {
        return totalFilesCreated;
    }

    public int getTotalOperations() {
        return totalOperations;
    }

    // STORAGE STATISTICS ---------------------------------

    public int getStorageUsedBlocks() {
        return storage.getUsedBlockCount();
    }

    public int getStorageFreeBlocks() {
        return storage.getFreeBlockCount();
    }

    public int getStorageTotalBlocks() {
        return storage.getTotalBlockCount();
    }

    public int getStorageBlockSize() {
        return storage.getBlockSize();
    }

    public int getStorageTotalSize() {
        return storage.getTotalSize();
    }

    public int getStorageUsedSize() {
        return storage.getUsedBlockCount() * storage.getBlockSize();
    }

    public int getStorageFreeSize() {
        return storage.getFreeBlockCount() * storage.getBlockSize();
    }

    public double getStorageUsagePercentage() {
        return storage.getUsagePercentage();
    }

    public String getStorageInfo() {
        return storage.getStorageInfo();
    }

    // utility methods ---------------------------------

    @Override
    public String toString() {
        return String.format("FileSystem[files=%d, open=%d, deleted=%d, storage=%.1f%% used]",
                getFileCount(),
                getOpenFileCount(),
                getDeletedFileCount(),
                getStorageUsagePercentage());
    }

    public String getSystemInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== File System Information ===\n\n");

        sb.append("--- Statistics ---\n");
        sb.append("Active Files: ").append(getFileCount()).append("\n");
        sb.append("Open Files: ").append(getOpenFileCount()).append("\n");
        sb.append("Deleted Files: ").append(getDeletedFileCount()).append("\n");
        sb.append("Total Files Created: ").append(totalFilesCreated).append("\n");
        sb.append("Total Operations: ").append(totalOperations).append("\n\n");

        sb.append("--- Storage ---\n");
        sb.append("Block Size: ").append(storage.getBlockSize()).append(" bytes\n");
        sb.append("Total Blocks: ").append(storage.getTotalBlockCount()).append("\n");
        sb.append("Used Blocks: ").append(storage.getUsedBlockCount()).append("\n");
        sb.append("Free Blocks: ").append(storage.getFreeBlockCount()).append("\n");
        sb.append("Total Size: ").append(formatSize(storage.getTotalSize())).append("\n");
        sb.append("Used Size: ").append(formatSize(getStorageUsedSize())).append("\n");
        sb.append("Free Size: ").append(formatSize(getStorageFreeSize())).append("\n");
        sb.append("Usage: ").append(String.format("%.2f%%", storage.getUsagePercentage())).append("\n\n");

        sb.append("--- Directory ---\n");
        sb.append(directory.toString()).append("\n\n");

        sb.append("--- Open File Table ---\n");
        sb.append(openFileTable.toString()).append("\n\n");

        sb.append("--- Recycle Bin ---\n");
        sb.append(recycleBin.toString()).append("\n");

        return sb.toString();
    }

    // Helper to format sizes nicely
    private String formatSize(int bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return bytes + " bytes";
        }
    }

    // getters for components

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