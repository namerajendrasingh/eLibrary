package view;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import util.DBUtil;
import util.PDFUploadUtility;

public class PDFUploadDialog extends JDialog {
    
    private JTextField titleField;
    private JComboBox<String> categoryCombo;  // ✅ String only (no ClassCastException)
    private JTextField filePathField;
    private JButton browseBtn, uploadBtn;
    private ActionListener onUploadSuccess;
    
    public PDFUploadDialog(Window parent, ActionListener onUploadSuccess) {
        super(parent, "📚 Upload PDF Book", Dialog.ModalityType.APPLICATION_MODAL);
        this.onUploadSuccess = onUploadSuccess;
        initUI();
        loadCategories();  // ✅ Load categories dynamically
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(500, 300));
        
        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(new JLabel("📖 Book Title: "));
        titleField = new JTextField(25);
        titlePanel.add(titleField);
        add(titlePanel, BorderLayout.NORTH);
        
        // Category + File Panel
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // ✅ DYNAMIC Category (Empty initially)
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("📂 Category: "));
        categoryCombo = new JComboBox<>();  // ✅ Empty - will be populated
        categoryCombo.setPreferredSize(new Dimension(150, 25));
        categoryPanel.add(categoryCombo);
        centerPanel.add(categoryPanel);
        
        // File
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(new JLabel("📄 PDF File: "));
        filePathField = new JTextField(20);
        filePathField.setEditable(false);
        browseBtn = new JButton("📂 Browse");
        filePanel.add(filePathField);
        filePanel.add(browseBtn);
        centerPanel.add(filePanel);
        add(centerPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        uploadBtn = new JButton("🚀 Upload PDF");
        JButton cancelBtn = new JButton("❌ Cancel");
        
        uploadBtn.setEnabled(false);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(uploadBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Event Handlers
        browseBtn.addActionListener(e -> browseFile());
        uploadBtn.addActionListener(e -> uploadPDF());
        cancelBtn.addActionListener(e -> dispose());
        
        // Enable upload when fields are ready
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkUploadReady(); }
            public void removeUpdate(DocumentEvent e) { checkUploadReady(); }
            public void changedUpdate(DocumentEvent e) { checkUploadReady(); }
        });
    }
    
    /**
     * ✅ DYNAMICALLY LOAD CATEGORIES FROM DATABASE
     */
    private void loadCategories() {
        new Thread(() -> {
            List<String> categories = getAllCategoriesFromDB();
            
            SwingUtilities.invokeLater(() -> {
                categoryCombo.removeAllItems();
                if (categories.isEmpty()) {
                    categoryCombo.addItem("❌ No Categories");
                } else {
                    for (String category : categories) {
                        categoryCombo.addItem(category);  // ✅ String only
                    }
                }
                checkUploadReady();  // Re-check after loading
            });
        }).start();
    }
    
    /**
     * ✅ DATABASE METHOD: Get all categories as Strings
     */
    private List<String> getAllCategoriesFromDB() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT category_name FROM book_category ORDER BY category_name ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            categories.add("❌ Database Error");
        }
        return categories;
    }
    
    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            filePathField.setText(chooser.getSelectedFile().getAbsolutePath());
            checkUploadReady();
        }
    }
    
    private void checkUploadReady() {
        boolean titleOk = !titleField.getText().trim().isEmpty();
        boolean fileOk = !filePathField.getText().trim().isEmpty() && 
                        filePathField.getText().endsWith(".pdf");
        boolean categoryOk = categoryCombo.getItemCount() > 0 && 
                           !((String)categoryCombo.getSelectedItem()).startsWith("❌");
        
        uploadBtn.setEnabled(titleOk && fileOk && categoryOk);
    }
    
    private void uploadPDF() {
        String title = titleField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        
        // ✅ VALIDATION
        if (category == null || category.startsWith("❌")) {
            JOptionPane.showMessageDialog(this, "❌ Please select a valid category!", 
                "Invalid Category", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File pdfFile = new File(filePathField.getText());
        
        if (PDFUploadUtility.uploadPDF(title, category, pdfFile)) {  // ✅ String works perfectly
            if (onUploadSuccess != null) {
                onUploadSuccess.actionPerformed(new ActionEvent(this, 0, "uploadSuccess"));
            }
            JOptionPane.showMessageDialog(this, "✅ PDF uploaded successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Upload failed. Check logs.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ✅ PUBLIC: Refresh categories (call after admin adds new)
    public void refreshCategories() {
        loadCategories();
    }
}
