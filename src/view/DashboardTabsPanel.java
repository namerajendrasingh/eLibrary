package view;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import model.User;

public class DashboardTabsPanel extends JTabbedPane {

    // ✅ Store panel references for refresh
    private final Map<String, Object> panels = new HashMap<>();
    private BookPanel booksPanel;
    private MyIssuesPanel myIssuesPanel;
    private IssueReturnPanel issueReturnPanel;
    private UserManagementPanel usersPanel;
    private ReportsPanel reportsPanel;
    private BookUploadPanel bookUploadPanel;  // ✅ NEW FIELD

    

    public DashboardTabsPanel(User user) {
        setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // ✅ Books tab (always visible)
        booksPanel = new BookPanel(user.getRole());
        addTab("Books", booksPanel);
        panels.put("Books", booksPanel);

        // ✅ My Issues tab (always visible)
        myIssuesPanel = new MyIssuesPanel(user.getId());
        addTab("My Issues", myIssuesPanel);
        panels.put("My Issues", myIssuesPanel);

        // ✅ Issue/Return (Admin/Staff)
        if ("ADMIN".equals(user.getRole()) || "STAFF".equals(user.getRole())) {
            issueReturnPanel = new IssueReturnPanel();
            addTab("Issue/Return", issueReturnPanel);
            panels.put("Issue/Return", issueReturnPanel);
            
         // ✅ NEW: Book Upload (Admin/Staff)
            bookUploadPanel = new BookUploadPanel();
            addTab("Book Upload", bookUploadPanel);
            panels.put("Book Upload", bookUploadPanel);
        }

        // ✅ Admin-only tabs
        if ("ADMIN".equals(user.getRole())) {
            usersPanel = new UserManagementPanel();
            addTab("Users", usersPanel);
            panels.put("Users", usersPanel);

            reportsPanel = new ReportsPanel();
            addTab("Reports", reportsPanel);
            panels.put("Reports", reportsPanel);
        }
    }

    // ✅ FIXED: Proper refresh method with instanceof checks
    public void refreshAllData() {
        // Books panel
        if (booksPanel != null) {
            booksPanel.refreshData();  // loadBooks()
        }

        // My Issues panel  
        if (myIssuesPanel != null) {
            myIssuesPanel.refreshData();  // loadIssues()
        }

        // Issue/Return panel
        if (issueReturnPanel != null) {
            issueReturnPanel.refreshData();  // loadActiveIssues()
        }

        // Users panel
        if (usersPanel != null) {
            usersPanel.refreshData();  // loadUsers()
        }

        // Reports panel (triggers stats → charts cascade)
        if (reportsPanel != null) {
            reportsPanel.refreshData();  // Calls ReportsStatsPanel → ReportsChartsPanel
        }
        
     // ✅ NEW: Book Upload panel (no refresh needed)
        if (bookUploadPanel != null) {
            // BookUploadPanel doesn't need refresh (static utility)
        }
    }

    public void selectTabByTitle(String title) {
        int count = getTabCount();
        for (int i = 0; i < count; i++) {
            if (title.equals(getTitleAt(i))) {
                setSelectedIndex(i);
                return;
            }
        }
    }
}
