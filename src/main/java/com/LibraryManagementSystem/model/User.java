
package com.LibraryManagementSystem.model;

import java.util.Objects;

public abstract class User {
    private final int userId;
    private String name;
    private String email;

    protected User(int userId, String name, String email) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        this.userId = userId;
        setName(name);
        setEmail(email);
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        this.email = email;
    }

    public abstract String getRole();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User other = (User) obj;
        return userId == other.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), userId);
    }

    @Override
    public String toString() {
        return getRole() + "{userId=" + userId
                + ", name='" + name + '\''
                + ", email='" + email + "'}";
    }
}