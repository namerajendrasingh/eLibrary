package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

public class PDFUploadUtility {
    
    private static final String BASE_PATH = "D:\\LibraryPDF";
    public static final String[] CATEGORIES = {
        "English", "Novel", "Engineering", "Maths", "Science", "History", "Computer", "Medical"
    };
    
    public static boolean uploadPDF(String bookTitle, String category, File pdfFile) {
        try {
            // ✅ Create base folder if not exists
            Path baseDir = Paths.get(BASE_PATH);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
                System.out.println("✅ Created base folder: " + BASE_PATH);
            }
            
            // ✅ Create category folder
            Path categoryDir = baseDir.resolve(category);
            if (!Files.exists(categoryDir)) {
                Files.createDirectories(categoryDir);
                System.out.println("✅ Created category folder: " + categoryDir);
            }
            
            // ✅ Generate filename: Category + Title + .pdf
            String safeTitle = bookTitle.replaceAll("[^a-zA-Z0-9\\s]", "").trim()
                                       .replaceAll("\\s+", "_");
            String fileName = category + "_" + safeTitle + ".pdf";
            Path targetPath = categoryDir.resolve(fileName);
            
            // ✅ Copy PDF to target location
            Files.copy(pdfFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("✅ PDF uploaded successfully: " + targetPath);
            JOptionPane.showMessageDialog(null, 
                "✅ PDF uploaded!\nLocation: " + targetPath.toString(), 
                "Upload Success", JOptionPane.INFORMATION_MESSAGE);
            
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "❌ Upload failed!\n" + e.getMessage(), 
                "Upload Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
 // ✅ NEW: Add this static method
    public static void openFolder() {
        try {
            java.awt.Desktop.getDesktop().open(new java.io.File(BASE_PATH));
        } catch (java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "❌ Cannot open folder:\n" + e.getMessage(), 
                "Folder Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public static String getFullPath(String category, String bookTitle) {
        String safeTitle = bookTitle.replaceAll("[^a-zA-Z0-9\\s]", "").trim()
                                   .replaceAll("\\s+", "_");
        return Paths.get(BASE_PATH, category, category + "_" + safeTitle + ".pdf").toString();
    }
}

