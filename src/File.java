import java.time.LocalDateTime;
import java.util.*;

// simulated representation of a file in the file management system
// contains metadata, content, tags, and version history.
public class File {
    // metadata
    private String name;
    private String content;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private LocalDateTime accessedTime;

    // organization
    private Set<String> tags;
    private boolean isDeleted;

    // version control
    private List<FileVersion> versionHistory;
    private static final int MAX_VERSIONS = 5;

    // storage
    private Integer contentPointer;

    // constructor for a fresh file
    public File(String name) {
        this.name = name;
        this.content = "";
        this.createdTime = LocalDateTime.now();
        this.modifiedTime = LocalDateTime.now();
        this.accessedTime = LocalDateTime.now();
        this.tags = new HashSet<>();
        this.isDeleted = false;
        this.versionHistory = new ArrayList<>();
        this.contentPointer = null;
    }

    // constructor for loading files from disk with all metadata
    public File(String name, String content, LocalDateTime createdTime,
                LocalDateTime modifiedTime, LocalDateTime accessedTime,
                Set<String> tags, boolean isDeleted, List<FileVersion> versionHistory) {
        this.name = name;
        this.content = content;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.accessedTime = accessedTime;
        this.tags = new HashSet<>(tags);
        this.isDeleted = isDeleted;
        this.versionHistory = new ArrayList<>(versionHistory);
        this.contentPointer = null;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        updateAccessedTime();
        return content;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public LocalDateTime getAccessedTime() {
        return accessedTime;
    }

    public Set<String> getTags() {
        return tags;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public List<FileVersion> getVersionHistory() {
        return versionHistory;
    }

    public Integer getContentPointer() {
        return contentPointer;
    }

    public void updateAccessedTime() {
        this.accessedTime = LocalDateTime.now();
    }
}
