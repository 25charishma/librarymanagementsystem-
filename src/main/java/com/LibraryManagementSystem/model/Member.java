
package com.LibraryManagementSystem.model;

public class Member extends User {
    private String phone;
    private boolean active;

    public Member(int userId, String name, String email, String phone) {
        super(userId, name, email);
        setPhone(phone);
        this.active = true;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone cannot be empty");
        }
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getRole() {
        return "Member";
    }
}