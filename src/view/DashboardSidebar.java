package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import model.User;
import util.CommonMethods;

public class DashboardSidebar extends JPanel {

    private final User user;
    private final Runnable onLogout;
    private final Runnable onProfile;
    private final Runnable onSettings;
    private final Consumer<String> onTabSelect;
    private final Runnable onGlobalRefresh;

    public DashboardSidebar(User user,
                            Runnable onLogout,
                            Runnable onProfile,
                            Runnable onSettings,
                            Consumer<String> onTabSelect,
                            Runnable onGlobalRefresh) {

        this.user = user;
        this.onLogout = onLogout;
        this.onProfile = onProfile;
        this.onSettings = onSettings;
        this.onTabSelect = onTabSelect;
        this.onGlobalRefresh = onGlobalRefresh;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 0));
        setMinimumSize(new Dimension(250, 0));
        setMaximumSize(new Dimension(250, Integer.MAX_VALUE));
        setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        setBackground(new Color(248, 249, 250));

        initHeader();
        initCenterNav();
        initBottomActions();
    }

    private void initHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // Logo row - ✅ DOUBLE-CLICK REFRESH
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoRow.setOpaque(false);
        
        JLabel logoLabel = new JLabel(loadLogo("/icons/elibrary_logo.png", 36));
        logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoLabel.setToolTipText("Double-click to refresh dashboard 🔄");
        
        // ✅ DOUBLE-CLICK LOGO = REFRESH ALL
        java.awt.event.MouseAdapter refreshAdapter = new java.awt.event.MouseAdapter() {
            private long lastClick = 0;
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                long now = System.currentTimeMillis();
                if (now - lastClick < 400) { // Double-click within 400ms
                    if (onGlobalRefresh != null) {
                        onGlobalRefresh.run();
                    }
                }
                lastClick = now;
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                logoLabel.setBorder(BorderFactory.createLineBorder(new Color(0x3B82F6), 2));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                logoLabel.setBorder(null);
            }
        };
        logoLabel.addMouseListener(refreshAdapter);
        
        JLabel appLabel = new JLabel("<html><span style='color:#E53935;font-weight:bold;'>e</span>-<span style='color:#1976D2;font-weight:bold;'>Library</span></html>");
        appLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        logoRow.add(logoLabel);
        logoRow.add(Box.createHorizontalStrut(8));
        logoRow.add(appLabel);
        header.add(logoRow);

        // User info row
        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userRow.setOpaque(false);

        String roleUser = CommonMethods.convertTextToTitleCase(user.getRole()) + " • " +
                          CommonMethods.convertTextToTitleCase(user.getUsername());
        JLabel userLabel = new JLabel(roleUser);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(new Color(0x424242));

        JLabel dotLabel = new JLabel(createGreenDotIcon(12));
        JLabel onlineLabel = new JLabel("Online");
        onlineLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        onlineLabel.setForeground(new Color(0x4CAF50));

        userRow.add(userLabel);
        userRow.add(Box.createHorizontalStrut(8));
        userRow.add(dotLabel);
        userRow.add(onlineLabel);
        header.add(userRow);

        add(header, BorderLayout.NORTH);
    }

    private void initCenterNav() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        navPanel.setOpaque(false);

        // ✅ PERFECT TAB MATCHING (Button text → Tab title)
        navPanel.add(createPrimaryNavButton("Books", "/icons/books.png", e -> onTabSelect.accept("Books")));
        navPanel.add(createPrimaryNavButton("My Issues", "/icons/issues.png", e -> onTabSelect.accept("My Issues")));

        if ("ADMIN".equals(user.getRole()) || "STAFF".equals(user.getRole())) {
            navPanel.add(createPrimaryNavButton("Issue/Return", "/icons/issue.png", 
                e -> onTabSelect.accept("Issue/Return")));
        }
        
        if ("ADMIN".equals(user.getRole())) {
            JSeparator adminSep = new JSeparator(SwingConstants.HORIZONTAL);
            adminSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            adminSep.setForeground(new Color(0xE5E7EB));
            navPanel.add(adminSep);
            
            navPanel.add(createPrimaryNavButton("Users", "/icons/users.png", e -> onTabSelect.accept("Users")));
            navPanel.add(createPrimaryNavButton("Reports", "/icons/reports.png", e -> onTabSelect.accept("Reports")));
        }

        add(navPanel, BorderLayout.CENTER);
    }

    private void initBottomActions() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        bottomPanel.setOpaque(false);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottomPanel.add(sep);

        JButton profileBtn  = createSecondaryNavButton("Profile", "/icons/user.png", e -> onProfile.run());
        JButton settingsBtn = createSecondaryNavButton("Settings", "/icons/settings.png", e -> onSettings.run());
        JButton logoutBtn   = createDangerNavButton("Logout", "/icons/logout.png", e -> onLogout.run());

        bottomPanel.add(profileBtn);
        bottomPanel.add(settingsBtn);
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createPrimaryNavButton(String text, String iconPath, ActionListener listener) {
        JButton btn = new JButton(text, loadIcon(iconPath, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.addActionListener(listener);
        

        // ✅ COLORED HIGHLIGHT HOVER (12 lines)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setOpaque(true);
                btn.setBackground(new Color(59, 130, 246, 20));      // Blue highlight (20% opacity)
                btn.setForeground(new Color(99, 102, 241));          // Vibrant purple text
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setOpaque(false);
                btn.setBackground(null);
                btn.setForeground(null);
            }
        });
        
        return btn;
    }

    private JButton createSecondaryNavButton(String text, String iconPath, ActionListener listener) {
        JButton btn = createPrimaryNavButton(text, iconPath, listener);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return btn;
    }

    private JButton createDangerNavButton(String text, String iconPath, ActionListener listener) {
        JButton btn = createSecondaryNavButton(text, iconPath, listener);
        btn.setForeground(new Color(0xEF4444));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
     // ✅ DANGER COLORED HIGHLIGHT (4 lines)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setOpaque(true);
                btn.setBackground(new Color(239, 68, 68, 80));       // Red highlight (20% opacity)
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setOpaque(false);
                btn.setBackground(null);
            }
        });
        
        return btn;
    }

    private Icon loadLogo(String path, int size) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return null;
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    private Icon loadIcon(String path, int size) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return null;
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    private Icon createGreenDotIcon(int size) {
        return new Icon() {
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x4CAF50));
                g2.fillOval(x + 2, y + 2, size - 4, size - 4);
                g2.dispose();
            }
        };
    }
}
