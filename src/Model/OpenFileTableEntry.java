package Model;

import java.time.LocalDateTime;
import java.time.Duration;

// the open file instance in the file system
// tracks reference, access, current position, and file descriptor
public class OpenFileTableEntry {

    // enum representing file access modes
    public enum AccessMode {
        READ_ONLY,
        WRITE_ONLY,
        READ_WRITE,
    }

    // fields
    private final int fileDescriptor;              // id for this open instance
    private final File file;                       // reference to the file object
    private final AccessMode accessMode;           // how the file was opened
    private final LocalDateTime openedTime;        // when the file was opened

    // state tracking
    private int currentPosition;                   // current read/write position
    private boolean isOpen;                        // whether this file is still active

    public OpenFileTableEntry(int fileDescriptor, File file, AccessMode accessMode) {
        this.fileDescriptor = fileDescriptor;
        this.file = file;
        this.accessMode = accessMode;
        this.openedTime = LocalDateTime.now();
        this.currentPosition = 0;
        this.isOpen = true;
    }

    // getters ------------------------------------

    public int getFileDescriptor() {
        return fileDescriptor;
    }

    public File getFile() {
        return file;
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public LocalDateTime getOpenedTime() {
        return openedTime;
    }

    public boolean isOpen() {
        return isOpen;
    }

    // position management ---------------------------------

    public boolean seek(int position) {
    }
}
