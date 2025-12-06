package Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// snapshot of files' content at specific point in time; immutable
public class FileVersion implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String content;
    private final LocalDateTime timestamp;
    private final int versionNumber;

    // creates a file version with given content and version number
    // Timestamp is set to current time
    // @param content the file content at this version
    // @param versionNumber the sequential version number (1, 2, 3, ...)
    public FileVersion(String content, int versionNumber) {
        this.content = content;
        this.versionNumber = versionNumber;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor that allows specifying custom timestamp
    // @param timestamp  the time this version was created in LocalDataTime format
    public FileVersion(String content, int versionNumber, LocalDateTime timestamp) {
        this.content = content;
        this.versionNumber = versionNumber;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    // returns size of this version's content in bytes
    public int getSize() {
        return content.length();
    }

    // returns a formatted timestamp string for display purposes
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    // returns formatted metadata string of this class
    @Override
    public String toString() {
        return String.format("Version %d (created: %s, size: %d bytes)", versionNumber, timestamp, getSize());
    }

    // creates a copy of this version
    public FileVersion copy() {
        return new FileVersion(this.content, this.versionNumber, this.timestamp);
    }
}
