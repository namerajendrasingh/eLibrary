package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.BookCategory;
import model.BookCategoryDAO;
import util.PDFUploadUtility;

public class BulkUploadDialog extends JDialog {
    
    private JComboBox<BookCategory> categoryCombo; 
    private JTextArea statusArea;
    private JButton selectFolderBtn, startUploadBtn;
    private List<File> selectedPDFs;
    private int uploadedCount = 0;
    private ActionListener onComplete;
    private final BookCategoryDAO categoryDAO; 
    
    public BulkUploadDialog(Window parent, ActionListener onComplete) {
        super(parent, "📦 Bulk Upload PDFs", Dialog.ModalityType.APPLICATION_MODAL);
        this.onComplete = onComplete;
        this.categoryDAO = new BookCategoryDAO();  // ✅ Initialize DAO
        this.selectedPDFs = new ArrayList<>();
        initUI();
        loadCategories();  // ✅ Load categories on startup
    }
    
    private void initUI() {
        
        setLayout(new BorderLayout(15, 15));
        setPreferredSize(new Dimension(700, 500));  // ✅ FIXED STANDARD WIDTH
        setMinimumSize(new Dimension(700, 500));
        setMaximumSize(new Dimension(700, 600));
        
        // ✅ CENTER ON SCREEN
        setLocationRelativeTo(null);
        
        
        // Header
        JLabel header = new JLabel("📦 BULK UPLOAD MULTIPLE PDFs", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setForeground(new Color(52, 73, 94));
        add(header, BorderLayout.NORTH);
        
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // ✅ DYNAMIC Category Selection (REPLACED)
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("📂 Category for ALL PDFs: "));
        categoryCombo = new JComboBox<>();  // ✅ Empty initially
        categoryCombo.setPreferredSize(new Dimension(220, 28));
        categoryPanel.add(categoryCombo);
        mainPanel.add(categoryPanel, BorderLayout.NORTH);
        
        // Folder Selection + Status
        JPanel folderPanel = new JPanel(new BorderLayout(10, 10));
        
        selectFolderBtn = new JButton("📁 Select Folder with PDFs");
        selectFolderBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectFolderBtn.setPreferredSize(new Dimension(220, 40));
        
        folderPanel.add(selectFolderBtn, BorderLayout.NORTH);
        
        // Status Area
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusArea.setText("👆 Click 'Select Folder' to choose a folder containing PDF files...\n");
        folderPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        
        mainPanel.add(folderPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        startUploadBtn = new JButton("🚀 START BULK UPLOAD");
        startUploadBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startUploadBtn.setBackground(new Color(46, 204, 113));
        startUploadBtn.setForeground(Color.WHITE);
        startUploadBtn.setEnabled(false);
        
        JButton cancelBtn = new JButton("❌ Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(startUploadBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Event Handlers
        selectFolderBtn.addActionListener(e -> selectPDFFolder());
        startUploadBtn.addActionListener(e -> startBulkUpload());
        cancelBtn.addActionListener(e -> dispose());
    }
    
    private void selectPDFFolder() {
        JFileChooser chooser = new JFileChooser();
        
        // ✅ FIXED: Allow BOTH folders AND individual PDF files
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle("📁 Select Folder OR Individual PDF File");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files & Folders", "pdf"));
        
        // ✅ Show PDFs by default
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            selectedPDFs = findPDFsInFolder(selected);
            
            // ✅ FIXED STATUS DISPLAY
            statusArea.setText(String.format(
                "✅ Selected: %s\n" +
                "📊 Found %d UNIQUE PDF files\n\n" +
                "📋 PDF Files (%d):\n",
                selected.getAbsolutePath(), selectedPDFs.size(), selectedPDFs.size()
            ));
            
            // Show first 8 files with sizes
            for (int i = 0; i < Math.min(8, selectedPDFs.size()); i++) {
                File pdf = selectedPDFs.get(i);
                long sizeMB = pdf.length() / (1024 * 1024);
                statusArea.append(String.format("  %d. %s (%d.%d MB)\n", 
                    i + 1, pdf.getName(), sizeMB, (pdf.length() % (1024*1024)) / 102400));
            }
            
            if (selectedPDFs.size() > 8) {
                statusArea.append(String.format("\n... and %d more files\n", 
                    selectedPDFs.size() - 8));
            }
            
            startUploadBtn.setEnabled(!selectedPDFs.isEmpty());
        }
    }


    
    private List<File> findPDFsInFolder(File folder) {
        Set<File> uniquePDFs = new HashSet<>();  // ✅ NO DUPLICATES
        scanFolderForPDFs(folder, uniquePDFs);
        
        List<File> pdfs = new ArrayList<>(uniquePDFs);
        return pdfs;
    }

    
    private void scanFolderForPDFs(File folder, Set<File> pdfs) {
        // Direct PDFs in this folder
        File[] files = folder.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".pdf"));
        
