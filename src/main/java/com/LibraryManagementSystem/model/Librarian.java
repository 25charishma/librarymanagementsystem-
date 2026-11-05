
package com.LibraryManagementSystem.model;

public class Librarian extends User {
    private String employeeNumber;

    public Librarian(int userId, String name, String email, String employeeNumber) {
        super(userId, name, email);
        setEmployeeNumber(employeeNumber);
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.isBlank()) {
            throw new IllegalArgumentException("Employee number cannot be empty");
        }
        this.employeeNumber = employeeNumber;
    }

    @Override
    public String getRole() {
        return "Librarian";
    }
}