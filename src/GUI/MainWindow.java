package GUI;

import Management.FileSystem;
import Persistence.FileSystemPersistence;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

// main application window for the file management system
// contains menu bar, toolbar, file list, details panel, storage panel, and status bar
public class MainWindow extends JFrame {

    // core components
    private FileSystem fileSystem;

    // UI components
    private FileListPanel fileListPanel;
    private FileDetailsPanel fileDetailsPanel;
    private StoragePanel storagePanel;
    private JLabel statusLabel;

    // menu items
    private JMenuBar menuBar;
    private JToolBar toolBar;

    // constructor
    public MainWindow() {
        // load or create file system
        this.fileSystem = FileSystemPersistence.load();

        // setup the window
        setupWindow();

        // create UI components
        createMenuBar();
        createToolBar();
        createMainPanel();
        createStatusBar();

        // add shutdown hook to save on exit
        addShutdownHook();

        // show the window
        setVisible(true);
    }

    // sets up the main window properties
    private void setupWindow() {
        setTitle("File Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
    }

    // creates the menu bar
    private void createMenuBar() {
        menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newFileItem = new JMenuItem("New File");
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newFileItem.addActionListener(e -> createNewFile());

        JMenuItem openFileItem = new JMenuItem("Open File");
        openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openFileItem.addActionListener(e -> openFile());

        JMenuItem saveAllItem = new JMenuItem("Save All");
        saveAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveAllItem.addActionListener(e -> saveFileSystem());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(newFileItem);
        fileMenu.add(openFileItem);
        fileMenu.addSeparator();
        fileMenu.add(saveAllItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.addActionListener(e -> renameFile());

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> copyFile());

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(e -> deleteFile());

        editMenu.add(renameItem);
        editMenu.add(copyItem);
        editMenu.addSeparator();
        editMenu.add(deleteItem);

        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem allFilesItem = new JMenuItem("All Files");
        allFilesItem.addActionListener(e -> showAllFiles());

        JMenuItem recycleBinItem = new JMenuItem("Recycle Bin");
        recycleBinItem.addActionListener(e -> showRecycleBin());

        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshItem.addActionListener(e -> refreshFileList());

        viewMenu.add(allFilesItem);
        viewMenu.add(recycleBinItem);
        viewMenu.addSeparator();
        viewMenu.add(refreshItem);

        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);

        JMenuItem searchItem = new JMenuItem("Search");
        searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        searchItem.addActionListener(e -> searchFiles());

        JMenuItem storageInfoItem = new JMenuItem("Storage Info");
        storageInfoItem.addActionListener(e -> showStorageInfo());

        JMenuItem systemInfoItem = new JMenuItem("System Info");
        systemInfoItem.addActionListener(e -> showSystemInfo());

        JMenuItem emptyBinItem = new JMenuItem("Empty Recycle Bin");
        emptyBinItem.addActionListener(e -> emptyRecycleBin());

        toolsMenu.add(searchItem);
        toolsMenu.addSeparator();
        toolsMenu.add(storageInfoItem);
        toolsMenu.add(systemInfoItem);
        toolsMenu.addSeparator();
        toolsMenu.add(emptyBinItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(aboutItem);

        // add all menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    // creates the toolbar with buttons
    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton newButton = new JButton("New");
        newButton.setToolTipText("Create new file");
        newButton.addActionListener(e -> createNewFile());
        toolBar.add(newButton);

        JButton openButton = new JButton("Open");
        openButton.setToolTipText("Open file");
        openButton.addActionListener(e -> openFile());
        toolBar.add(openButton);

        toolBar.addSeparator();

        JButton deleteButton = new JButton("Delete");
        deleteButton.setToolTipText("Delete file");
        deleteButton.addActionListener(e -> deleteFile());
        toolBar.add(deleteButton);

        toolBar.addSeparator();

        JButton searchButton = new JButton("Search");
        searchButton.setToolTipText("Search files");
        searchButton.addActionListener(e -> searchFiles());
        toolBar.add(searchButton);

        toolBar.addSeparator();

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh file list");
        refreshButton.addActionListener(e -> refreshFileList());
        toolBar.add(refreshButton);

        toolBar.addSeparator();

        JButton storageButton = new JButton("Storage");
        storageButton.setToolTipText("View storage details");
        storageButton.addActionListener(e -> showStorageInfo());
        toolBar.add(storageButton);

        add(toolBar, BorderLayout.NORTH);
    }

    // creates the main panel with split panes
    private void createMainPanel() {
        // left panel: file list
        fileListPanel = new FileListPanel(fileSystem);

        fileListPanel.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailsPanel();
            }
        });

        fileListPanel.setDoubleClickListener(fileName -> handleEdit(fileName));

        // right panel: file details
        fileDetailsPanel = new FileDetailsPanel(fileSystem);

        fileDetailsPanel.setActionListener(new FileDetailsPanel.FileActionListener() {
            @Override
            public void onEdit(String fileName) {
                handleEdit(fileName);
            }

            @Override
            public void onManageTags(String fileName) {
                handleManageTags(fileName);
            }

            @Override
            public void onViewVersions(String fileName) {
                handleViewVersions(fileName);
            }

            @Override
            public void onRename(String fileName) {
                renameFile();
            }

            @Override
            public void onCopy(String fileName) {
                copyFile();
            }

            @Override
            public void onDelete(String fileName) {
                deleteFile();
            }

            @Override
            public void onRestore(String fileName) {
                handleRestore(fileName);
            }
        });

        // bottom right panel: storage info
        storagePanel = new StoragePanel(fileSystem);
        storagePanel.setPreferredSize(new Dimension(350, 120));

        // Right side: details + storage
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(fileDetailsPanel, BorderLayout.CENTER);
        rightPanel.add(storagePanel, BorderLayout.SOUTH);

        // create main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                fileListPanel,
                rightPanel);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.6);

        add(splitPane, BorderLayout.CENTER);
    }

    // updates the details panel with selected file info
    private void updateDetailsPanel() {
        Model.File selectedFile = fileListPanel.getSelectedFile();
        boolean isDeleted = (fileListPanel.getViewMode() == FileListPanel.ViewMode.RECYCLE_BIN);

        if (selectedFile != null) {
            fileDetailsPanel.displayFile(selectedFile, isDeleted);
        } else {
            fileDetailsPanel.showNoFileSelected();
        }
    }

    private void handleEdit(String fileName) {
        String content = fileSystem.readFileContent(fileName);

        if (content != null) {
            JTextArea textArea = new JTextArea(content, 20, 50);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            int result = JOptionPane.showConfirmDialog(this,
                    scrollPane,
                    "Edit: " + fileName,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                fileSystem.writeFileContent(fileName, textArea.getText());
                refreshAll();
                updateStatus("File saved: " + fileName);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not open file: " + fileName,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleManageTags(String fileName) {
        Model.File file = fileSystem.getDirectory().getFile(fileName);

        if (file == null) {
            return;
        }

        String currentTags = String.join(", ", file.getTags());
        String message = "Current tags: " + (currentTags.isEmpty() ? "(none)" : currentTags) + "\n\n";
        message += "Enter tags (comma-separated):";

        String input = JOptionPane.showInputDialog(this, message, "Manage Tags", JOptionPane.PLAIN_MESSAGE);

        if (input != null) {
            for (String tag : file.getTags()) {
                fileSystem.removeTag(fileName, tag);
            }

            if (!input.trim().isEmpty()) {
                String[] tags = input.split(",");
                for (String tag : tags) {
                    fileSystem.addTag(fileName, tag.trim());
                }
            }

            refreshAll();
            updateStatus("Tags updated for: " + fileName);
        }
    }

    private void handleViewVersions(String fileName) {
        List<Model.FileVersion> versions = fileSystem.listVersions(fileName);

        if (versions == null || versions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No versions available for this file.\n\n" +
                            "Versions are saved automatically when you close a file after editing.",
                    "Version History",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder message = new StringBuilder("Version History for: " + fileName + "\n\n");
        for (Model.FileVersion v : versions) {
            message.append("  • Version ").append(v.getVersionNumber())
                    .append(" - ").append(v.getFormattedTimestamp())
                    .append(" (").append(v.getSize()).append(" bytes)\n");
        }
        message.append("\nEnter version number to restore (or cancel):");

        String input = JOptionPane.showInputDialog(this, message.toString(), "Version History", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int versionNum = Integer.parseInt(input.trim());
                boolean restored = fileSystem.restoreVersion(fileName, versionNum);

                if (restored) {
                    JOptionPane.showMessageDialog(this,
                            "Version " + versionNum + " restored successfully.\n" +
                                    "A new version was created with the previous content.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshAll();
                    updateStatus("Restored version " + versionNum + " for: " + fileName);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not restore version " + versionNum + ".\n" +
                                    "The version may not exist.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid version number: " + input,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRestore(String fileName) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Restore '" + fileName + "' from recycle bin?",
                "Restore File",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean restored = fileSystem.restoreFromBin(fileName);

            if (restored) {
                JOptionPane.showMessageDialog(this,
                        "File restored successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshAll();
                updateStatus("Restored: " + fileName);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not restore file.\n" +
                                "A file with this name may already exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // creates the status bar at bottom
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());

        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        statusBar.add(statusLabel, BorderLayout.WEST);

        add(statusBar, BorderLayout.SOUTH);

        updateStatusBar();
    }

    private void updateStatusBar() {
        String status = String.format(" %d files | %d open | %d deleted | Storage: %.1f%% used (%d/%d blocks)",
                fileSystem.getFileCount(),
                fileSystem.getOpenFileCount(),
                fileSystem.getDeletedFileCount(),
                fileSystem.getStorageUsagePercentage(),
                fileSystem.getStorageUsedBlocks(),
                fileSystem.getStorageTotalBlocks());
        statusLabel.setText(status);
    }

    private void updateStatus(String message) {
        statusLabel.setText(" " + message);
        Timer timer = new Timer(3000, e -> updateStatusBar());
        timer.setRepeats(false);
        timer.start();
    }

    // refreshes all panels
    private void refreshAll() {
        fileListPanel.refresh();
        storagePanel.refresh();
        updateDetailsPanel();
        updateStatusBar();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FileSystemPersistence.save(fileSystem);
        }));
    }

    // menu action methods ---------------------------------

    private void createNewFile() {
        String fileName = JOptionPane.showInputDialog(this,
                "Enter file name:",
                "New File",
                JOptionPane.PLAIN_MESSAGE);

        if (fileName != null && !fileName.trim().isEmpty()) {
            boolean created = fileSystem.createFile(fileName.trim());

            if (created) {
                refreshAll();
                updateStatus("Created: " + fileName);

                int choice = JOptionPane.showConfirmDialog(this,
                        "File created: " + fileName + "\n\nWould you like to edit it now?",
                        "File Created",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    handleEdit(fileName.trim());
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not create file.\n" +
                                "A file with this name may already exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFile() {
        String fileName = fileListPanel.getSelectedFileName();

        if (fileName == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file first",
                    "No File Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        handleEdit(fileName);
    }

    private void saveFileSystem() {
        boolean saved = FileSystemPersistence.save(fileSystem);

        if (saved) {
            updateStatus("File system saved successfully");
            JOptionPane.showMessageDialog(this,
                    "File system saved successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error saving file system",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Save before exiting?",
                "Exit",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            FileSystemPersistence.save(fileSystem);
            System.exit(0);
        } else if (choice == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }

    private void renameFile() {
        String oldName = fileListPanel.getSelectedFileName();

        if (oldName == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file first",
                    "No File Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newName = JOptionPane.showInputDialog(this,
                "Enter new name for '" + oldName + "':",
                "Rename File",
                JOptionPane.PLAIN_MESSAGE);

        if (newName != null && !newName.trim().isEmpty()) {
            boolean renamed = fileSystem.renameFile(oldName, newName.trim());

            if (renamed) {
                refreshAll();
                updateStatus("Renamed: " + oldName + " → " + newName);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not rename file.\n" +
                                "A file with this name may already exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyFile() {
        String sourceName = fileListPanel.getSelectedFileName();

        if (sourceName == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file first",
                    "No File Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String destName = JOptionPane.showInputDialog(this,
                "Enter name for copy of '" + sourceName + "':",
                "Copy File",
                JOptionPane.PLAIN_MESSAGE);

        if (destName != null && !destName.trim().isEmpty()) {
            boolean copied = fileSystem.copyFile(sourceName, destName.trim());

            if (copied) {
                refreshAll();
                updateStatus("Copied: " + sourceName + " → " + destName);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not copy file.\n" +
                                "A file with this name may already exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteFile() {
        String fileName = fileListPanel.getSelectedFileName();

        if (fileName == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file first",
                    "No File Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (fileListPanel.getViewMode() == FileListPanel.ViewMode.RECYCLE_BIN) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Permanently delete '" + fileName + "'?\n\nThis will free the storage blocks and cannot be undone.",
                    "Permanent Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                boolean deleted = fileSystem.permanentDelete(fileName);

                if (deleted) {
                    refreshAll();
                    updateStatus("Permanently deleted: " + fileName + " (storage freed)");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not delete file",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Move '" + fileName + "' to recycle bin?\n\n(Storage blocks remain allocated until permanent deletion)",
                    "Delete File",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                boolean deleted = fileSystem.deleteFile(fileName);

                if (deleted) {
                    refreshAll();
                    updateStatus("Moved to recycle bin: " + fileName);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not delete file",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showAllFiles() {
        fileListPanel.setViewMode(FileListPanel.ViewMode.ALL_FILES);
        updateStatusBar();
        updateStatus("Viewing all files");
    }

    private void showRecycleBin() {
        fileListPanel.setViewMode(FileListPanel.ViewMode.RECYCLE_BIN);
        updateStatusBar();
        updateStatus("Viewing recycle bin");
    }

    private void refreshFileList() {
        refreshAll();
    }

    private void searchFiles() {
        JPanel searchPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JTextField queryField = new JTextField(20);

        JRadioButton nameSearch = new JRadioButton("Search by name", true);
        JRadioButton tagSearch = new JRadioButton("Search by tag");
        JRadioButton contentSearch = new JRadioButton("Search by content");

        ButtonGroup group = new ButtonGroup();
        group.add(nameSearch);
        group.add(tagSearch);
        group.add(contentSearch);

        searchPanel.add(new JLabel("Enter search query:"));
        searchPanel.add(queryField);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(nameSearch);
        radioPanel.add(tagSearch);
        radioPanel.add(contentSearch);
        searchPanel.add(radioPanel);

        int result = JOptionPane.showConfirmDialog(this, searchPanel, "Search Files",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String query = queryField.getText().trim();

            if (!query.isEmpty()) {
                java.util.List<Model.File> results;
                String searchType;

                if (tagSearch.isSelected()) {
                    results = fileSystem.searchByTag(query);
                    searchType = "tag";
                } else if (contentSearch.isSelected()) {
                    results = fileSystem.searchByContent(query);
                    searchType = "content";
                } else {
                    results = fileSystem.searchByName(query);
                    searchType = "name";
                }

                StringBuilder message = new StringBuilder();
                message.append("Search results for \"").append(query).append("\" (by ").append(searchType).append("):\n\n");

                if (results.isEmpty()) {
                    message.append("No files found.");
                } else {
                    message.append("Found ").append(results.size()).append(" file(s):\n\n");
                    for (Model.File f : results) {
                        message.append("  • ").append(f.getName());
                        message.append(" (").append(f.getSize()).append(" bytes, ");
                        message.append(f.getBlockCount()).append(" blocks)");
                        if (!f.getTags().isEmpty()) {
                            message.append(" [").append(String.join(", ", f.getTags())).append("]");
                        }
                        message.append("\n");
                    }
                }

                JOptionPane.showMessageDialog(this,
                        message.toString(),
                        "Search Results",
                        JOptionPane.INFORMATION_MESSAGE);

                updateStatus("Found " + results.size() + " file(s) matching \"" + query + "\"");
            }
        }
    }

    private void showStorageInfo() {
        JTextArea textArea = new JTextArea(fileSystem.getStorageInfo());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Storage Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSystemInfo() {
        JTextArea textArea = new JTextArea(fileSystem.getSystemInfo());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane,
                "System Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void emptyRecycleBin() {
        int deletedCount = fileSystem.getDeletedFileCount();

        if (deletedCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "Recycle bin is already empty.",
                    "Empty Recycle Bin",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Permanently delete all " + deletedCount + " file(s) in recycle bin?\n\n" +
                        "This will free all storage blocks used by these files.\n" +
                        "This cannot be undone.",
                "Empty Recycle Bin",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            int count = fileSystem.emptyRecycleBin();
            refreshAll();
            updateStatus("Permanently deleted " + count + " file(s) - storage freed");
            JOptionPane.showMessageDialog(this,
                    "Deleted " + count + " file(s) permanently\nStorage blocks freed.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAbout() {
        String message = "File Management System\n\n" +
                "A simulated file system with:\n" +
                "  • Block-based virtual disk storage\n" +
                "  • File creation, editing, and deletion\n" +
                "  • Tag-based organization\n" +
                "  • Version history (up to 5 versions)\n" +
                "  • Recycle bin with restore\n" +
                "  • Persistent storage\n\n" +
                "Storage Configuration:\n" +
                "  • Block Size: " + fileSystem.getStorageBlockSize() + " bytes\n" +
                "  • Total Blocks: " + fileSystem.getStorageTotalBlocks() + "\n" +
                "  • Capacity: " + formatSize(fileSystem.getStorageTotalSize()) + "\n\n" +
                "Current Status:\n" +
                "  • Files: " + fileSystem.getFileCount() + " active, " +
                fileSystem.getDeletedFileCount() + " deleted\n" +
                "  • Storage: " + String.format("%.1f%%", fileSystem.getStorageUsagePercentage()) + " used\n" +
                "  • Operations: " + fileSystem.getTotalOperations() + "\n\n" +
                "Built with Java Swing";

        JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatSize(int bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return bytes + " bytes";
        }
    }

    // main method to launch the application
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // use default
        }

        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}