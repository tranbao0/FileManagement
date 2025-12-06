package GUI;

import Management.FileSystem;
import Persistence.FileSystemPersistence;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

// main application window for the file management system
// contains menu bar, toolbar, file list, details panel, and status bar
public class MainWindow extends JFrame {

    // core components
    private FileSystem fileSystem;

    // UI components
    private FileListPanel fileListPanel;
    private FileDetailsPanel fileDetailsPanel;
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
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center on screen

        // set layout
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

        JMenuItem emptyBinItem = new JMenuItem("Empty Recycle Bin");
        emptyBinItem.addActionListener(e -> emptyRecycleBin());

        toolsMenu.add(searchItem);
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

        // New File button
        JButton newButton = new JButton("New");
        newButton.setToolTipText("Create new file");
        newButton.addActionListener(e -> createNewFile());
        toolBar.add(newButton);

        // Open button
        JButton openButton = new JButton("Open");
        openButton.setToolTipText("Open file");
        openButton.addActionListener(e -> openFile());
        toolBar.add(openButton);

        toolBar.addSeparator();

        // Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setToolTipText("Delete file");
        deleteButton.addActionListener(e -> deleteFile());
        toolBar.add(deleteButton);

        toolBar.addSeparator();

        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.setToolTipText("Search files");
        searchButton.addActionListener(e -> searchFiles());
        toolBar.add(searchButton);

