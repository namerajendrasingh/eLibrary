package view;

import java.awt.BorderLayout;
import java.util.List;

//view/SearchPanel.java
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import model.Book;
import model.BookDAO;

public class SearchPanel extends JPanel {

 private JTextField searchField;
 private JTable table;
 private DefaultTableModel tableModel;
 private TableRowSorter<DefaultTableModel> rowSorter;
 private BookDAO bookDAO;

 public SearchPanel() {
     this.bookDAO = new BookDAO();
     initUI();
     loadAllBooks();
 }

 private void initUI() {
     setLayout(new BorderLayout(10, 10));

     JPanel topPanel = new JPanel(new BorderLayout(5, 5));
     JLabel searchLabel = new JLabel("Search (Title / Author / ISBN / Category): ");
     searchField = new JTextField();

     topPanel.add(searchLabel, BorderLayout.WEST);
     topPanel.add(searchField, BorderLayout.CENTER);

     add(topPanel, BorderLayout.NORTH);

     tableModel = new DefaultTableModel(
             new Object[]{"ID", "Title", "Author", "ISBN", "Category", "Total", "Available", "File Path"},
             0
     ) {
         @Override
         public boolean isCellEditable(int row, int col) { return false; }
     };

     table = new JTable(tableModel);
     table.setRowHeight(24);

     rowSorter = new TableRowSorter<>(tableModel);
     table.setRowSorter(rowSorter);

     JScrollPane scrollPane = new JScrollPane(table);
     add(scrollPane, BorderLayout.CENTER);

     // Live filter
     searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
         private void updateFilter() {
             String text = searchField.getText();
             if (text == null || text.isBlank()) {
                 rowSorter.setRowFilter(null);
             } else {
                 rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); // case-insensitive
             }
         }
         @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
         @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
         @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
     });
 }

 private void loadAllBooks() {
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
                 b.getFilePath()
         });
     }
 }
}
