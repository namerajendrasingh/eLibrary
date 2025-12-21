package model;

import java.sql.Timestamp;

public class Book {
 private int id, totalCopies, availableCopies;
 private String title, author, isbn, category, filePath;
 private Timestamp addedDate;
  public Book() {	  
  }
 public Book(String title, String author, String isbn, String category, int totalCopies) {
     this.title = title;
     this.author = author;
     this.isbn = isbn;
     this.category = category;
     this.totalCopies = totalCopies;
     this.availableCopies = totalCopies;
 }

 public int getId() {
	return id;
 }

 public void setId(int id) {
	this.id = id;
 }

 public int getTotalCopies() {
	return totalCopies;
 }

 public void setTotalCopies(int totalCopies) {
	this.totalCopies = totalCopies;
 }

 public int getAvailableCopies() {
	return availableCopies;
 }

 public void setAvailableCopies(int availableCopies) {
	this.availableCopies = availableCopies;
 }

 public String getTitle() {
	return title;
 }

 public void setTitle(String title) {
	this.title = title;
 }

 public String getAuthor() {
	return author;
 }

 public void setAuthor(String author) {
	this.author = author;
 }

 public String getIsbn() {
	return isbn;
 }

 public void setIsbn(String isbn) {
	this.isbn = isbn;
 }

 public String getCategory() {
	return category;
 }

 public void setCategory(String category) {
	this.category = category;
 }

 public String getFilePath() {
	return filePath;
 }

 public void setFilePath(String filePath) {
	this.filePath = filePath;
 }
 public Timestamp getAddedDate() {
	return addedDate;
 }
 public void setAddedDate(Timestamp addedDate) {
	this.addedDate = addedDate;
 }

 

}
