package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatIntelliJLaf;

import view.LoginFrame;

public class Main {

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        // 1) Set FlatLaf before any Swing UI is created
        FlatIntelliJLaf.setup();   // or FlatLightLaf.setup(), FlatDarkLaf.setup(), etc.
        // 2) Start UI on EDT
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setLocationRelativeTo(null); // center on screen
            login.setVisible(true);
        });
    }
}
