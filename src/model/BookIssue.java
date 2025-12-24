package model;

import java.sql.Timestamp;

public class BookIssue {

    private int id;
    private int userId;
    private int bookId;
    private String bookName;
    private Timestamp issueDate;
    private Timestamp dueDate;
    private Timestamp returnDate;
    private String status; // ISSUED, RETURNED, OVERDUE

    public BookIssue() {
    }

    public BookIssue(int userId, int bookId, Timestamp issueDate, Timestamp dueDate, String status) {
        this.userId = userId;
        this.bookId = bookId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }    
    
    

    public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }    

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }    

    public Timestamp getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Timestamp issueDate) {
        this.issueDate = issueDate;
    }    

    public Timestamp getDueDate() {
        return dueDate;
    }

    public void setDueDate(Timestamp dueDate) {
        this.dueDate = dueDate;
    }    

    public Timestamp getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Timestamp returnDate) {
        this.returnDate = returnDate;
    }    

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Convenience methods

    public boolean isReturned() {
        return "RETURNED".equalsIgnoreCase(status);
    }

    public boolean isOverdue() {
        return "OVERDUE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "BookIssue{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status='" + status + '\'' +
                '}';
    }
}
