package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class ReportsChartsPanel extends JPanel {

    private ChartPanel barChartPanel;
    private ChartPanel pieChartPanel;
    private ChartPanel timeSeriesPanel;

    public ReportsChartsPanel() {
        setupProfessionalTheme();
        initUI();
    }

    private void setupProfessionalTheme() {
        StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
        theme.setExtraLargeFont(new Font("Segoe UI", Font.BOLD, 18));
        theme.setLargeFont(new Font("Segoe UI", Font.BOLD, 14));
        theme.setRegularFont(new Font("Segoe UI", Font.PLAIN, 12));
        theme.setSmallFont(new Font("Segoe UI", Font.PLAIN, 11));
        ChartFactory.setChartTheme(theme);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        barChartPanel = new ChartPanel(null);
        pieChartPanel = new ChartPanel(null);
        timeSeriesPanel = new ChartPanel(null);

        tabs.addTab("📊 Overview", barChartPanel);
        tabs.addTab("📈 Availability", pieChartPanel);
        tabs.addTab("📉 Trends", timeSeriesPanel);

        add(tabs, BorderLayout.CENTER);
    }

    public void refreshData(int totalBooks, int available, int totalUsers, int activeIssues) {
        updateProfessionalBarChart(totalBooks, available, totalUsers, activeIssues);
        updateProfessionalPieChart(totalBooks, available);
        updateTimeSeriesChart();
    }

    private void updateProfessionalBarChart(int totalBooks, int available, int totalUsers, int activeIssues) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series = "Library Metrics";
        
        dataset.addValue(totalBooks, series, "Total Books");
        dataset.addValue(available, series, "Available");
        dataset.addValue(totalUsers, series, "Users");
        dataset.addValue(activeIssues, series, "Active Issues");

        JFreeChart chart = ChartFactory.createBarChart(
            null, "Metrics", "Count", dataset,
            PlotOrientation.VERTICAL, false, true, false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        
        // Professional gradient colors
        renderer.setSeriesPaint(0, new Color(0x2196F3));
        renderer.setSeriesPaint(1, new Color(0x4CAF50));
        renderer.setSeriesPaint(2, new Color(0xFF9800));
        renderer.setSeriesPaint(3, new Color(0xF44336));

        // Modern bar styling
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setDrawBarOutline(true);
        renderer.setSeriesOutlinePaint(0, Color.WHITE);
        renderer.setSeriesOutlineStroke(0, new java.awt.BasicStroke(1.5f));
        renderer.setShadowVisible(true);

        // ✅ CORRECT: Simple value labels on bars using text annotations
        plot.addAnnotation(new org.jfree.chart.annotations.CategoryTextAnnotation(
            totalBooks + "",                    // Text
            "Total Books",                      // Category
            totalBooks * 0.9                    // Value position (90% of bar height)
        ));
        
        plot.addAnnotation(new org.jfree.chart.annotations.CategoryTextAnnotation(
            available + "",
            "Available", 
            available * 0.9
        ));
        
        plot.addAnnotation(new org.jfree.chart.annotations.CategoryTextAnnotation(
            totalUsers + "",
            "Users",
            totalUsers * 0.9
        ));
        
        plot.addAnnotation(new org.jfree.chart.annotations.CategoryTextAnnotation(
            activeIssues + "",
            "Active Issues",
            activeIssues * 0.9
        ));

        // X-axis improvements
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setMaximumCategoryLabelLines(2);

        // Clean styling
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        chart.setBorderVisible(false);

        barChartPanel.setChart(chart);
        barChartPanel.setMouseWheelEnabled(true);
        barChartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
    }

    private void updateProfessionalPieChart(int totalBooks, int available) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        int unavailable = Math.max(totalBooks - available, 0);
        
        // Only add non-zero slices
        if (available > 0) {
            dataset.setValue("✅ Available", available);
        }
        if (unavailable > 0) {
            dataset.setValue("📚 Checked Out", unavailable);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            null,                                    // No title
            dataset,                                 // Dataset
            true,                                    // Legend ON (professional)
            true,                                    // Tooltips
            false                                    // No URLs
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        
        // ✅ PROFESSIONAL COLOR PALETTE
        plot.setSectionPaint("✅ Available", new Color(46, 125, 50));    // Material Green 700
        plot.setSectionPaint("📚 Checked Out", new Color(239, 108, 0)); // Material Orange 600
        
        // ✅ ENHANCED LABELS (outside slices)
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
            "{2}\n({1})"  // Percentage + Value (cleaner)
        ));
        
        plot.setLabelFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        plot.setLabelPaint(Color.BLACK);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 220));  // Semi-transparent white bg
        plot.setLabelOutlinePaint(new Color(0, 0, 0, 100));
        plot.setLabelShadowPaint(new Color(0, 0, 0, 80));
        plot.setSimpleLabels(false);
        
        // ✅ POSITION LABELS OUTSIDE slices
        plot.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
        plot.setMaximumLabelWidth(0.2);
        
        // ✅ PERFECT GEOMETRY
        plot.setCircular(true);
        plot.setMinimumArcAngleToDraw(3);
        plot.setInteriorGap(0.08);  // Perfect spacing
        
        // ✅ SUBTLE ELEVATION (not overdone)
        plot.setShadowXOffset(2);
        plot.setShadowYOffset(2);
        plot.setShadowPaint(new Color(0, 0, 0, 40));  // Very subtle
        
        // ✅ CLEAN BACKGROUND & BORDERS
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(true);
        plot.setOutlinePaint(new Color(200, 200, 200));
        plot.setOutlineStroke(new java.awt.BasicStroke(2.0f));
        
        // ✅ PROFESSIONAL LEGEND
        if (chart.getLegend() != null) {
            chart.getLegend().setFrame(BlockBorder.NONE);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 12));
            chart.getLegend().setBackgroundPaint(new Color(255, 255, 255, 240));
            chart.getLegend().setPadding(10, 10, 10, 10);
        }
        
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(Color.WHITE);

        pieChartPanel.setChart(chart);
        pieChartPanel.setMouseWheelEnabled(true);
        pieChartPanel.setPreferredSize(new java.awt.Dimension(650, 450));
    }



    private void updateTimeSeriesChart() {
        JFreeChart dummyChart = ChartFactory.createXYLineChart(
            "📈 Issues Over Time (Coming Soon)", "Date", "Active Issues", null
        );
        timeSeriesPanel.setChart(dummyChart);
        timeSeriesPanel.setMouseWheelEnabled(true);
    }
}
