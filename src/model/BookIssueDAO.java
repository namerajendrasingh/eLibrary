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


//✅ PAGINATION METHODS
public List<BookIssue> findByUserWithPagination(int userId, int offset, int limit) {
    String sql = """
        SELECT bi.id, bi.book_id, bi.issue_date, bi.due_date, bi.return_date, bi.status,
               b.title AS book_name                           -- ✅ JOIN book name
        FROM book_issues bi
        JOIN books b ON bi.book_id = b.id                    -- ✅ JOIN with books
        WHERE bi.user_id = ? 
        ORDER BY bi.issue_date DESC NULLS LAST
        LIMIT ? OFFSET ?
    """;
    
    try (PreparedStatement pstmt = DBUtil.getConnection().prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        pstmt.setInt(2, limit);
        pstmt.setInt(3, offset);
        try (ResultSet rs = pstmt.executeQuery()) {
            List<BookIssue> issues = new ArrayList<>();
            while (rs.next()) {
                BookIssue issue = new BookIssue();
                issue.setId(rs.getInt("id"));
                issue.setBookName(rs.getString("book_name"));      // ✅ NEW: Set book name
                issue.setBookId(rs.getInt("book_id"));
                issue.setIssueDate(rs.getTimestamp("issue_date"));
                issue.setDueDate(rs.getTimestamp("due_date"));
                issue.setReturnDate(rs.getTimestamp("return_date"));
                issue.setStatus(rs.getString("status"));
                issues.add(issue);
            }
            return issues;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}


public int getUserIssueCount(int userId) {
    String sql = "SELECT COUNT(*) FROM book_issues WHERE user_id = ?";  
    try (PreparedStatement pstmt = DBUtil.getConnection().prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

public List<BookIssue> findByUser(int userId) {
    String sql = """
        SELECT id, book_id, issue_date, due_date, return_date, status 
        FROM book_issues 
        WHERE user_id = ?
        ORDER BY issue_date DESC
        """;
    
    try (PreparedStatement pstmt = DBUtil.getConnection().prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        try (ResultSet rs = pstmt.executeQuery()) {
            List<BookIssue> issues = new ArrayList<>();
            while (rs.next()) {
                BookIssue issue = new BookIssue();
                issue.setId(rs.getInt("id"));
                issue.setBookId(rs.getInt("book_id"));
                issue.setUserId(rs.getInt("user_id"));
                issue.setIssueDate(rs.getTimestamp("issue_date"));
                issue.setDueDate(rs.getTimestamp("due_date"));
                issue.setReturnDate(rs.getTimestamp("return_date"));
                issue.setStatus(rs.getString("status"));
                issues.add(issue);
            }
            return issues;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}



}
