package controller;

import java.sql.SQLException;

import model.User;
import model.UserDAO;
import view.LoginFrame;
import view.SignupFrame;

//controller/LoginController.java
public class LoginController {
 private LoginFrame view;
 private UserDAO userDAO;
 
 public LoginController(LoginFrame view) {
     this.view = view;
     this.userDAO = new UserDAO();
 }
 
//controller/LoginController.java
public void login() throws SQLException {
  String username = view.getUsername();
  String password = view.getPassword();
  String role     = view.getRole();

  if (userDAO.login(username, password)) {
      // Get full user with id, role, email
      User user = userDAO.findByUsername(username); // implement this in UserDAO
      view.openDashboard(user);                     // ✅ pass User, not String
  } else {
      view.showMessage("Invalid credentials!");
  }
}

 public void showSignup() {
     new SignupFrame().setVisible(true);
 }
}
