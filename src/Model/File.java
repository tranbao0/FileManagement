package Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

// simulated representation of a file in the file management system
// contains metadata, content, tags, and version history.
public class File implements Serializable {
    private static final long serialVersionUID = 1L;
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
    private int nextVersionNumber;  // tracks the next version number to assign

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
        this.nextVersionNumber = 1;
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
        // calculate next version number from existing history
        this.nextVersionNumber = versionHistory.stream()
                .mapToInt(FileVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;
        this.contentPointer = null;
    }

    // getters -------------------------------------

    public String getName() {
        return name;
    }

    public String getContent() {
        updateAccessedTime();
        return content;
    }

    // gets content without updating accessed time (for internal comparisons)
    public String getContentRaw() {
        return content;
    }

    public int getSize() {
        return content.length();
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

    public Integer getContentPointer() {
        return contentPointer;
    }

    // setters --------------------------------------

    public void setName(String name) {
        this.name = name;
        updateModifiedTime();
    }

    public void setContent(String content) {
        this.content = content;
        updateModifiedTime();
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void appendContent(String additionalContent) {
        this.content += additionalContent;
        updateModifiedTime();
    }

    public void setContentPointer(Integer contentPointer) {
        this.contentPointer = contentPointer;
    }

    // tag management ------------------------------------

    // adds a tag to this file
    // @return true if tag was added, false if already exists
    public boolean addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return false;
        }
        return tags.add(tag.toLowerCase().trim());
    }

    // removes a tag from this file
    // @return true if tag was removed, false if it did not exist
    public boolean removeTag(String tag) {
        if (tag == null) {
            return false;
        }
        return tags.remove(tag.toLowerCase().trim());
    }

    // checks if this file has a specific tag
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

    // saves current content as a new version if content has changed
    // maintains MAX_VERSIONS amount of versions
    // @return true if a new version was saved, false if content unchanged
    public boolean saveVersion() {
        // check if content has actually changed from the last version
        if (!versionHistory.isEmpty()) {
            FileVersion lastVersion = versionHistory.get(versionHistory.size() - 1);
            if (lastVersion.getContent().equals(content)) {
                // content hasn't changed, don't save a new version
                return false;
            }
        } else if (content.isEmpty()) {
            // empty file with no history, don't save empty version
            return false;
        }

        FileVersion newVersion = new FileVersion(content, nextVersionNumber++);
        versionHistory.add(newVersion);

        // Trim old versions (removes oldest first)
        while (versionHistory.size() > MAX_VERSIONS) {
            versionHistory.remove(0);
        }

        return true;
    }

    // forces saving a version regardless of whether content changed
    // useful for explicit save operations
    public void forceSaveVersion() {
        FileVersion newVersion = new FileVersion(content, nextVersionNumber++);
        versionHistory.add(newVersion);

        while (versionHistory.size() > MAX_VERSIONS) {
            versionHistory.remove(0);
        }
    }

    // returns a copy of the version history list
    public List<FileVersion> getVersionHistory() {
        return new ArrayList<>(versionHistory);
    }

    // returns specific version by its version number
    // @return the FileVersion, or null if not found
    public FileVersion getVersion(int versionNumber) {
        for (FileVersion version : versionHistory) {
            if (version.getVersionNumber() == versionNumber) {
                return version;
            }
        }
        return null;
    }

    // restores the file content from previous version
    // @param versionNumber the version number to restore
    // @return true if successful, false if version not found
    public boolean restoreVersion(int versionNumber) {
        FileVersion version = getVersion(versionNumber);
        if (version == null) {
            return false;
        }

        // save current state before restoring (only if different)
        saveVersion();

        this.content = version.getContent();
        updateModifiedTime();

        return true;
    }

    // returns number of versions stored for this file
    public int getVersionCount() {
        return versionHistory.size();
    }

    // returns the total number of versions ever created (including trimmed ones)
    public int getTotalVersionsCreated() {
        return nextVersionNumber - 1;
    }

    // checks if current content differs from the last saved version
    public boolean hasUnsavedChanges() {
        if (versionHistory.isEmpty()) {
            return !content.isEmpty(); // new file with content = unsaved
        }
        FileVersion lastVersion = versionHistory.get(versionHistory.size() - 1);
        return !lastVersion.getContent().equals(content);
    }

    // deletion management --------------------------------

    // mark this file as deleted (moved to bin)
    public void markAsDeleted() {
        this.isDeleted = true;
    }

    // restore this file from bin
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
        return String.format("File{name='%s', size=%d bytes, tags=%s, deleted=%b, versions=%d}",
                name, getSize(), tags, isDeleted, versionHistory.size());
    }

    // return detailed information about this file
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(name).append("\n");
        sb.append("Size: ").append(getSize()).append(" bytes\n");
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