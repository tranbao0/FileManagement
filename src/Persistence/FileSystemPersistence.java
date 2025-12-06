package Persistence;

import Management.FileSystem;
import Model.File;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// handles saving and loading file system state to/from disk
public class FileSystemPersistence {

    // default save file name
    private static final String DEFAULT_FILENAME = "filesystem.dat";

    // saves the file system to a file
    // param fs the file system to save
    // param filename the name of the file to save to
    // return true if saved successfully, false otherwise
    public static boolean save(FileSystem fs, String filename) {
        if (fs == null || filename == null || filename.trim().isEmpty()) {
            return false;
        }

        try {
            // extract data from file system
            FileSystemData data = new FileSystemData();
            data.activeFiles = fs.getDirectory().getActiveFiles();
            data.deletedFiles = fs.getRecycleBin().getAllDeletedFiles();
            data.totalFilesCreated = fs.getTotalFilesCreated();
            data.totalOperations = fs.getTotalOperations();

            // write to file
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(filename))) {
                out.writeObject(data);
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error saving file system: " + e.getMessage());
            return false;
        }
    }

    // saves the file system to the default file
    // param fs the file system to save
    // return true if saved successfully
    public static boolean save(FileSystem fs) {
        return save(fs, DEFAULT_FILENAME);
    }

    // loads a file system from a file
    // param filename the name of the file to load from
    // return the loaded FileSystem, or a new empty FileSystem if file doesn't exist or error occurs
    public static FileSystem load(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return new FileSystem();
        }

        java.io.File file = new java.io.File(filename);

        // if file doesn't exist, return new file system
        if (!file.exists()) {
            System.out.println("Save file not found. Starting with empty file system.");
            return new FileSystem();
        }

        try {
            // read data from file
            FileSystemData data;
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(filename))) {
                data = (FileSystemData) in.readObject();
            }

            // create new file system
            FileSystem fs = new FileSystem();

            // restore active files to directory
            if (data.activeFiles != null) {
                for (Model.File f : data.activeFiles) {
                    fs.getDirectory().addFile(f);
                }
            }

            // restore deleted files to recycle bin
            if (data.deletedFiles != null) {
                for (Model.File f : data.deletedFiles) {
                    fs.getRecycleBin().addFile(f);
                }
            }

            // note: we can't restore statistics due to final fields
            // this is a limitation of this approach

            System.out.println("File system loaded successfully.");
            System.out.println("Restored " + (data.activeFiles != null ? data.activeFiles.size() : 0) + " active files");
            System.out.println("Restored " + (data.deletedFiles != null ? data.deletedFiles.size() : 0) + " deleted files");

            return fs;

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading file system: " + e.getMessage());
            System.out.println("Starting with empty file system.");
            return new FileSystem();
        }
    }

    // loads a file system from the default file
    // return the loaded FileSystem
    public static FileSystem load() {
        return load(DEFAULT_FILENAME);
    }

    // checks if a save file exists
    // param filename the name of the file to check
    // return true if file exists
    public static boolean saveFileExists(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        java.io.File file = new java.io.File(filename);
        return file.exists();
    }

    // checks if the default save file exists
    // return true if file exists
    public static boolean saveFileExists() {
        return saveFileExists(DEFAULT_FILENAME);
    }

    // deletes a save file
    // param filename the name of the file to delete
    // return true if deleted successfully
    public static boolean deleteSaveFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        java.io.File file = new java.io.File(filename);

        if (!file.exists()) {
            return false;
        }

        return file.delete();
    }

    // deletes the default save file
    // return true if deleted successfully
    public static boolean deleteSaveFile() {
        return deleteSaveFile(DEFAULT_FILENAME);
    }

    // gets the default save filename
    public static String getDefaultFilename() {
        return DEFAULT_FILENAME;
    }
}