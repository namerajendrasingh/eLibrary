package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import util.DBUtil;
public class UserDAO {  
 public boolean login(String username, String password) {
     String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
     try (Connection conn = DBUtil.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         
         pstmt.setString(1, username);
         pstmt.setString(2, hashPassword(password));
         
         ResultSet rs = pstmt.executeQuery();
         return rs.next();
     } catch (SQLException e) {
         e.printStackTrace();
         return false;
     }
 }
 
 public boolean register(User user) {
     String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
     try (Connection conn = DBUtil.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         
         pstmt.setString(1, user.getUsername());
         pstmt.setString(2, hashPassword(user.getPassword()));
         pstmt.setString(3, user.getEmail());
         pstmt.setString(4, user.getRole());
         return pstmt.executeUpdate() > 0;
     } catch (SQLException e) {
         e.printStackTrace();
         return false;
     }
 }
 
//In model/UserDAO.java

public List<User> getAllUsers() {
  List<User> list = new ArrayList<>();
  String sql = "SELECT * FROM users ORDER BY id";
  try (Connection conn = DBUtil.getConnection();
       PreparedStatement ps = conn.prepareStatement(sql);
       ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
          User u = new User();
          u.setId(rs.getInt("id"));
          u.setUsername(rs.getString("username"));
          u.setEmail(rs.getString("email"));
          u.setRole(rs.getString("role"));
          // password usually not shown
          list.add(u);
      }
  } catch (SQLException e) {
      e.printStackTrace();
  }
  return list;
}

public boolean updateUser(User user) {
  String sql = "UPDATE users SET email = ?, role = ? WHERE id = ?";
  try (Connection conn = DBUtil.getConnection();
       PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, user.getEmail());
      ps.setString(2, user.getRole());
      ps.setInt(3, user.getId());
      return ps.executeUpdate() == 1;
  } catch (SQLException e) {
      e.printStackTrace();
      return false;
  }
}

public boolean deleteUser(int userId) {
  String sql = "DELETE FROM users WHERE id = ?";
  try (Connection conn = DBUtil.getConnection();
       PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, userId);
      return ps.executeUpdate() == 1;
  } catch (SQLException e) {
      e.printStackTrace();
      return false;
  }
}

//In UserDAO.java
public int countAllUsers() {
 String sql = "SELECT COUNT(*) FROM users";
 try (Connection conn = DBUtil.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery()) {
     return rs.next() ? rs.getInt(1) : 0;
 } catch (SQLException e) {
     e.printStackTrace();
     return 0;
 }
}

//In model/UserDAO.java

public User findById(int id) {
 String sql = "SELECT * FROM users WHERE id = ?";
 try (Connection conn = DBUtil.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)) {

     ps.setInt(1, id);
     try (ResultSet rs = ps.executeQuery()) {
         if (rs.next()) {
             User user = new User();
             user.setId(rs.getInt("id"));
             user.setUsername(rs.getString("username"));
             // You usually do NOT return the password hash to UI, but you can if needed
             user.setEmail(rs.getString("email"));
             user.setRole(rs.getString("role"));
             return user;
         }
     }
 } catch (SQLException e) {
     e.printStackTrace();
 }
 return null; // not found
}

public User findByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, username);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                return u;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

public boolean updatePassword(int userId, String rawPassword) {
    String sql = "UPDATE users SET password = ? WHERE id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, hashPassword(rawPassword)); // your SHA-256 method
        ps.setInt(2, userId);
        return ps.executeUpdate() == 1;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


private String hashPassword(String password) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }
        return hex.toString();
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("SHA-256 algorithm not available", e);
    }
}

}
