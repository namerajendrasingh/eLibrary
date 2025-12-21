package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import model.Book;
import model.BookDAO;

public class AddBookPanel extends JPanel {

    private JTextField titleField;
    private JTextField authorField;
    private JTextField isbnField;
    private JTextField categoryField;
    private JSpinner totalCopiesSpinner;
    private JTextField filePathField;
    private JButton browseBtn;
    private JButton saveBtn;
    private JButton clearBtn;

    private final BookDAO bookDAO;

    public AddBookPanel() {
        this.bookDAO = new BookDAO();
        initUI();
    }

    // *** NEW: hook method so parent (BookPanel/JDialog) can refresh table or close dialog ***
    protected void onBookSaved() {
        // default: do nothing; override when you create this panel in BookPanel
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Title
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Title *:"), gbc);
        gbc.gridx = 1;
        titleField = new JTextField(25);
        formPanel.add(titleField, gbc);
        row++;

        // Author
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Author *:"), gbc);
        gbc.gridx = 1;
        authorField = new JTextField(25);
        formPanel.add(authorField, gbc);
        row++;

        // ISBN
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        isbnField = new JTextField(25);
        formPanel.add(isbnField, gbc);
        row++;

        // Category
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryField = new JTextField(25);
        formPanel.add(categoryField, gbc);
        row++;

        // Total Copies
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Total Copies *:"), gbc);
        gbc.gridx = 1;
        totalCopiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        ((JSpinner.DefaultEditor) totalCopiesSpinner.getEditor())
                .getTextField().setColumns(5);
        formPanel.add(totalCopiesSpinner, gbc);
        row++;

        // File path (PDF)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("PDF File Path:"), gbc);
        gbc.gridx = 1;
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePathField = new JTextField(20);
        browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> chooseFile());
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(browseBtn, BorderLayout.EAST);
        formPanel.add(filePanel, gbc);
        row++;

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Save Book");
        clearBtn = new JButton("Clear");

        saveBtn.addActionListener(e -> saveBook());
        clearBtn.addActionListener(e -> clearForm());

        buttonPanel.add(saveBtn);
        buttonPanel.add(clearBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Book PDF");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            filePathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void saveBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String category = categoryField.getText().trim();
        int totalCopies = (int) totalCopiesSpinner.getValue();
        String filePath = filePathField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Title and Author are required.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn.isEmpty() ? null : isbn);
        book.setCategory(category.isEmpty() ? null : category);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(totalCopies);
        book.setFilePath(filePath.isEmpty() ? null : filePath);

        boolean ok = false;
        try {
            ok = bookDAO.addBook(book);   // this must insert into DB and return true on success [web:549][web:550]
        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            clearForm();
            onBookSaved();                // *** NEW: notify parent so it can refresh table or close dialog
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to add book. Check console/logs.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        categoryField.setText("");
        totalCopiesSpinner.setValue(1);
        filePathField.setText("");
    }
}
