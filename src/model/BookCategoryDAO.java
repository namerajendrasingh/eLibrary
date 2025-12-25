package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import util.DBUtil;

public class BookCategoryDAO {
    public List<BookCategory> getAllCategories() {
        List<BookCategory> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM book_category  where status = true ORDER BY category_name ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(new BookCategory(
                    rs.getInt("category_id"), 
                    rs.getString("category_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
}
