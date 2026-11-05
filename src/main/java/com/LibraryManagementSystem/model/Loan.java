
package com.LibraryManagementSystem.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Loan {
    private final int loanId;
    private final Book book;
    private final Member member;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;

    public Loan(int loanId, Book book, Member member,
                LocalDate issueDate, LocalDate dueDate) {
        if (loanId <= 0) {
            throw new IllegalArgumentException("Loan ID must be positive");
        }

        this.loanId = loanId;
        this.book = Objects.requireNonNull(book, "book");
        this.member = Objects.requireNonNull(member, "member");
        this.issueDate = Objects.requireNonNull(issueDate, "issueDate");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");

        if (!dueDate.isAfter(issueDate)) {
            throw new IllegalArgumentException("Due date must be after issue date");
        }
    }

    public int getLoanId() {
        return loanId;
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public void markReturned(LocalDate date) {
        Objects.requireNonNull(date, "date");

        if (isReturned()) {
            throw new IllegalStateException("This loan is already returned");
        }
        if (date.isBefore(issueDate)) {
            throw new IllegalArgumentException("Return date cannot be before issue date");
        }

        this.returnDate = date;
    }

    // Fine rule for this project: ₹2 for each day after the due date.
    public double calculateFine(LocalDate asOfDate) {
        LocalDate endDate = isReturned()
                ? returnDate
                : Objects.requireNonNull(asOfDate, "asOfDate");

        long overdueDays = ChronoUnit.DAYS.between(dueDate, endDate);
        return Math.max(0, overdueDays) * 2.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Loan other = (Loan) obj;
        return loanId == other.loanId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId);
    }

    @Override
    public String toString() {
        return "Loan{loanId=" + loanId
                + ", book='" + book.getTitle() + '\''
                + ", member='" + member.getName() + '\''
                + ", issueDate=" + issueDate
                + ", dueDate=" + dueDate
                + ", returnDate=" + returnDate + "}";
    }
}