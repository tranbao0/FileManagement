package Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

// simulated representation of a file in the file management system
// contains metadata, content pointer to storage blocks, tags, and version history.
public class File implements Serializable {
    private static final long serialVersionUID = 2L;  // Updated version for new structure

    // metadata
    private String name;
    private int size;  // size in bytes (calculated from content)
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private LocalDateTime accessedTime;

    // organization
    private Set<String> tags;
    private boolean isDeleted;

    // version control
    private List<FileVersion> versionHistory;
    private static final int MAX_VERSIONS = 5;
    private int nextVersionNumber;

    // storage - now actually used!
    private int[] allocatedBlocks;  // array of block IDs where content is stored
    private String cachedContent;   // cached content for quick access (mirrors storage)

    // constructor for a fresh file
    public File(String name) {
        this.name = name;
        this.size = 0;
        this.createdTime = LocalDateTime.now();
        this.modifiedTime = LocalDateTime.now();
        this.accessedTime = LocalDateTime.now();
        this.tags = new HashSet<>();
        this.isDeleted = false;
        this.versionHistory = new ArrayList<>();
        this.nextVersionNumber = 1;
        this.allocatedBlocks = null;
        this.cachedContent = "";
    }

    // constructor for loading files from disk with all metadata
    public File(String name, String content, LocalDateTime createdTime,
                LocalDateTime modifiedTime, LocalDateTime accessedTime,
                Set<String> tags, boolean isDeleted, List<FileVersion> versionHistory,
                int[] allocatedBlocks) {
        this.name = name;
        this.cachedContent = content;
        this.size = content.length();
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.accessedTime = accessedTime;
        this.tags = new HashSet<>(tags);
        this.isDeleted = isDeleted;
        this.versionHistory = new ArrayList<>(versionHistory);
        this.nextVersionNumber = versionHistory.stream()
                .mapToInt(FileVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;
        this.allocatedBlocks = allocatedBlocks;
    }

    // simplified constructor for persistence loading
    public File(String name, String content, LocalDateTime createdTime,
                LocalDateTime modifiedTime, LocalDateTime accessedTime,
                Set<String> tags, boolean isDeleted, List<FileVersion> versionHistory) {
        this(name, content, createdTime, modifiedTime, accessedTime,
                tags, isDeleted, versionHistory, null);
    }

    // getters -------------------------------------

    public String getName() {
        return name;
    }

    public String getContent() {
        updateAccessedTime();
        return cachedContent;
    }

    // gets content without updating accessed time (for internal comparisons)
    public String getContentRaw() {
        return cachedContent;
    }

    public int getSize() {
        return size;
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
        return new HashSet<>(tags);
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public int[] getAllocatedBlocks() {
        return allocatedBlocks;
    }

    public int getBlockCount() {
        return allocatedBlocks != null ? allocatedBlocks.length : 0;
    }

    // setters --------------------------------------

    public void setName(String name) {
        this.name = name;
        updateModifiedTime();
    }

    // Sets content and updates size (storage allocation handled by FileSystem)
    public void setContent(String content) {
        this.cachedContent = content;
        this.size = content.length();
        updateModifiedTime();
    }

    public void setAllocatedBlocks(int[] blocks) {
        this.allocatedBlocks = blocks;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void appendContent(String additionalContent) {
        this.cachedContent += additionalContent;
        this.size = this.cachedContent.length();
        updateModifiedTime();
    }

    // tag management ------------------------------------

    public boolean addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return false;
        }
        return tags.add(tag.toLowerCase().trim());
    }

    public boolean removeTag(String tag) {
        if (tag == null) {
            return false;
        }
        return tags.remove(tag.toLowerCase().trim());
    }

    public boolean hasTag(String tag) {
        if (tag == null) {
            return false;
        }
        return tags.contains(tag.toLowerCase().trim());
    }

    public void clearTags() {
        tags.clear();
    }

    // version management --------------------------------

    public boolean saveVersion() {
        // check if content has actually changed from the last version
        if (!versionHistory.isEmpty()) {
            FileVersion lastVersion = versionHistory.get(versionHistory.size() - 1);
            if (lastVersion.getContent().equals(cachedContent)) {
                return false;
            }
        } else if (cachedContent.isEmpty()) {
            return false;
        }

        FileVersion newVersion = new FileVersion(cachedContent, nextVersionNumber++);
        versionHistory.add(newVersion);

        while (versionHistory.size() > MAX_VERSIONS) {
            versionHistory.remove(0);
        }

        return true;
    }

    public void forceSaveVersion() {
        FileVersion newVersion = new FileVersion(cachedContent, nextVersionNumber++);
        versionHistory.add(newVersion);

        while (versionHistory.size() > MAX_VERSIONS) {
            versionHistory.remove(0);
        }
    }

    public List<FileVersion> getVersionHistory() {
        return new ArrayList<>(versionHistory);
    }

    public FileVersion getVersion(int versionNumber) {
        for (FileVersion version : versionHistory) {
            if (version.getVersionNumber() == versionNumber) {
                return version;
            }
        }
        return null;
    }

    public boolean restoreVersion(int versionNumber) {
        FileVersion version = getVersion(versionNumber);
        if (version == null) {
            return false;
        }

        saveVersion();

        this.cachedContent = version.getContent();
        this.size = cachedContent.length();
        updateModifiedTime();

        return true;
    }

    public int getVersionCount() {
        return versionHistory.size();
    }

    public int getTotalVersionsCreated() {
        return nextVersionNumber - 1;
    }

    public boolean hasUnsavedChanges() {
        if (versionHistory.isEmpty()) {
            return !cachedContent.isEmpty();
        }
        FileVersion lastVersion = versionHistory.get(versionHistory.size() - 1);
        return !lastVersion.getContent().equals(cachedContent);
    }

    // deletion management --------------------------------

    public void markAsDeleted() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }

    // timestamps updates -------------------------------------------

    public void updateAccessedTime() {
        this.accessedTime = LocalDateTime.now();
    }

    public void updateModifiedTime() {
        this.modifiedTime = LocalDateTime.now();
    }

    // other methods -----------------------------------------------

    @Override
    public String toString() {
        return String.format("File{name='%s', size=%d bytes, blocks=%d, tags=%s, deleted=%b, versions=%d}",
                name, size, getBlockCount(), tags, isDeleted, versionHistory.size());
    }

    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(name).append("\n");
        sb.append("Size: ").append(size).append(" bytes\n");
        sb.append("Blocks: ").append(getBlockCount());
        if (allocatedBlocks != null && allocatedBlocks.length > 0) {
            sb.append(" (IDs: ");
            for (int i = 0; i < Math.min(allocatedBlocks.length, 5); i++) {
                if (i > 0) sb.append(", ");
                sb.append(allocatedBlocks[i]);
            }
            if (allocatedBlocks.length > 5) {
                sb.append(", ...");
            }
            sb.append(")");
        }
        sb.append("\n");
        sb.append("Created: ").append(createdTime).append("\n");
        sb.append("Modified: ").append(modifiedTime).append("\n");
        sb.append("Accessed: ").append(accessedTime).append("\n");
        sb.append("Tags: ").append(tags.isEmpty() ? "none" : tags).append("\n");
        sb.append("Deleted: ").append(isDeleted ? "Yes" : "No").append("\n");
        sb.append("Versions: ").append(versionHistory.size());
        if (getTotalVersionsCreated() > versionHistory.size()) {
            sb.append(" (").append(getTotalVersionsCreated()).append(" total created)");
        }
        sb.append("\n");
        return sb.toString();
    }
}