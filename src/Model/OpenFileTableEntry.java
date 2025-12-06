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

    // moves current position to specific byte offset
    // param position the new position (0-based)
    // return true if successful, false if position is invalid
    public boolean seek(int position) {
        if (position < 0) {
            return false;
        }

        this.currentPosition = position;
        return true;
    }

    // advances the current position by a specified number of bytes
    // param bytes number of bytes to advance
    // return true if successful, false if bytes is negative
    public boolean advance(int bytes) {
        if (bytes < 0) {
            return false;
        }

        this.currentPosition += bytes;
        return true;
    }

    // resets the current position to the start of the file (position 0)
    public void reset() {
        this.currentPosition = 0;
    }

    // checks if the current position is at or beyond the end of the file
    // return true if at end of file
    public boolean isAtEnd() {
        return currentPosition >= file.getSize();
    }

    // returns the number of bytes remaining from current position to end of file
    // return bytes remaining, or 0 if at or past end
    public int getBytesRemaining() {
        int remaining = file.getSize() - currentPosition;
        return Math.max(0, remaining);
    }

    // access validation ---------------------------------

    // checks if reading is allowed with the current access mode
    // return true if file can be read
    public boolean canRead() {
        return accessMode == AccessMode.READ_ONLY || accessMode == AccessMode.READ_WRITE;
    }

    // checks if writing is allowed with the current access mode
    // return true if file can be written
    public boolean canWrite() {
        return accessMode == AccessMode.WRITE_ONLY || accessMode == AccessMode.READ_WRITE;
    }

    // validates that the entry is open and can perform read operations
    // throws IllegalStateException if file is closed or not readable
    public void validateRead() {
        if (!isOpen) {
            throw new IllegalStateException("File descriptor " + fileDescriptor + " is closed");
        }
        if (!canRead()) {
            throw new IllegalStateException("File opened in write-only mode, cannot read");
        }
    }

    // validates that the entry is open and can perform write operations
    // throws IllegalStateException if file is closed or not writable
    public void validateWrite() {
        if (!isOpen) {
            throw new IllegalStateException("File descriptor " + fileDescriptor + " is closed");
        }
        if (!canWrite()) {
            throw new IllegalStateException("File opened in read-only mode, cannot write");
        }
    }

    // file operations ---------------------------------

    // closes this file table entry
    // once closed, the entry should not be used for further operations
    public void close() {
        this.isOpen = false;
    }

    // gets how long this file has been open
    // return Duration since the file was opened
    public Duration getOpenDuration() {
        return Duration.between(openedTime, LocalDateTime.now());
    }

    // returns a human-readable string of how long the file has been open
    public String getOpenDurationString() {
        Duration duration = getOpenDuration();
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else {
            return (seconds / 3600) + " hours";
        }
    }

    // utility methods ---------------------------------

    @Override
    public String toString() {
        return String.format("OpenFileTableEntry[fd=%d, file='%s', mode=%s, position=%d, open=%b]",
                fileDescriptor,
                file.getName(),
                accessMode,
                currentPosition,
                isOpen);
    }

    // returns detailed information about this open file entry
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("File Descriptor: ").append(fileDescriptor).append("\n");
        sb.append("File Name: ").append(file.getName()).append("\n");
        sb.append("Access Mode: ").append(accessMode).append("\n");
        sb.append("Current Position: ").append(currentPosition).append(" / ")
                .append(file.getSize()).append(" bytes\n");
        sb.append("Opened: ").append(openedTime).append("\n");
        sb.append("Open Duration: ").append(getOpenDurationString()).append("\n");
        sb.append("Status: ").append(isOpen ? "Open" : "Closed").append("\n");
        sb.append("Can Read: ").append(canRead()).append("\n");
        sb.append("Can Write: ").append(canWrite()).append("\n");
        sb.append("At End: ").append(isAtEnd()).append("\n");
        return sb.toString();
    }
}