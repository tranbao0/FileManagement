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

    // getters -------------------------------------

    public String getName() {
        return name;
    }

    public String getContent() {
        updateAccessedTime();
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

    public void appendContent(String additionaContent) {
        this.content += additionaContent;
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
        if(tag == null) {
            return false;
        }
        return tags.contains(tag.toLowerCase().trim());
    }

    public void clearTags() {
        tags.clear();
    }

    // version management --------------------------------

    //saves current version as a new version; maintains MAX_VERSIONS amount of versions
    public void saveVersion(){
        int versionNumber = versionHistory.size() + 1;
        FileVersion newVersion = new FileVersion(content, versionNumber);
        versionHistory.add(newVersion);

        // Trim old versions
        while (versionHistory.size() > MAX_VERSIONS){
            versionHistory.remove(0);
        }
    }

    // returns a copy of the version history list
    public List<FileVersion> getVersionHistory() {
        return new ArrayList<>(versionHistory);
    }

    // returns specific versions by its version number
    // @return the FileVersion, or null if not found
    public FileVersion getVersion(int versionNumber){
        for(FileVersion version : versionHistory) {
            if(version.getVersionNumber() == versionNumber) {
                return version;
            }
        }
        return null;
    }

    // restores the file content from previous version
    // @param versionNumber the version number to restore
    // @return return true if successful, false if version not found
    public boolean restoreVersion(int versionNumber) {
        FileVersion version = getVersion(versionNumber);
        if(version == null) {
            return false;
        }

        saveVersion();

        this.content = version.getContent();
        updateModifiedTime();

        return true;
    }

    // returns number of version stored for this file
    public int getVersionCount() {
        return versionHistory.size();
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
        return String.format("File{name='%s', size=%d bytes, tags=%s, deleted=%b, versions=%d", name, getSize(), tags, isDeleted, versionHistory.size());
    }

    // return information about this file
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(name).append("\n");
        sb.append("Size: ").append(getSize()).append(" bytes\n");
        sb.append("Created: ").append(createdTime).append("\n");
        sb.append("Modified: ").append(modifiedTime).append("\n");
        sb.append("Accessed: ").append(accessedTime).append("\n");
        sb.append("Tags: ").append(tags.isEmpty() ? "none" : tags).append("\n");
        sb.append("Deleted: ").append(isDeleted ? "Yes" : "No").append("\n");
        sb.append("Versions: ").append(versionHistory.size()).append("\n");
        return sb.toString();
    }
}

























