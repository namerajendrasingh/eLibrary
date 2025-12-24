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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.PDFUploadUtility;

public class PDFUploadDialog extends JDialog {
    
    private JTextField titleField;
    private JComboBox<String> categoryCombo;
    private JTextField filePathField;
    private JButton browseBtn, uploadBtn;
    private ActionListener onUploadSuccess;
    
    public PDFUploadDialog(Window parent, ActionListener onUploadSuccess) {
        super(parent, "📚 Upload PDF Book", Dialog.ModalityType.APPLICATION_MODAL);
        this.onUploadSuccess = onUploadSuccess;
        initUI();
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
        
        // Category
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("📂 Category: "));
        categoryCombo = new JComboBox<>(PDFUploadUtility.CATEGORIES);
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
        titleField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkUploadReady(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkUploadReady(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkUploadReady(); }
        });
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
        uploadBtn.setEnabled(
            !titleField.getText().trim().isEmpty() &&
            !filePathField.getText().trim().isEmpty() &&
            filePathField.getText().endsWith(".pdf")
        );
    }
    
    private void uploadPDF() {
        String title = titleField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        File pdfFile = new File(filePathField.getText());
        
        if (PDFUploadUtility.uploadPDF(title, category, pdfFile)) {
            if (onUploadSuccess != null) {
                onUploadSuccess.actionPerformed(new ActionEvent(this, 0, "uploadSuccess"));
            }
            dispose();
        }
    }
}

