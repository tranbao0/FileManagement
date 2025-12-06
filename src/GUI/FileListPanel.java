package GUI;

import Management.FileSystem;
import Model.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Comparator;

// panel that displays the list of files in a table
public class FileListPanel extends JPanel {

    private FileSystem fileSystem;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private TableRowSorter<DefaultTableModel> sorter;

    // view mode
    private ViewMode currentView;

    public enum ViewMode {
        ALL_FILES,      // show active files
        RECYCLE_BIN     // show deleted files
    }

    // column names for the table
    private static final String[] COLUMN_NAMES = {
            "Name", "Size (bytes)", "Modified", "Tags", "Versions"
    };

    // column indices
    private static final int COL_NAME = 0;
    private static final int COL_SIZE = 1;
    private static final int COL_MODIFIED = 2;
    private static final int COL_TAGS = 3;
    private static final int COL_VERSIONS = 4;

    // constructor
    public FileListPanel(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.currentView = ViewMode.ALL_FILES;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Files"));

        createTable();
        loadFiles();
    }

    // creates the table and scroll pane
    private void createTable() {
        // create table model (non-editable)
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // make table read-only
            }

            // specify column types for proper sorting
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case COL_NAME:
                        return String.class;
                    case COL_SIZE:
                        return Integer.class;  // size is integer
                    case COL_MODIFIED:
                        return String.class;
                    case COL_TAGS:
                        return String.class;
                    case COL_VERSIONS:
                        return Integer.class;  // versions is integer
                    default:
                        return String.class;
                }
            }
        };

        // create table
        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setRowHeight(25);

        // create and set the row sorter
        sorter = new TableRowSorter<>(tableModel);
        fileTable.setRowSorter(sorter);

        // set custom comparators for specific columns
        // size column - compare as integers
        sorter.setComparator(COL_SIZE, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        // versions column - compare as integers
        sorter.setComparator(COL_VERSIONS, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        // set column widths
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Name
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Size
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Modified
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Tags
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Versions

        // add double-click listener
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                }
            }
        });

        // add to scroll pane
        scrollPane = new JScrollPane(fileTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    // loads files from file system into table
    public void loadFiles() {
        // clear existing rows
        tableModel.setRowCount(0);

        List<File> files;

        // get files based on current view
        if (currentView == ViewMode.ALL_FILES) {
            files = fileSystem.listFiles(); // active files only
            setBorder(BorderFactory.createTitledBorder("Files (" + files.size() + ")"));
        } else {
            files = fileSystem.listDeletedFiles(); // deleted files
            setBorder(BorderFactory.createTitledBorder("Recycle Bin (" + files.size() + ")"));
        }

        // add files to table
        for (File file : files) {
            addFileToTable(file);
        }
    }

    // adds a file to the table
    private void addFileToTable(File file) {
        Object[] rowData = new Object[5];

        rowData[0] = file.getName();
        rowData[1] = file.getSize();
        rowData[2] = formatDateTime(file.getModifiedTime());
        rowData[3] = formatTags(file.getTags());
        rowData[4] = file.getVersionCount();

        tableModel.addRow(rowData);
    }

    // formats date/time for display
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    // formats tags for display
    private String formatTags(java.util.Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }

        return String.join(", ", tags);
    }

    // handles double-click on a file
    private void handleDoubleClick() {
        int selectedRow = fileTable.getSelectedRow();

        if (selectedRow >= 0) {
            // convert view index to model index for sorting
            int modelRow = fileTable.convertRowIndexToModel(selectedRow);
            String fileName = (String) tableModel.getValueAt(modelRow, 0);

            // notify listener (will be handled by MainWindow)
            if (doubleClickListener != null) {
                doubleClickListener.onFileDoubleClicked(fileName);
            }
        }
    }

    // gets the currently selected file name
    public String getSelectedFileName() {
        int selectedRow = fileTable.getSelectedRow();

        if (selectedRow >= 0) {
            // convert view index to model index for sorting
            int modelRow = fileTable.convertRowIndexToModel(selectedRow);
            return (String) tableModel.getValueAt(modelRow, 0);
        }

        return null;
    }

    // gets the currently selected file
    public File getSelectedFile() {
        String fileName = getSelectedFileName();

        if (fileName != null) {
            if (currentView == ViewMode.ALL_FILES) {
                return fileSystem.getDirectory().getFile(fileName);
            } else {
                return fileSystem.getRecycleBin().getFile(fileName);
            }
        }

        return null;
    }

    // sets the view mode (all files or recycle bin)
    public void setViewMode(ViewMode mode) {
        this.currentView = mode;
        loadFiles();
    }

    // gets current view mode
    public ViewMode getViewMode() {
        return currentView;
    }

    // refreshes the file list
    public void refresh() {
        loadFiles();
    }

    // adds selection listener
    public void addSelectionListener(javax.swing.event.ListSelectionListener listener) {
        fileTable.getSelectionModel().addListSelectionListener(listener);
    }

    // double-click listener interface
    public interface FileDoubleClickListener {
        void onFileDoubleClicked(String fileName);
    }

    private FileDoubleClickListener doubleClickListener;

    public void setDoubleClickListener(FileDoubleClickListener listener) {
        this.doubleClickListener = listener;
    }
}