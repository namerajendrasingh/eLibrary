package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import util.DBUtil;

public class BookDAO {
    public BookDAO() {
    }
    private Connection getConnection() throws SQLException {
        return  DBUtil.getConnection();
    }
    // CREATE: add new book
    public boolean addBook(Book book) {
        String sql = """
            INSERT INTO books (title, author, isbn, category_id, total_copies, available_copies, file_path)
            VALUES (?, ?, ?, (SELECT category_id FROM book_category WHERE category_name = ? AND status = true), ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = DBUtil.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getCategory());  // ✅ Find category_id by name
            pstmt.setInt(5, book.getTotalCopies());
            pstmt.setInt(6, book.getAvailableCopies());
            pstmt.setString(7, book.getFilePath());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Get generated ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        book.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // UPDATE: edit book details (not IDs)
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, category = ?, " +
                     "total_copies = ?, available_copies = ?, file_path = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());
            ps.setString(7, book.getFilePath());
            ps.setInt(8, book.getId());

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // DELETE: remove book by id
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ: get single book
    public Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ: list all books
    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // SEARCH: by title/author/isbn/category (for search box)
    public List<Book> searchBooks(String keyword) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books " +
                     "WHERE LOWER(title)   LIKE ? " +
                     "   OR LOWER(author)  LIKE ? " +
                     "   OR LOWER(isbn)    LIKE ? " +
                     "   OR LOWER(category) LIKE ? " +
                     "ORDER BY title";
        String like = "%" + keyword.toLowerCase() + "%";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRowToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ISSUE: decrease available_copies if > 0
    public boolean decreaseAvailableCopies(int bookId) {
        String sql = "UPDATE books SET available_copies = available_copies - 1 " +
                     "WHERE id = ? AND available_copies > 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() == 1; // 1 row updated = success
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // RETURN: increase available_copies but not beyond total_copies
    public boolean increaseAvailableCopies(int bookId) {
        String sql = "UPDATE books " +
                     "SET available_copies = CASE " +
                     "    WHEN available_copies < total_copies THEN available_copies + 1 " +
                     "    ELSE available_copies " +
                     "END " +
                     "WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // For showing counts / dashboard
    public int countAllBooks() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countAvailableBooks() {
        String sql = "SELECT SUM(available_copies) FROM books";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
 // Add this method to BookDAO
    public boolean setTotalCopiesToZero(int bookId) {
        String sql = "UPDATE books SET total_copies = 0, available_copies = 0 WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
 // 1. Count active issues
    public int countActiveIssuesForBook(int bookId) {
        String sql = "SELECT COUNT(*) FROM book_issues WHERE book_id = ? AND status = 'ISSUED'";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 2. Get active issue IDs
    public List<Integer> getActiveIssueIdsForBook(int bookId) {
        List<Integer> issueIds = new ArrayList<>();
        String sql = "SELECT id FROM book_issues WHERE book_id = ? AND status = 'ISSUED'";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                issueIds.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return issueIds;
    }

    // 3. Force delete (CASCADE)
    public boolean forceDeleteBookWithIssues(int bookId) {
        try {
            getConnection().setAutoCommit(false);
            
            // Delete issues first
            String deleteIssues = "DELETE FROM book_issues WHERE book_id = ?";
            try (PreparedStatement pstmt = getConnection().prepareStatement(deleteIssues)) {
                pstmt.setInt(1, bookId);
                pstmt.executeUpdate();
            }
            
            // Delete book
            String deleteBook = "DELETE FROM books WHERE id = ?";
            try (PreparedStatement pstmt = getConnection().prepareStatement(deleteBook)) {
                pstmt.setInt(1, bookId);
                int rows = pstmt.executeUpdate();
                getConnection().commit();
                return rows > 0;
            }
        } catch (SQLException e) {
            try { getConnection().rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { getConnection().setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
 // ✅ NEW PAGINATION METHODS
    public List<Book> getBooksWithOffset(int offset, int limit) {
        String sql = """
            SELECT b.id, b.title, b.author, b.isbn, 
                   bc.category_name AS category, 
                   b.total_copies, b.available_copies, b.file_path
            FROM books b
            LEFT JOIN book_category bc ON b.category_id = bc.category_id  -- ✅ LEFT JOIN
            WHERE bc.status = true OR bc.status IS NULL  -- ✅ Active categories only
            ORDER BY b.id ASC 
            LIMIT ? OFFSET ?
            """;
        
        try (PreparedStatement pstmt = DBUtil.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setIsbn(rs.getString("isbn"));
                    book.setCategory(rs.getString("category"));  // ✅ From JOIN
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    book.setFilePath(rs.getString("file_path"));
                    books.add(book);
                }
                return books;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    
    public int getTotalBookCount() {
        String sql = "SELECT COUNT(*) FROM books";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    // Map ResultSet → Book object
    private Book mapRowToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setCategory(rs.getString("category"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        book.setFilePath(rs.getString("file_path"));
        book.setAddedDate(rs.getTimestamp("added_date"));
        return book;
    }
    /**
     * ✅ NEW: Get active categories for dropdown filter
     */
    public List<String> getActiveCategories() {
        String sql = """
            SELECT category_name 
            FROM book_category 
            WHERE status = true 
            ORDER BY category_name ASC
        """;
        
        try (PreparedStatement pstmt = DBUtil.getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            List<String> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
            return categories;
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Return common categories as fallback
            return List.of("English", "Novel", "Engineering", "Maths", "Science", "Computer");
        }
    }
    public List<Book> searchByTitle(String title) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, isbn, total_copies, available_copies " +
                    "FROM books WHERE LOWER(title) LIKE LOWER(?) LIMIT 10";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setIsbn(rs.getString("isbn"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching books: " + e.getMessage());
            e.printStackTrace();
        }
        return books;
    }
	
    public Book findById(int id) {
        String sql = "SELECT id, title, author, isbn, total_copies, available_copies " +
                    "FROM books WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setIsbn(rs.getString("isbn"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
            
                    return book;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding book by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    
    
}
