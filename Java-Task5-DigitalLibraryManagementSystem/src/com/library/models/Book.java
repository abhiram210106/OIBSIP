package com.library.models;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private int totalQuantity;
    private int availableQuantity;
    private String coverUrl;
    private String description;
    private String publishedYear;
    private double rating;

    public Book() {}

    public Book(int id, String title, String author, String isbn, String category, 
                int totalQuantity, int availableQuantity, String coverUrl, 
                String description, String publishedYear, double rating) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.coverUrl = coverUrl;
        this.description = description;
        this.publishedYear = publishedYear;
        this.rating = rating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPublishedYear() { return publishedYear; }
    public void setPublishedYear(String publishedYear) { this.publishedYear = publishedYear; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
