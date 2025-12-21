package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class PDFUtils {

    /**
     * Lets the user choose where to download/save the PDF,
     * then copies the file there.
     */
    public static void downloadBook(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            JOptionPane.showMessageDialog(null, "No file path specified for this book.");
            return;
        }

        File source = new File(filePath);
        if (!source.exists()) {
            JOptionPane.showMessageDialog(null, "Source PDF not found:\n" + filePath);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Book As");
        chooser.setSelectedFile(new File(source.getName())); // default name

        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            try {
                // Ensure .pdf extension
                if (!dest.getName().toLowerCase().endsWith(".pdf")) {
                    dest = new File(dest.getAbsolutePath() + ".pdf");
                }

                Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(null, "Book downloaded to:\n" + dest.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error downloading book:\n" + ex.getMessage());
            }
        }
    }
}
