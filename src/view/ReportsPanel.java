package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ReportsPanel extends JPanel {

    private ReportsChartsPanel charts;
    private ReportsStatsPanel stats;  // ✅ Store reference

    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Library Analytics", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x333333));
        add(title, BorderLayout.NORTH);

        // ✅ Store references for refresh
        charts = new ReportsChartsPanel();
        stats = new ReportsStatsPanel(
            (totalBooks, available, totalUsers, activeIssues) -> 
                charts.refreshData(totalBooks, available, totalUsers, activeIssues)
        );

        add(charts, BorderLayout.CENTER);
        add(stats, BorderLayout.SOUTH);
    }

    // ✅ FIXED: Proper refresh method
    public void refreshData() {
        if (stats != null) {
            stats.refreshStats();  // ✅ Calls public refreshStats() → QuadConsumer → Charts
        }
    }
}
