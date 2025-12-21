package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import util.DBUtil;

public class BookIssueDAO {
 public BookIssueDAO() {
    
 }
 public List<BookIssue> findByUser(int userId) {
     List<BookIssue> list = new ArrayList<>();
     String sql = "SELECT * FROM book_issues WHERE user_id = ? ORDER BY issue_date DESC";
     try (Connection conn = DBUtil.getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setInt(1, userId);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
             BookIssue bi = new BookIssue();
             bi.setId(rs.getInt("id"));
             bi.setUserId(rs.getInt("user_id"));
             bi.setBookId(rs.getInt("book_id"));
             bi.setIssueDate(rs.getTimestamp("issue_date"));
             bi.setDueDate(rs.getTimestamp("due_date"));
             bi.setReturnDate(rs.getTimestamp("return_date"));
             bi.setStatus(rs.getString("status"));
             list.add(bi);
         }
     } catch (SQLException e) {
         e.printStackTrace();
     }
     return list;
 }
 
//In BookIssueDAO.java
public int countActiveIssues() {
  String sql = "SELECT COUNT(*) FROM book_issues WHERE status = 'ISSUED'";
  try (Connection conn = DBUtil.getConnection();
       PreparedStatement ps = conn.prepareStatement(sql);
       ResultSet rs = ps.executeQuery()) {
      return rs.next() ? rs.getInt(1) : 0;
  } catch (SQLException e) {
      e.printStackTrace();
      return 0;
  }
}

//In model/BookIssueDAO.java

public boolean issueBook(BookIssue issue) {
 String sql = "INSERT INTO book_issues (user_id, book_id, issue_date, due_date, status) " +
              "VALUES (?, ?, ?, ?, ?)";
 try (Connection conn = DBUtil.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)) {

     ps.setInt(1, issue.getUserId());
     ps.setInt(2, issue.getBookId());
     ps.setTimestamp(3, issue.getIssueDate());
     ps.setTimestamp(4, issue.getDueDate());
     ps.setString(5, issue.getStatus());
     return ps.executeUpdate() == 1;
 } catch (SQLException e) {
     e.printStackTrace();
     return false;
 }
}

public boolean returnBook(int issueId) {
 String sql = "UPDATE book_issues " +
              "SET status = 'RETURNED', return_date = CURRENT_TIMESTAMP " +
              "WHERE id = ? AND status = 'ISSUED'";
 try (Connection conn = DBUtil.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)) {

     ps.setInt(1, issueId);
     return ps.executeUpdate() == 1;
 } catch (SQLException e) {
     e.printStackTrace();
     return false;
 }
}

/** All active (ISSUED) issues, used for return table */
public List<BookIssue> findActiveIssues() {
 List<BookIssue> list = new ArrayList<>();
 String sql = "SELECT * FROM book_issues WHERE status = 'ISSUED' ORDER BY issue_date DESC";
 try (Connection conn = DBUtil.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery()) {
     while (rs.next()) {
         BookIssue bi = new BookIssue();
         bi.setId(rs.getInt("id"));
         bi.setUserId(rs.getInt("user_id"));
         bi.setBookId(rs.getInt("book_id"));
         bi.setIssueDate(rs.getTimestamp("issue_date"));
         bi.setDueDate(rs.getTimestamp("due_date"));
         bi.setReturnDate(rs.getTimestamp("return_date"));
         bi.setStatus(rs.getString("status"));
         list.add(bi);
     }
 } catch (SQLException e) {
     e.printStackTrace();
 }
 return list;
}

}
