package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBUtil;

public class DashboardDAO {


    public int countTotalBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countIssuedBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM issues WHERE status = 'ISSUED'";
        //...
        return 0;
    }

    public int countMembers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM members";
        //...
        return 0;
    }

    public int countOverdueBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM issues WHERE due_date < CURRENT_DATE AND status = 'ISSUED'";
        //...
        return 0;
    }

	public int countIssuedBooksByLibrary() {
		// TODO Auto-generated method stub
		return 0;
	}
}
