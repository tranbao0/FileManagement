package Persistence;

import Model.File;
import java.io.Serializable;
import java.util.List;

// serializable data structure to hold file system state
// used for saving/loading the file system
class FileSystemData implements Serializable {
    private static final long serialVersionUID = 1L;

    // data to persist
    public List<File> activeFiles;      // files in directory
    public List<File> deletedFiles;     // files in recycle bin
    public int totalFilesCreated;
    public int totalOperations;

    // constructor
    public FileSystemData() {
    }

    public FileSystemData(List<File> activeFiles, List<File> deletedFiles,
                          int totalFilesCreated, int totalOperations) {
        this.activeFiles = activeFiles;
        this.deletedFiles = deletedFiles;
        this.totalFilesCreated = totalFilesCreated;
        this.totalOperations = totalOperations;
    }
}