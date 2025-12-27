package controller;

import model.User;
import model.UserDAO;
import util.CommonMethods;
import util.EmailConfig;
import util.EmailService;
import view.SignupFrame;

public class SignupController {
    private final SignupFrame view;
    private final UserDAO userDAO;
    
    private static final EmailService emailService = 
        new EmailService(EmailConfig.getUsername(), EmailConfig.getPassword());

    public SignupController(SignupFrame view) {
        this.view = view;
        this.userDAO = new UserDAO();
    }

    /**
     * ✅ UPDATED REGISTER with firstname, lastname, full validation
     */
    public void register() {
        // ✅ GET ALL NEW FIELDS
        String firstname = view.getFirstname();
        String lastname = view.getLastname();
        String username = view.getUsername();
        String password = view.getPassword();
        String confirmPassword = view.getConfirmPassword();
        String email = view.getEmail();
        String role = view.getRole();
        
        // ✅ COMPREHENSIVE VALIDATION using CommonMethods
        if (!validateRegistration(firstname, lastname, username, password, confirmPassword, email)) {
            return;  // Validation shows errors automatically
        }
        
        // ✅ FINAL USERNAME AVAILABILITY CHECK
        if (!userDAO.isUsernameAvailable(username)) {
            CommonMethods.showError(view, "❌ Username '<b>" + username + "</b>' is already taken!");
            return;
        }
        
        // ✅ CREATE USER WITH ALL FIELDS
        User user = new User(username, password, firstname, lastname, email, role);
        boolean ok = userDAO.register(user);
        
        if (ok) {
            CommonMethods.showMessage(view, 
                "✅ Registration successful!<br>" +
                "Welcome email sent to <b>" + email + "</b>"
            );
            
            // ✅ SEND WELCOME EMAIL with ALL details (background thread)
            new Thread(() -> {
                try {
                    emailService.sendWelcomeEmail(email, username, password, firstname, lastname);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Email sending failed for user: " + username);
                }
            }).start();
            
            view.dispose();
        } else {
            CommonMethods.showError(view, 
                "❌ Registration failed.<br>" +
                "Please try again or contact support."
            );
        }
    }
    
    /**
     * ✅ COMPREHENSIVE VALIDATION
     */
    private boolean validateRegistration(String firstname, String lastname, String username, 
                                       String password, String confirmPassword, String email) {
        
        if (firstname.isBlank() || firstname.length() > 50) {
            CommonMethods.showError(view, "👤 First name required (max 50 characters)");
            return false;
        }
        if (lastname.isBlank() || lastname.length() > 50) {
            CommonMethods.showError(view, "👤 Last name required (max 50 characters)");
            return false;
        }
        if (username.length() < 8 || username.length() > 40 || !isValidUsername(username)) {
            CommonMethods.showError(view, 
                "🆔 Username: <b>8-40 characters</b><br>" +
                "Allowed: letters, numbers, _, - only"
            );
            return false;
        }
        if (password.length() < 6) {
            CommonMethods.showError(view, "🔒 Password minimum <b>6 characters</b>");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            CommonMethods.showError(view, "🔒 Passwords do not match!");
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            CommonMethods.showError(view, "📧 Enter a <b>valid email address</b>");
            return false;
        }
        return true;
    }
    
    /**
     * ✅ USERNAME VALIDATION (matches frontend)
     */
    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_-]+$");
    }
}
