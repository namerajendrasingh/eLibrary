package util;

import javax.swing.JOptionPane;
import java.awt.Component;

public class CommonMethods {
    
    public static void showMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,  // ✅ Works with JFrame/JDialog/JComponent directly
            "<html><div style='padding: 15px; color: #2e7d32;'>" + message + "</div></html>",
            "✅ Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            "<html><div style='padding: 15px; color: #d32f2f;'>" + message + "</div></html>",
            "❌ Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            "<html><div style='padding: 15px; color: #f57c00;'>" + message + "</div></html>",
            "⚠️ Warning",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(
            parent,
            "<html><div style='padding: 15px;'>" + message + "</div></html>",
            "Confirm Action",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }
    
    public static String showInput(Component parent, String message, String defaultValue) {
        Object result = JOptionPane.showInputDialog(
            parent,
            "<html><div style='padding: 15px;'>" + message + "</div></html>",
            "Input Required",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            defaultValue
        );
        return result != null ? result.toString().trim() : null;
    }


    /**
	 * @param text
	 * @return Title Case String eg: hello as Hello HELLO as Hello
	 */
	public static String convertTextToTitleCase(String text) {
	    if (text == null || text.isEmpty()) {
	        return text;
	    }

	    StringBuilder converted = new StringBuilder();

	    boolean convertNext = true;
	    for (char ch : text.toCharArray()) {
	        if (Character.isSpaceChar(ch)) {
	            convertNext = true;
	        } else if (convertNext) {
	            ch = Character.toTitleCase(ch);
	            convertNext = false;
	        } else {
	            ch = Character.toLowerCase(ch);
	        }
	        converted.append(ch);
	    }

	    return converted.toString();
	}
}
