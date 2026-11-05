
package com.LibraryManagementSystem.persistence;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.LibraryManagementSystem.model.Book;
import com.LibraryManagementSystem.model.Loan;
import com.LibraryManagementSystem.model.Member;
import com.LibraryManagementSystem.service.LibraryManager;

public class JsonManager {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonManager() {
        // Prevent object creation
    }

    public static void saveState(LibraryManager library, String fileName)
            throws IOException {

        if (library == null || fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Invalid library or file name.");
        }

        StateFile state = new StateFile();

        for (Book book : library.getBooks().values()) {
            BookData data = new BookData();

            data.itemId = book.getItemId();
            data.title = book.getTitle();
            data.author = book.getAuthor();
            data.isbn = book.getIsbn();
            data.available = book.isAvailable();

            state.books.add(data);
        }

        for (Member member : library.getMembers().values()) {
            MemberData data = new MemberData();

            data.userId = member.getUserId();
            data.name = member.getName();
            data.email = member.getEmail();
            data.phone = member.getPhone();
            data.active = member.isActive();

            state.members.add(data);
        }

        for (Loan loan : library.getLoans()) {
            LoanData data = new LoanData();

            data.loanId = loan.getLoanId();
            data.bookId = loan.getBook().getItemId();
            data.memberId = loan.getMember().getUserId();
            data.issueDate = loan.getIssueDate();
            data.dueDate = loan.getDueDate();
            data.returnDate = loan.getReturnDate();

            state.loans.add(data);
        }

        File file = new File(fileName);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        MAPPER.writeValue(file, state);

        System.out.println("Library data saved successfully!");
    }

    public static void loadState(String fileName, LibraryManager library)
            throws IOException {

        if (library == null || fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Invalid library or file name.");
        }

        File file = new File(fileName);

        if (!file.exists()) {
            throw new IOException("JSON file not found: " + fileName);
        }

        StateFile state = MAPPER.readValue(file, StateFile.class);

        if (state == null) {
            throw new IOException("Invalid JSON data.");
        }

        if (state.books == null) state.books = new ArrayList<>();
        if (state.members == null) state.members = new ArrayList<>();
        if (state.loans == null) state.loans = new ArrayList<>();

        List<Book> books = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        List<Loan> loans = new ArrayList<>();

        Set<Integer> bookIds = new HashSet<>();
        Set<Integer> memberIds = new HashSet<>();
        Set<Integer> loanIds = new HashSet<>();

        for (BookData data : state.books) {
            if (data == null || !bookIds.add(data.itemId)) {
                throw new IOException("Invalid or duplicate book ID.");
            }

            Book book = new Book(
                    data.itemId,
                    data.title,
                    data.author,
                    data.isbn
            );

            book.setAvailable(data.available);
            books.add(book);
        }

        for (MemberData data : state.members) {
            if (data == null || !memberIds.add(data.userId)) {
                throw new IOException("Invalid or duplicate member ID.");
            }

            Member member = new Member(
                    data.userId,
                    data.name,
                    data.email,
                    data.phone
            );

            member.setActive(data.active);
            members.add(member);
        }

        for (LoanData data : state.loans) {
            if (data == null || !loanIds.add(data.loanId)) {
                throw new IOException("Invalid or duplicate loan ID.");
            }

            Book book = books.stream()
                    .filter(b -> b.getItemId() == data.bookId)
                    .findFirst()
                    .orElseThrow(() ->
                            new IOException("Book not found for loan: " + data.loanId));

            Member member = members.stream()
                    .filter(m -> m.getUserId() == data.memberId)
                    .findFirst()
                    .orElseThrow(() ->
                            new IOException("Member not found for loan: " + data.loanId));

            Loan loan;

            try {
                loan = new Loan(
                        data.loanId,
                        book,
                        member,
                        data.issueDate,
                        data.dueDate
                );

                if (data.returnDate != null) {
                    loan.markReturned(data.returnDate);
                }

            } catch (IllegalArgumentException | NullPointerException e) {
                throw new IOException("Invalid loan data: " + data.loanId, e);
            }

            loans.add(loan);
        }

        try {
            library.restoreState(books, members, loans);
        } catch (IllegalArgumentException e) {
            throw new IOException("Could not restore library data: "
                    + e.getMessage(), e);
        }

        System.out.println("Library data loaded successfully!");
    }

    // Main JSON structure
    public static class StateFile {
        public List<BookData> books = new ArrayList<>();
        public List<MemberData> members = new ArrayList<>();
        public List<LoanData> loans = new ArrayList<>();
    }

    // Book information
    public static class BookData {
        public int itemId;
        public String title;
        public String author;
        public String isbn;
        public boolean available;
    }

    // Member information
    public static class MemberData {
        public int userId;
        public String name;
        public String email;
        public String phone;
        public boolean active;
    }

    // Loan information
    public static class LoanData {
        public int loanId;
        public int bookId;
        public int memberId;
        public LocalDate issueDate;
        public LocalDate dueDate;
        public LocalDate returnDate;
    }
}
