package view;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import model.Book;
import model.BookDAO;
import util.PDFReader;
import util.PDFUtils;

public class BookPanel extends JPanel {

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton readBtn, downloadBtn, refreshBtn, addBtn, deleteBtn;
    private JButton prevBtn, nextBtn;  // ✅ NEW PAGINATION BUTTONS
    private JLabel pageInfoLabel;      // ✅ NEW PAGE INFO
    private BookDAO bookDAO;
    private String userRole;

    // ✅ PAGINATION STATE
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalRecords = 0;
    private boolean hasMoreRecords = true;

    public BookPanel(String role) {
        this.userRole = role;
        this.bookDAO = new BookDAO();
        initUI();
        loadPage(0);  // Load first page
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Table model
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "ISBN", "Category", "Total", "Available", "File Path"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        
        // ✅ SORTING (same as before)
        sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);
        
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            sorter.setComparator(i, createColumnComparator(i));
        }
        
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        bookTable.getTableHeader().setReorderingAllowed(true);
        
        bookTable.setRowHeight(28);
        bookTable.setGridColor(new java.awt.Color(230, 230, 230));
        bookTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        bookTable.setShowGrid(true);
        bookTable.setIntercellSpacing(new java.awt.Dimension(0, 1));

        // Column alignment (same as before)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(javax.swing.JLabel.LEFT);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);

        for (int i = 0; i < bookTable.getColumnCount(); i++) {
            switch (i) {
                case 1, 2, 4 -> bookTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
                case 7 -> bookTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
                default -> bookTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Header styling
        bookTable.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        bookTable.getTableHeader().setBackground(new java.awt.Color(248, 249, 250));
        bookTable.getTableHeader().setForeground(new java.awt.Color(33, 33, 33));

        // Column widths
        int[] widths = {60, 200, 150, 120, 120, 80, 90, 250};
        for (int i = 0; i < widths.length; i++) {
            bookTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        add(scrollPane, BorderLayout.CENTER);

        // ✅ NEW PAGINATION PANEL
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        paginationPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        prevBtn = new JButton("⬅️ Previous");
        nextBtn = new JButton("➡️ Next");
        pageInfoLabel = new JLabel("Page 1 (1-50 of 0)");
        
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        
        prevBtn.addActionListener(e -> previousPage());
        nextBtn.addActionListener(e -> nextPage());

        paginationPanel.add(prevBtn);
        paginationPanel.add(nextBtn);
        paginationPanel.add(pageInfoLabel);

        // Main button panel (same as before)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));

        readBtn = new JButton("📖 Read Book");
        downloadBtn = new JButton("⬇️ Download PDF");
        refreshBtn = new JButton("🔄 Refresh");
        addBtn = new JButton("➕ Add Book");
        deleteBtn = new JButton("🗑️ Delete Book");

        readBtn.addActionListener(e -> readSelectedBook());
        downloadBtn.addActionListener(e -> downloadSelectedBook());
        refreshBtn.addActionListener(e -> loadCurrentPage());
        addBtn.addActionListener(e -> showAddBookDialog());
        deleteBtn.addActionListener(e -> deleteSelectedBook());

        if ("ADMIN".equalsIgnoreCase(userRole)) {
            buttonPanel.add(addBtn);
            buttonPanel.add(deleteBtn);
            buttonPanel.add(downloadBtn);
        }
        buttonPanel.add(readBtn);
        buttonPanel.add(refreshBtn);

        // ✅ Combine pagination + buttons
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(paginationPanel, BorderLayout.WEST);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * ✅ FIXED Comparator
     */
    private Comparator<Object> createColumnComparator(int columnIndex) {
        return (o1, o2) -> {
            return switch (columnIndex) {
                case 0, 5, 6 -> {
                    int n1 = o1 instanceof Number ? ((Number) o1).intValue() : 0;
                    int n2 = o2 instanceof Number ? ((Number) o2).intValue() : 0;
                    yield Integer.compare(n1, n2);
                }
                case 1, 2, 3, 4, 7 -> {
                    String s1 = o1 != null ? o1.toString() : "";
                    String s2 = o2 != null ? o2.toString() : "";
                    yield s1.compareToIgnoreCase(s2);
                }
                default -> 0;
            };
        };
    }

    /**
     * ✅ NEW: Load specific page with offset
     */
    private void loadPage(int page) {
        currentPage = page;
        tableModel.setRowCount(0);
        
        // Get paginated data from DAO
        List<Book> books = bookDAO.getBooksWithOffset(page * pageSize, pageSize);
        totalRecords = bookDAO.getTotalBookCount();  // NEW METHOD NEEDED IN DAO
        
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    b.getCategory(),
                    b.getTotalCopies(),
                    b.getAvailableCopies(),
                    b.getFilePath() != null ? b.getFilePath() : "—"
            });
        }
        
        updatePaginationControls();
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
    }

    /**
     * ✅ NEW: Load current page (for refresh)
     */
    private void loadCurrentPage() {
        loadPage(currentPage);
    }

    /**
     * ✅ NEW: Previous page
     */
    private void previousPage() {
        if (currentPage > 0) {
            loadPage(currentPage - 1);
        }
    }

    /**
     * ✅ NEW: Next page
     */
    private void nextPage() {
        if (hasMoreRecords) {
            loadPage(currentPage + 1);
        }
    }

    /**
     * ✅ NEW: Update pagination UI
     */
    private void updatePaginationControls() {
        int startRecord = currentPage * pageSize + 1;
        int endRecord = Math.min(startRecord + pageSize - 1, totalRecords);
        
        pageInfoLabel.setText(String.format("Page %d (%d-%d of %d)", 
            currentPage + 1, startRecord, endRecord, totalRecords));
        
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled(endRecord < totalRecords);
    }

    // ✅ UPDATED: All methods use model row index
    private void readSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to read.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String filePath = (String) tableModel.getValueAt(bookTable.convertRowIndexToModel(row), 7);
        if (filePath == null || filePath.equals("—") || filePath.isBlank()) {
            JOptionPane.showMessageDialog(this, "No PDF file configured for this book.", "No File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PDFReader.openPDF(filePath);
    }

    private void downloadSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to download.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String filePath = (String) tableModel.getValueAt(bookTable.convertRowIndexToModel(row), 7);
        if (filePath == null || filePath.equals("—") || filePath.isBlank()) {
            JOptionPane.showMessageDialog(this, "No PDF file configured for this book.", "No File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PDFUtils.downloadBook(filePath);
    }

    private void deleteSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "❌ Please select a book to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(row);
        int bookId = (Integer) tableModel.getValueAt(modelRow, 0);
        String title = (String) tableModel.getValueAt(modelRow, 1);
        int totalCopies = (Integer) tableModel.getValueAt(modelRow, 5);

        // ... (rest of delete logic remains same)
        if (totalCopies > 0) {
            Object[] options = {"🔧 Set Copies to 0", "❌ Cancel"};
            int choice = JOptionPane.showOptionDialog(this,
                String.format("<html><b>📦 %s has %d copies in inventory</b><br><br>" +
                             "• To delete, set Total Copies to 0 first</html>", title, totalCopies),
                "Book Inventory Check", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);
            if (choice == 0) {
                bookDAO.setTotalCopiesToZero(bookId);
                JOptionPane.showMessageDialog(this, "✅ Copies reset. Now check active issues.", "Ready for Next Step", JOptionPane.INFORMATION_MESSAGE);
                loadCurrentPage();
                return;
            }
            return;
        }

        int activeIssuesCount = bookDAO.countActiveIssuesForBook(bookId);
        if (activeIssuesCount > 0) {
            Object[] options = {"🔍 View Active Issues", "⚠️ Force Delete Issues", "❌ Cancel"};
            int choice = JOptionPane.showOptionDialog(this,
                String.format("<html><b>⚠️ %d active issues reference this book</b><br><br>" +
                             "• Book: <b>%s</b> (ID: %d)</html>", activeIssuesCount, title, bookId),
                "Foreign Key Constraint", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);

            if (choice == 0) {
                showActiveIssuesForBook(bookId, title);
            } else if (choice == 1) {
                forceDeleteBookWithIssues(bookId, title);
            }
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("🗑️ Delete book permanently?\n\n📖 %s\n🆔 ID: %d\n📦 Copies: %d", title, bookId, totalCopies),
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = bookDAO.deleteBook(bookId);
            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Book deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCurrentPage();  // ✅ Reload current page
            } else {
                JOptionPane.showMessageDialog(this, "❌ Delete failed. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ... (showActiveIssuesForBook, forceDeleteBookWithIssues, showAddBookDialog remain same)
    
    private void showActiveIssuesForBook(int bookId, String title) {
        List<Integer> issueIds = bookDAO.getActiveIssueIdsForBook(bookId);
        String issuesList = issueIds.stream()
            .map(id -> "• Issue #" + id)
            .collect(java.util.stream.Collectors.joining("\n"));
        
        JOptionPane.showMessageDialog(this,
            String.format("<html><h3>📋 Active Issues for '%s'</h3><br>%s<br><br>" +
                         "<i>Return these books first, then try deleting again.</i></html>",
                         title, issuesList),
            "Active Issues", JOptionPane.INFORMATION_MESSAGE);
    }

    private void forceDeleteBookWithIssues(int bookId, String title) {
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("<html><b>⚠️ DANGER: Force Delete Book</b><br><br>" +
                         "📖 %s (ID: %d)<br><br>This will DELETE ALL related issues & book PERMANENTLY!</html>",
                         title, bookId),
            "Force Delete Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = bookDAO.forceDeleteBookWithIssues(bookId);
            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Book & issues deleted!", "Force Delete Complete", JOptionPane.INFORMATION_MESSAGE);
                loadCurrentPage();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Force delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New Book", Dialog.ModalityType.APPLICATION_MODAL);
        AddBookPanel addPanel = new AddBookPanel() {
            @Override
            protected void onBookSaved() {
                loadCurrentPage();  // ✅ Reload current page
                dialog.dispose();
            }
        };
        dialog.setContentPane(addPanel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(500, 400));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void loadBooks() {
        // Preserve current sort state
        List<SortKey> currentSortKeys = (List<SortKey>) sorter.getSortKeys();
        
        tableModel.setRowCount(0);
        List<Book> books = bookDAO.getAllBooks();
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    b.getCategory(),
                    b.getTotalCopies(),
                    b.getAvailableCopies(),
                    b.getFilePath() != null ? b.getFilePath() : "—"
            });
        }
        
        // Restore sorting after data reload
        sorter.setSortKeys(currentSortKeys);
    }

    public void refreshData() {
        int selectedRow = bookTable.getSelectedRow();
        List<SortKey> currentSortKeys = (List<SortKey>) sorter.getSortKeys();
        loadBooks();
        
        // Restore selection if possible
        if (selectedRow >= 0 && selectedRow < bookTable.getRowCount()) {
            bookTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
        
        sorter.setSortKeys(currentSortKeys);
    }
}
