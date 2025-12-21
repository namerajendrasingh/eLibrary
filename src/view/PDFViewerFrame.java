package view;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PDFViewerFrame extends JFrame {

    private final PDDocument document;
    private final PDFRenderer renderer;
    private int currentPage = 0;
    private JLabel imageLabel;
    private JButton prevButton, nextButton;
    private JLabel pageInfoLabel;

    public PDFViewerFrame(PDDocument document, String filePath) {
        this.document = document;
        this.renderer = new PDFRenderer(document);
        initUI(filePath);
        renderPage(currentPage);
    }

    private void initUI(String filePath) {
        setTitle("PDF Viewer - " + new File(filePath).getName());
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        pageInfoLabel = new JLabel("", SwingConstants.CENTER);

        prevButton.addActionListener(e -> showPreviousPage());
        nextButton.addActionListener(e -> showNextPage());

        JPanel buttons = new JPanel();
        buttons.add(prevButton);
        buttons.add(nextButton);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(buttons, BorderLayout.WEST);
        bottom.add(pageInfoLabel, BorderLayout.CENTER);

        add(bottom, BorderLayout.SOUTH);
    }

    private void renderPage(int pageIndex) {
        try {
            int pageCount = document.getNumberOfPages();
            if (pageIndex < 0 || pageIndex >= pageCount) return;

            float scale = 1.25f;
            BufferedImage image = renderer.renderImage(pageIndex, scale);
            imageLabel.setIcon(new ImageIcon(image));

            currentPage = pageIndex;
            pageInfoLabel.setText("Page " + (currentPage + 1) + " / " + pageCount);
            prevButton.setEnabled(currentPage > 0);
            nextButton.setEnabled(currentPage < pageCount - 1);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error rendering page:\n" + e.getMessage());
        }
    }

    private void showPreviousPage() {
        if (currentPage > 0) renderPage(currentPage - 1);
    }

    private void showNextPage() {
        if (currentPage < document.getNumberOfPages() - 1) renderPage(currentPage + 1);
    }
}