        toolBar.addSeparator();

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh file list");
        refreshButton.addActionListener(e -> refreshFileList());
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);
    }

    // creates the main panel with split pane
    // creates the main panel with split pane
    private void createMainPanel() {
        // left panel: file list
        fileListPanel = new FileListPanel(fileSystem);

        // add selection listener to update details panel
        fileListPanel.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailsPanel();
            }
        });

        // add double-click listener to open files
        fileListPanel.setDoubleClickListener(fileName -> handleEdit(fileName));

        // right panel: file details
        fileDetailsPanel = new FileDetailsPanel(fileSystem);

        // set action listener for detail panel buttons
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

        // create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                fileListPanel,
                fileDetailsPanel);
        splitPane.setDividerLocation(500);
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

    // handle edit (placeholder - will implement editor dialog next)
    private void handleEdit(String fileName) {
        String content = fileSystem.readFileContent(fileName);

        if (content != null) {
            // for now, show in simple dialog
            JTextArea textArea = new JTextArea(content, 20, 50);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            int result = JOptionPane.showConfirmDialog(this,
                    new JScrollPane(textArea),
                    "Edit: " + fileName,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                fileSystem.writeFileContent(fileName, textArea.getText());
                refreshFileList();
                updateDetailsPanel();
            }
        }
    }

    // handle manage tags (placeholder)
    private void handleManageTags(String fileName) {
        Model.File file = fileSystem.getDirectory().getFile(fileName);

        if (file == null) {
            return;
        }

        // show current tags
        String currentTags = String.join(", ", file.getTags());
        String message = "Current tags: " + (currentTags.isEmpty() ? "(none)" : currentTags) + "\n\n";
        message += "Enter tags (comma-separated):";

        String input = JOptionPane.showInputDialog(this, message, "Manage Tags", JOptionPane.PLAIN_MESSAGE);

        if (input != null) {
            // clear existing tags
            for (String tag : file.getTags()) {
                fileSystem.removeTag(fileName, tag);
            }

            // add new tags
            if (!input.trim().isEmpty()) {
                String[] tags = input.split(",");
                for (String tag : tags) {
                    fileSystem.addTag(fileName, tag.trim());
                }
            }

            refreshFileList();
            updateDetailsPanel();
        }
    }

    // handle view versions (placeholder)
    private void handleViewVersions(String fileName) {
        List<Model.FileVersion> versions = fileSystem.listVersions(fileName);

        if (versions == null || versions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No versions available",
                    "Version History",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // build version list message
        StringBuilder message = new StringBuilder("Version History for: " + fileName + "\n\n");
        for (Model.FileVersion v : versions) {
            message.append(v.toString()).append("\n");
        }
        message.append("\nEnter version number to restore (or cancel):");

        String input = JOptionPane.showInputDialog(this, message.toString(), "Version History", JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int versionNum = Integer.parseInt(input.trim());
                boolean restored = fileSystem.restoreVersion(fileName, versionNum);

                if (restored) {
                    JOptionPane.showMessageDialog(this,
                            "Version " + versionNum + " restored",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshFileList();
                    updateDetailsPanel();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not restore version",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid version number",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // handle restore from recycle bin
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
                refreshFileList();
                updateDetailsPanel();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not restore file (name may already exist)",
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

    // updates the status bar with current file system info
    private void updateStatusBar() {
        String status = String.format(" %d files | %d open | %d deleted",
                fileSystem.getFileCount(),
                fileSystem.getOpenFileCount(),
                fileSystem.getDeletedFileCount());
        statusLabel.setText(status);
    }

    // adds shutdown hook to save file system on exit
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FileSystemPersistence.save(fileSystem);
        }));
    }

    // menu action methods (placeholders for now) ---------------------------------

    private void createNewFile() {
        String fileName = JOptionPane.showInputDialog(this,
                "Enter file name:",
                "New File",
                JOptionPane.PLAIN_MESSAGE);

        if (fileName != null && !fileName.trim().isEmpty()) {
            boolean created = fileSystem.createFile(fileName);

            if (created) {
                JOptionPane.showMessageDialog(this,
                        "File created: " + fileName,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshFileList();
            } else {
                JOptionPane.showMessageDialog(this,
                        "File already exists or invalid name",
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

        openFileInEditor(fileName);
    }

    // opens file in editor (placeholder for now)
    private void openFileInEditor(String fileName) {
        String content = fileSystem.readFileContent(fileName);

        if (content != null) {
            JOptionPane.showMessageDialog(this,
                    "Opening: " + fileName + "\n\nContent:\n" + content,
                    "File Content",
                    JOptionPane.INFORMATION_MESSAGE);
            // TODO: open in proper editor dialog in next phase
        }
    }

    private void saveFileSystem() {
        boolean saved = FileSystemPersistence.save(fileSystem);

        if (saved) {
            JOptionPane.showMessageDialog(this, "File system saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Error saving file system", "Error", JOptionPane.ERROR_MESSAGE);
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
        // CANCEL - do nothing
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
                "Enter new name:",
                "Rename File",
                JOptionPane.PLAIN_MESSAGE);

        if (newName != null && !newName.trim().isEmpty()) {
            boolean renamed = fileSystem.renameFile(oldName, newName);

            if (renamed) {
                JOptionPane.showMessageDialog(this,
                        "File renamed successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshFileList();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not rename file (name may already exist)",
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
                "Enter name for copy:",
                "Copy File",
                JOptionPane.PLAIN_MESSAGE);

        if (destName != null && !destName.trim().isEmpty()) {
            boolean copied = fileSystem.copyFile(sourceName, destName);

            if (copied) {
                JOptionPane.showMessageDialog(this,
                        "File copied successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshFileList();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not copy file (name may already exist)",
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

        int choice = JOptionPane.showConfirmDialog(this,
                "Move '" + fileName + "' to recycle bin?",
                "Delete File",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean deleted = fileSystem.deleteFile(fileName);

            if (deleted) {
                JOptionPane.showMessageDialog(this,
                        "File moved to recycle bin",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshFileList();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete file",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAllFiles() {
        fileListPanel.setViewMode(FileListPanel.ViewMode.ALL_FILES);
        updateStatusBar();
    }

    private void showRecycleBin() {
        fileListPanel.setViewMode(FileListPanel.ViewMode.RECYCLE_BIN);
        updateStatusBar();
    }

    private void refreshFileList() {
        fileListPanel.refresh();
        updateStatusBar();
    }

    private void searchFiles() {
        String query = JOptionPane.showInputDialog(this,
                "Enter search query:",
                "Search Files",
                JOptionPane.PLAIN_MESSAGE);

        if (query != null && !query.trim().isEmpty()) {
            java.util.List<Model.File> results = fileSystem.searchByName(query);

            StringBuilder message = new StringBuilder("Found " + results.size() + " file(s):\n\n");
            for (Model.File f : results) {
                message.append("- ").append(f.getName()).append("\n");
            }

            JOptionPane.showMessageDialog(this,
                    message.toString(),
                    "Search Results",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void emptyRecycleBin() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Permanently delete all files in recycle bin?",
                "Empty Recycle Bin",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            int count = fileSystem.emptyRecycleBin();
            JOptionPane.showMessageDialog(this,
                    "Deleted " + count + " files permanently",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            updateStatusBar();
        }
    }

    private void showAbout() {
        String message = "File Management System\n\n" +
                "A simulated file system with features:\n" +
                "- File creation, editing, deletion\n" +
                "- Tag-based organization\n" +
                "- Version history\n" +
                "- Recycle bin\n\n" +
                "Built with Java Swing";

        JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // main method to launch the application
    public static void main(String[] args) {
        // use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // use default if system look and feel fails
        }

        // create and show window on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}