package GUI;

import Management.FileSystem;
import javax.swing.*;
import java.awt.*;

// panel that displays storage statistics
// shows block usage, capacity, and visual progress bar
public class StoragePanel extends JPanel {

    private FileSystem fileSystem;

    // UI components
    private JProgressBar usageBar;
    private JLabel usageLabel;
    private JLabel blocksLabel;
    private JLabel sizeLabel;

    // constructor
    public StoragePanel(FileSystem fileSystem) {
        this.fileSystem = fileSystem;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Virtual Disk Storage"));

        createComponents();
        refresh();
    }

    private void createComponents() {
        // Main info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 2, 2));

        // Usage progress bar
        usageBar = new JProgressBar(0, 100);
        usageBar.setStringPainted(true);
        usageBar.setPreferredSize(new Dimension(200, 25));

        // Labels
        usageLabel = new JLabel();
        blocksLabel = new JLabel();
        sizeLabel = new JLabel();

        // Style labels
        Font labelFont = new Font("SansSerif", Font.PLAIN, 11);
        usageLabel.setFont(labelFont);
        blocksLabel.setFont(labelFont);
        sizeLabel.setFont(labelFont);

        infoPanel.add(usageLabel);
        infoPanel.add(blocksLabel);
        infoPanel.add(sizeLabel);

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(usageBar, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.CENTER);

        // Details button
        JButton detailsButton = new JButton("Details");
        detailsButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
        detailsButton.addActionListener(e -> showStorageDetails());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.add(detailsButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // refreshes the storage display
    public void refresh() {
        double usagePercent = fileSystem.getStorageUsagePercentage();
        int usedBlocks = fileSystem.getStorageUsedBlocks();
        int totalBlocks = fileSystem.getStorageTotalBlocks();
        int freeBlocks = fileSystem.getStorageFreeBlocks();
        int blockSize = fileSystem.getStorageBlockSize();

        // Update progress bar
        usageBar.setValue((int) usagePercent);
        usageBar.setString(String.format("%.1f%% Used", usagePercent));

        // Color based on usage
        if (usagePercent > 90) {
            usageBar.setForeground(new Color(220, 53, 69));  // red
        } else if (usagePercent > 70) {
            usageBar.setForeground(new Color(255, 193, 7));  // yellow
        } else {
            usageBar.setForeground(new Color(40, 167, 69));  // green
        }

        // Update labels
        usageLabel.setText(String.format("Usage: %d / %d blocks", usedBlocks, totalBlocks));
        blocksLabel.setText(String.format("Free: %d blocks | Block Size: %d bytes", freeBlocks, blockSize));
        sizeLabel.setText(String.format("Capacity: %s | Used: %s | Free: %s",
                formatSize(fileSystem.getStorageTotalSize()),
                formatSize(fileSystem.getStorageUsedSize()),
                formatSize(fileSystem.getStorageFreeSize())));
    }

    // shows detailed storage information dialog
    private void showStorageDetails() {
        StringBuilder details = new StringBuilder();
        details.append("VIRTUAL DISK STORAGE DETAILS\n");
        details.append("════════════════════════════════════════\n\n");

        details.append("Configuration:\n");
        details.append("  Block Size:     ").append(fileSystem.getStorageBlockSize()).append(" bytes\n");
        details.append("  Total Blocks:   ").append(fileSystem.getStorageTotalBlocks()).append("\n");
        details.append("  Total Capacity: ").append(formatSize(fileSystem.getStorageTotalSize())).append("\n\n");

        details.append("Current Usage:\n");
        details.append("  Used Blocks:    ").append(fileSystem.getStorageUsedBlocks()).append("\n");
        details.append("  Free Blocks:    ").append(fileSystem.getStorageFreeBlocks()).append("\n");
        details.append("  Used Space:     ").append(formatSize(fileSystem.getStorageUsedSize())).append("\n");
        details.append("  Free Space:     ").append(formatSize(fileSystem.getStorageFreeSize())).append("\n");
        details.append("  Usage:          ").append(String.format("%.2f%%", fileSystem.getStorageUsagePercentage())).append("\n\n");

        details.append("File Statistics:\n");
        details.append("  Active Files:   ").append(fileSystem.getFileCount()).append("\n");
        details.append("  Deleted Files:  ").append(fileSystem.getDeletedFileCount()).append("\n");
        details.append("  Open Files:     ").append(fileSystem.getOpenFileCount()).append("\n\n");

        // Show per-file block allocation
        details.append("Block Allocation by File:\n");
        details.append("─────────────────────────────────────────\n");

        java.util.List<Model.File> files = fileSystem.listFiles();
        if (files.isEmpty()) {
            details.append("  (no active files)\n");
        } else {
            for (Model.File file : files) {
                details.append(String.format("  %-25s %4d bytes  %2d block(s)",
                        truncate(file.getName(), 25),
                        file.getSize(),
                        file.getBlockCount()));

                int[] blocks = file.getAllocatedBlocks();
                if (blocks != null && blocks.length > 0 && blocks.length <= 5) {
                    details.append("  [");
                    for (int i = 0; i < blocks.length; i++) {
                        if (i > 0) details.append(", ");
                        details.append(blocks[i]);
                    }
                    details.append("]");
                } else if (blocks != null && blocks.length > 5) {
                    details.append("  [").append(blocks[0]).append(", ").append(blocks[1])
                            .append(", ... +").append(blocks.length - 2).append(" more]");
                }
                details.append("\n");
            }
        }

        // Show deleted files too
        java.util.List<Model.File> deletedFiles = fileSystem.listDeletedFiles();
        if (!deletedFiles.isEmpty()) {
            details.append("\nDeleted Files (blocks still allocated):\n");
            details.append("─────────────────────────────────────────\n");
            for (Model.File file : deletedFiles) {
                details.append(String.format("  %-25s %4d bytes  %2d block(s)\n",
                        truncate(file.getName(), 25),
                        file.getSize(),
                        file.getBlockCount()));
            }
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Storage Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // helper to format sizes
    private String formatSize(int bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return bytes + " B";
        }
    }

    // helper to truncate long strings
    private String truncate(String str, int maxLen) {
        if (str.length() <= maxLen) {
            return str;
        }
        return str.substring(0, maxLen - 3) + "...";
    }
}