        if (files != null) {
            for (File file : files) {
                pdfs.add(file);
            }
        }
        
        // Subfolders (unlimited depth, but safe)
        File[] subdirs = folder.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                scanFolderForPDFs(subdir, pdfs);
            }
        }
    }
    
    
    /**
     * ✅ DYNAMICALLY LOAD CATEGORIES FROM DB
     */
    private void loadCategories() {
        new Thread(() -> {
            List<BookCategory> categories = categoryDAO.getAllCategories();
            
            SwingUtilities.invokeLater(() -> {
                categoryCombo.removeAllItems();
                if (categories.isEmpty()) {
                    categoryCombo.addItem(new BookCategory(0, "❌ No Categories Found"));
                    statusArea.append("⚠️ No categories in database. Please add categories first.\n");
                } else {
                    for (BookCategory cat : categories) {
                        categoryCombo.addItem(cat);
                    }
                    statusArea.append(String.format("✅ Loaded %d categories from database\n", categories.size()));
                }
            });
        }).start();
    }

    private void startBulkUpload() {
    	BookCategory selectedCategory = (BookCategory) categoryCombo.getSelectedItem();
        if (selectedCategory == null || selectedCategory.getId() == 0) {
            JOptionPane.showMessageDialog(this, "❌ Please select a valid category!", "Invalid Category", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // ✅ FIXED: Extract String name before passing
        String categoryName = selectedCategory.getName();  // ← String!
        
        statusArea.append(String.format("\n🚀 Starting bulk upload to category: %s\n\n", categoryName));
        
        new Thread(() -> {
            uploadedCount = 0;
            int total = selectedPDFs.size();
            
            for (int i = 0; i < total; i++) {
                File pdf = selectedPDFs.get(i);
                statusArea.append(String.format("⏳ [%d/%d] Uploading: %s\n", 
                    i + 1, total, pdf.getName()));
                
                // ✅ FIXED: Pass String categoryName (not BookCategory)
                boolean success = PDFUploadUtility.uploadPDF(
                    extractTitleFromFilename(pdf.getName()), 
                    categoryName,  // ← String, not BookCategory!
                    pdf
                );
                
                if (success) {
                    uploadedCount++;
                    statusArea.append("✅ SUCCESS\n\n");
                } else {
                    statusArea.append("❌ FAILED\n\n");
                }
                
                statusArea.setCaretPosition(statusArea.getDocument().getLength());
            }
            
            SwingUtilities.invokeLater(() -> {
                statusArea.append(String.format(
                    "\n🎉 BULK UPLOAD COMPLETE!\n✅ %d/%d PDFs uploaded successfully!\n",
                    uploadedCount, total
                ));
                
                if (onComplete != null) {
                    onComplete.actionPerformed(new ActionEvent(this, 0, String.valueOf(uploadedCount)));
                }
            });
        }).start();
    }
    
    private String extractTitleFromFilename(String filename) {
        String name = filename.toLowerCase().replace(".pdf", "").trim();
        
        // ✅ FIXED: Safe category name extraction
        BookCategory cat = (BookCategory) categoryCombo.getSelectedItem();
        if (cat != null && cat.getId() != 0) {
            String category = cat.getName().toLowerCase();
            if (name.startsWith(category)) {
                name = name.substring(category.length()).trim();
            }
        }
        return name.replace("_", " ").replace("-", " ").trim();
    }

}

