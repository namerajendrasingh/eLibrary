package controller;

//controller/SignupController.java
import model.User;
import model.UserDAO;
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

 public void register() {
	    String username = view.getUsername();
	    String email    = view.getEmail();
	    String password = view.getPassword();
	    String confirm  = view.getConfirmPassword();
	    String role     = view.getRole();

	    if (username.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
	        view.showMessage("All fields marked * are required.");
	        return;
	    }
	    if (!password.equals(confirm)) {
	        view.showMessage("Passwords do not match.");
	        return;
	    }

	    User user = new User(username, password, email, role);
	    boolean ok = userDAO.register(user);
	    if (ok) {
	        view.showMessage("Registration successful. You can log in now.");
	        // Save user in DB first...
	        // then send welcome email in a background thread:
	        new Thread(() -> {
	            try {
	                emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
	            } catch (Exception ex) {
	                ex.printStackTrace();
	                // optional: log error or show status in UI
	            }
	        }).start();
	        view.dispose();
	    } else {
	        view.showMessage("Registration failed. Username or email may already exist.");
	    }
	}

}
