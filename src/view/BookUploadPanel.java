package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import util.CommonMethods;
import util.PDFUploadUtility;

public class BookUploadPanel extends JPanel {
    
    private JTabbedPane tabbedPane;
    private PDFUploadDialog uploadDialog;
    
    public BookUploadPanel() {
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("BOOK UPLOAD CENTER", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(52, 73, 94));
        add(headerLabel, BorderLayout.NORTH);
        
        // Tabbed Interface
        tabbedPane = new JTabbedPane();
        
        // Upload Tab
        JPanel uploadTab = createUploadTab();
        tabbedPane.addTab("📤 Upload PDF", uploadTab);
        
        // Instructions Tab
        JPanel instructionsTab = createInstructionsTab();
        tabbedPane.addTab("📖 Instructions", instructionsTab);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createUploadTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Upload Button (Large & Prominent)
        JButton uploadBtn = new JButton("UPLOAD NEW PDF BOOK");
        uploadBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        uploadBtn.setBackground(new Color(46, 204, 113));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setFocusPainted(false);
        uploadBtn.setPreferredSize(new Dimension(300, 60));
     
     // Quick Actions Panel
        JPanel quickActions = new JPanel(new GridLayout(2, 2, 10, 10));
        quickActions.setBorder(BorderFactory.createTitledBorder(null, "Quick Actions", 
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 14),  // ✅ BOLD FONT
            new Color(52, 73, 94)));  // ✅ DARK COLOR

        
        
        JButton uploadAndAddBtn = new JButton("📚 Upload + Add Book Record");
        JButton bulkUploadBtn = new JButton("📦 Bulk Upload PDFs");
        JButton viewFoldersBtn = new JButton("📁 View Folders");
        JButton refreshBtn = new JButton("🔄 Refresh");
        
        quickActions.add(uploadAndAddBtn);
        quickActions.add(bulkUploadBtn);
        quickActions.add(viewFoldersBtn);
        quickActions.add(refreshBtn);
        
        // Main Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        actionPanel.add(uploadBtn);
        panel.add(actionPanel, BorderLayout.NORTH);
        panel.add(quickActions, BorderLayout.CENTER);
        
        // Event Handlers
        uploadBtn.addActionListener(e -> showUploadDialog());
        uploadAndAddBtn.addActionListener(e -> showUploadDialog());
        bulkUploadBtn.addActionListener(e -> showBulkUploadDialog());
        viewFoldersBtn.addActionListener(e -> PDFUploadUtility.openFolder());
        refreshBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "✅ Ready!"));
        
        return panel;
    }
    
    private JPanel createInstructionsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Instructions
        String instructions = ""+
            "📚 HOW TO UPLOAD BOOKS\n"+
            
            "1️⃣ Click 🚀 UPLOAD NEW PDF BOOK\n"+
            "2️⃣ Enter Book Title & Select Category\n"+
            "3️⃣ Browse & Select PDF File\n"+
            "4️⃣ Click UPLOAD ✅\n"+
            
            "📁 Files saved automatically: in \n"+
            "•eg. D:\\LibraryPDF\\English\\English_BookTitle.pdf\n"+
            "• D:\\LibraryPDF\\Engineering\\Engineering_Java.pdf\n"+
            
            "🔗 After upload, go to Books tab to add DB record\n"+
            
            "📂 More Than 90+ Book Categories Available:\n";
        
        JTextArea textArea = new JTextArea(instructions);
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(new Color(248, 249, 250));
        textArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(new JScrollPane(textArea));
        
        return panel;
    }
    
    private void showUploadDialog() {
        uploadDialog = new PDFUploadDialog(
            SwingUtilities.getWindowAncestor(this),
            e -> {
                tabbedPane.setSelectedIndex(1); // Switch to Instructions
                CommonMethods.showMessage(this, "\"✅ PDF uploaded successfully!\\n\\n\" +\r\n"
                		+ "                    \"👉 Now go to Books tab to add record\", \r\n"
                		+ "                    \"Upload Complete\"");
               
            }
        );
        uploadDialog.setVisible(true);
    }
    
    private void showBulkUploadDialog() {
        BulkUploadDialog bulkDialog = new BulkUploadDialog(
            SwingUtilities.getWindowAncestor(this),
            e -> {
                int uploadedCount = Integer.parseInt(e.getActionCommand());
                CommonMethods.showMessage(this, 
                    String.format("✅ Bulk Upload Complete!\n📊 %d PDFs uploaded successfully!\n\n👉 Go to Books tab to add records", uploadedCount));
            }
        );
        bulkDialog.setVisible(true);
    }


}

