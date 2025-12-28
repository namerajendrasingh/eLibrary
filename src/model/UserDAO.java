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
	    // ✅ SQL LOWER() for case-insensitive username matching
	    String sql = """
	        SELECT * from users 
	        WHERE LOWER(username) = LOWER(?) 
	        AND password = ?
	        """;
	    
	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, username.trim());           // ✅ Case-insensitive
	        pstmt.setString(2, hashPassword(password));    // ✅ Password hash
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            return rs.next();  // ✅ Returns true if user found
	        }
	        
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
    String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
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

//✅ PAGINATION METHODS
public List<User> getUsersWithPagination(int offset, int limit) {
 String sql = """
     SELECT id, username, email, role 
     FROM users 
     ORDER BY id ASC 
     LIMIT ? OFFSET ?
 """;
 
 try (PreparedStatement pstmt = DBUtil.getConnection().prepareStatement(sql)) {
     pstmt.setInt(1, limit);
     pstmt.setInt(2, offset);
     try (ResultSet rs = pstmt.executeQuery()) {
         List<User> users = new ArrayList<>();
         while (rs.next()) {
             User user = new User();
             user.setId(rs.getInt("id"));
             user.setUsername(rs.getString("username"));
             user.setEmail(rs.getString("email"));
             user.setRole(rs.getString("role"));
             users.add(user);
         }
         return users;
     }
 } catch (SQLException e) {
     e.printStackTrace();
     return new ArrayList<>();
 }
}

public int getTotalUserCount() {
 String sql = "SELECT COUNT(*) FROM users";
 try (PreparedStatement pstmt = DBUtil.getConnection().prepareStatement(sql);
      ResultSet rs = pstmt.executeQuery()) {
     if (rs.next()) {
         return rs.getInt(1);
     }
 } catch (SQLException e) {
     e.printStackTrace();
 }
 return 0;
}

public boolean register(User user) {
    // ✅ VALIDATE ROLE FIRST (Prevents DB constraint violation)
	System.out.println("received user role ln220"+user.getRole());
    if (!UserRole.isValid(user.getRole())) {
        System.err.println("❌ Invalid role: " + user.getRole() + 
                          " | Allowed: " + java.util.Arrays.toString(UserRole.values()));
        return false;
    }
    
    // ✅ VALIDATE USERNAME LENGTH (DB constraint)
    if (user.getUsername().length() < 8 || user.getUsername().length() > 40) {
        System.err.println("❌ Username length invalid: " + user.getUsername().length());
        return false;
    }
    
    String sql = """
        INSERT INTO users (username, password, firstname, lastname, email, role) 
        VALUES (?, ?, ?, ?, ?, ?) 
        ON CONFLICT (username) DO NOTHING
        RETURNING id
    """;
    
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, hashPassword(user.getPassword()));
        pstmt.setString(3, user.getFirstname());
        pstmt.setString(4, user.getLastname());
        pstmt.setString(5, user.getEmail());
        pstmt.setString(6, user.getRole());  // ✅ Now validated!
        
        try (ResultSet rs = pstmt.executeQuery()) {
            boolean success = rs.next();  // ✅ Check RETURNING clause
            if (success) {
                System.out.println("✅ User registered: " + user.getUsername() + " (ID: " + rs.getInt("id") + ")");
            }
            return success;
        }
        
    } catch (SQLException e) {
        // ✅ BETTER ERROR MESSAGES
        if (e.getMessage().contains("users_role_check")) {
            System.err.println("❌ Role violation: '" + user.getRole() + "' not in [ADMIN, STAFF, GUEST]");
        } else if (e.getMessage().contains("username_length")) {
            System.err.println("❌ Username length violation: '" + user.getUsername() + "'");
        } else if (e.getMessage().contains("unique_username")) {
            System.err.println("❌ Username already exists: " + user.getUsername());
        } else {
            e.printStackTrace();
        }
        return false;
    }
}


/**
 * ✅ CHECK USERNAME AVAILABILITY (Real-time)
 */
public boolean isUsernameAvailable(String username) {
    String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) == 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

}
