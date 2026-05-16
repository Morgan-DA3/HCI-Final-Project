package com.smartlibrary.model;

public class Book {
    private final int id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private int publicationYear;
    private int quantity;
    private int availableCopies;
    private String shelfLocation;
    private String status;
    private String coverImagePath;

    public Book(int id, String title, String author, String isbn, String category, int publicationYear,
                int quantity, int availableCopies, String shelfLocation, String status, String coverImagePath) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.publicationYear = publicationYear;
        this.quantity = quantity;
        this.availableCopies = availableCopies;
        this.shelfLocation = shelfLocation;
        this.status = status;
        this.coverImagePath = coverImagePath;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }
}
