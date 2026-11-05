
package com.LibraryManagementSystem.model;

import java.util.Objects;

public abstract class LibraryItem {
    private final int itemId;
    private String title;
    private boolean available;

    protected LibraryItem(int itemId, String title) {
        if (itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        this.itemId = itemId;
        setTitle(title);
        this.available = true;
    }

    public int getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public abstract String getItemType();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        LibraryItem other = (LibraryItem) obj;
        return itemId == other.itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), itemId);
    }

    @Override
    public String toString() {
        return getItemType() + "{itemId=" + itemId
                + ", title='" + title + '\''
                + ", available=" + available + "}";
    }
}