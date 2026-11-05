
package com.LibraryManagementSystem.model;

public class Book extends LibraryItem {
    private String author;
    private String isbn;

    public Book(int itemId, String title, String author, String isbn) {
        super(itemId, title);
        setAuthor(author);
        setIsbn(isbn);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author cannot be empty");
        }
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be empty");
        }
        this.isbn = isbn;
    }

    @Override
    public String getItemType() {
        return "Book";
    }

    @Override
    public String toString() {
        return "Book{itemId=" + getItemId()
                + ", title='" + getTitle() + '\''
                + ", author='" + author + '\''
                + ", isbn='" + isbn + '\''
                + ", available=" + isAvailable() + "}";
    }
}