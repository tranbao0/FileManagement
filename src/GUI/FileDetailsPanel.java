package GUI;

import Management.FileSystem;
import Model.File;
import Model.FileVersion;
import javax.swing.table.TableRowSorter;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Comparator;

// panel that displays detailed information about a selected file
// includes buttons for file operations
public class FileDetailsPanel extends JPanel {

    private FileSystem fileSystem;
    private File currentFile;

    // UI components
    private JTextArea infoTextArea;
    private JPanel buttonPanel;

    // buttons
    private JButton editButton;
    private JButton tagsButton;
    private JButton versionsButton;
    private JButton renameButton;
    private JButton copyButton;
    private JButton deleteButton;
    private JButton restoreButton;

    // listener for actions
    private FileActionListener actionListener;

    // constructor
    public FileDetailsPanel(FileSystem fileSystem) {
        this.fileSystem = fileSystem;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("File Details"));

        createInfoArea();
        createButtonPanel();

        showNoFileSelected();
    }

    // creates the info text area
    private void createInfoArea() {
        infoTextArea = new JTextArea();
        infoTextArea.setEditable(false);
        infoTextArea.setMargin(new Insets(10, 10, 10, 10));
        infoTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(infoTextArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    // creates the button panel
    private void createButtonPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // edit button
        editButton = new JButton("Edit");
        editButton.setToolTipText("Edit file content");
        editButton.addActionListener(e -> handleEdit());
        buttonPanel.add(editButton);

        // tags button
        tagsButton = new JButton("Tags");
        tagsButton.setToolTipText("Manage file tags");
        tagsButton.addActionListener(e -> handleTags());
        buttonPanel.add(tagsButton);

        // versions button
        versionsButton = new JButton("Versions");
        versionsButton.setToolTipText("View version history");
        versionsButton.addActionListener(e -> handleVersions());
        buttonPanel.add(versionsButton);

        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // rename button
        renameButton = new JButton("Rename");
        renameButton.setToolTipText("Rename this file");
        renameButton.addActionListener(e -> handleRename());
        buttonPanel.add(renameButton);

        // copy button
        copyButton = new JButton("Copy");
        copyButton.setToolTipText("Create a copy of this file");
        copyButton.addActionListener(e -> handleCopy());
        buttonPanel.add(copyButton);

        // delete button
        deleteButton = new JButton("Delete");
        deleteButton.setToolTipText("Move to recycle bin");
        deleteButton.addActionListener(e -> handleDelete());
        buttonPanel.add(deleteButton);

        // restore button (for deleted files)
        restoreButton = new JButton("Restore");
        restoreButton.setToolTipText("Restore from recycle bin");
        restoreButton.addActionListener(e -> handleRestore());
        buttonPanel.add(restoreButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // displays information about a file
    public void displayFile(File file, boolean isDeleted) {
        this.currentFile = file;

        if (file == null) {
            showNoFileSelected();
            return;
        }

        // build info text
        StringBuilder info = new StringBuilder();
        info.append("FILE INFORMATION\n");
        info.append("═══════════════════════════════════════\n\n");

        info.append("Name:           ").append(file.getName()).append("\n");
        info.append("Size:           ").append(file.getSize()).append(" bytes\n");
        info.append("Created:        ").append(formatDateTime(file.getCreatedTime())).append("\n");
        info.append("Modified:       ").append(formatDateTime(file.getModifiedTime())).append("\n");
        info.append("Accessed:       ").append(formatDateTime(file.getAccessedTime())).append("\n");
        info.append("\n");

        info.append("Tags:           ");
        if (file.getTags().isEmpty()) {
            info.append("(none)");
        } else {
            info.append(String.join(", ", file.getTags()));
        }
        info.append("\n");

        info.append("Versions:       ").append(file.getVersionCount()).append("\n");
        info.append("Status:         ").append(file.isDeleted() ? "DELETED" : "Active").append("\n");
        info.append("\n");

        info.append("CONTENT PREVIEW\n");
        info.append("═══════════════════════════════════════\n\n");

        String content = file.getContent();
        if (content.isEmpty()) {
            info.append("(empty file)");
        } else {
            // show first 500 characters
            if (content.length() > 500) {
                info.append(content.substring(0, 500));
                info.append("\n\n... (").append(content.length() - 500).append(" more characters)");
            } else {
                info.append(content);
            }
        }

        infoTextArea.setText(info.toString());
        infoTextArea.setCaretPosition(0); // scroll to top

        // update button visibility based on file state
        updateButtons(isDeleted);
    }

    // formats date/time for display
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    // updates button visibility based on file state
    private void updateButtons(boolean isDeleted) {
        if (isDeleted) {
            // deleted file - show only restore
            editButton.setVisible(false);
            tagsButton.setVisible(false);
            versionsButton.setVisible(false);
            renameButton.setVisible(false);
            copyButton.setVisible(false);
            deleteButton.setVisible(false);
            restoreButton.setVisible(true);
        } else {
            // active file - show all except restore
            editButton.setVisible(true);
            tagsButton.setVisible(true);
            versionsButton.setVisible(true);
            renameButton.setVisible(true);
            copyButton.setVisible(true);
            deleteButton.setVisible(true);
            restoreButton.setVisible(false);
        }
    }

    // shows message when no file is selected
    public void showNoFileSelected() {
        this.currentFile = null;
        infoTextArea.setText("\n\n\n          No file selected\n\n          Select a file from the list to view details");

        // hide all buttons
        editButton.setVisible(false);
        tagsButton.setVisible(false);
        versionsButton.setVisible(false);
        renameButton.setVisible(false);
        copyButton.setVisible(false);
        deleteButton.setVisible(false);
        restoreButton.setVisible(false);
    }

    // button action handlers ---------------------------------

    private void handleEdit() {
        if (currentFile != null && actionListener != null) {
            actionListener.onEdit(currentFile.getName());
        }
    }

    private void handleTags() {
        if (currentFile != null && actionListener != null) {
            actionListener.onManageTags(currentFile.getName());
        }
    }

    private void handleVersions() {
        if (currentFile != null && actionListener != null) {
            actionListener.onViewVersions(currentFile.getName());
        }
    }

    private void handleRename() {
        if (currentFile != null && actionListener != null) {
            actionListener.onRename(currentFile.getName());
        }
    }

    private void handleCopy() {
        if (currentFile != null && actionListener != null) {
            actionListener.onCopy(currentFile.getName());
        }
    }

    private void handleDelete() {
        if (currentFile != null && actionListener != null) {
            actionListener.onDelete(currentFile.getName());
        }
    }

    private void handleRestore() {
        if (currentFile != null && actionListener != null) {
            actionListener.onRestore(currentFile.getName());
        }
    }

    // listener interface for file actions
    public interface FileActionListener {
        void onEdit(String fileName);
        void onManageTags(String fileName);
        void onViewVersions(String fileName);
        void onRename(String fileName);
        void onCopy(String fileName);
        void onDelete(String fileName);
        void onRestore(String fileName);
    }

    public void setActionListener(FileActionListener listener) {
        this.actionListener = listener;
    }
}