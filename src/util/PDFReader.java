package util;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

//util/PDFReader.java
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import view.PDFViewerFrame;

public class PDFReader {

 public static void openPDF(String filePath) {
     if (filePath == null || filePath.isBlank()) {
         JOptionPane.showMessageDialog(null, "No PDF file configured for this book.");
         return;
     }

     File file = new File(filePath);
     if (!file.exists() || !file.isFile()) {
         JOptionPane.showMessageDialog(null, "PDF not found:\n" + filePath);
         return;
     }

     SwingWorker<PDDocument, Void> worker = new SwingWorker<>() {
         @Override
         protected PDDocument doInBackground() throws Exception {
             // Heavy I/O work off the EDT
             return Loader.loadPDF(file);
         }

         @Override
         protected void done() {
             try {
                 PDDocument document = get(); // result from doInBackground
                 PDFViewerFrame frame = new PDFViewerFrame(document, filePath);
                 frame.setVisible(true);      // on EDT
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(null,
                         "Error opening PDF:\n" + e.getMessage());
                 e.printStackTrace();
             }
         }
     };

     worker.execute();
 }
}
