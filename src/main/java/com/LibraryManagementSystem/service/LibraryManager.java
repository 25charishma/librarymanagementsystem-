
package com.LibraryManagementSystem.service;

import java.util.Collection;
import com.LibraryManagementSystem.model.Book;
import com.LibraryManagementSystem.model.Loan;
import com.LibraryManagementSystem.model.Member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LibraryManager {
    private final Map<Integer, Book> books = new HashMap<>();
    private final Map<Integer, Member> members = new HashMap<>();  
    private final List<Loan> loans = new ArrayList<>();
    private final Set<String> registeredEmails = new HashSet<>();

    public synchronized boolean addBook(Book book) {
        if (book == null || books.containsKey(book.getItemId())) {
            return false;
        }
        books.put(book.getItemId(), book);
        return true;
    }

    public synchronized boolean addMember(Member member) {
        if (member == null || members.containsKey(member.getUserId())) {
            return false;
        }

        String email = member.getEmail().toLowerCase(Locale.ROOT);
        if (!registeredEmails.add(email)) {
            return false;
        }

        members.put(member.getUserId(), member);
        return true;
    }

    public synchronized Book findBook(int itemId) {
        return books.get(itemId);
    }

    public synchronized Member findMember(int userId) {
        return members.get(userId);
    }

    public synchronized Loan findLoan(int loanId) {
        return loans.stream()
                .filter(loan -> loan.getLoanId() == loanId)
                .findFirst()
                .orElse(null);
    }

    public synchronized List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String search = query.toLowerCase(Locale.ROOT);
        return books.values().stream()
                .filter(book -> book.getTitle().toLowerCase(Locale.ROOT).contains(search)
                        || book.getAuthor().toLowerCase(Locale.ROOT).contains(search))
                .sorted(Comparator.comparing(Book::getTitle,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public synchronized List<Book> getAvailableBooks() {
        return books.values().stream()
                .filter(Book::isAvailable)
                .sorted(Comparator.comparingInt(Book::getItemId))
                .toList();
    }

    public synchronized List<Book> sortBooksByTitle() {
        return books.values().stream()
                .sorted(Comparator.comparing(Book::getTitle,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public synchronized List<Book> sortBooksByAuthor() {
        return books.values().stream()
                .sorted(Comparator.comparing(Book::getAuthor,
                        String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Book::getTitle,
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public synchronized Loan issueBook(int loanId, int userId, int itemId) {
        Member member = members.get(userId);
        Book book = books.get(itemId);

        if (member == null || !member.isActive()) {
            throw new IllegalArgumentException("Active member not found");
        }
        if (book == null || !book.isAvailable()) {
            throw new IllegalArgumentException("Book not found or unavailable");
        }
        if (findLoan(loanId) != null) {
            throw new IllegalArgumentException("Loan ID already exists");
        }

        LocalDate issueDate = LocalDate.now();
        Loan loan = new Loan(loanId, book, member, issueDate,
                issueDate.plusDays(14));

        book.setAvailable(false);
        loans.add(loan);
        return loan;
    }

    public synchronized boolean returnBook(int loanId) {
        Loan loan = findLoan(loanId);

        if (loan == null || loan.isReturned()) {
            return false;
        }

        loan.markReturned(LocalDate.now());
        loan.getBook().setAvailable(true);
        return true;
    }

    public synchronized List<Loan> sortLoansByDueDate() {
        return loans.stream()
                .sorted(Comparator.comparing(Loan::getDueDate))
                .toList();
    }

    public synchronized List<Loan> getLoans() {
        return List.copyOf(loans);
    }

    public synchronized Map<Integer, Book> getBooks() {
        return Collections.unmodifiableMap(new HashMap<>(books));
    }

    public synchronized Map<Integer, Member> getMembers() {
        return Collections.unmodifiableMap(new HashMap<>(members));
    }

    public <T> void printList(List<T> items) {
        items.forEach(System.out::println);
    }
    public synchronized void restoreState(
            Collection<Book> restoredBooks,
            Collection<Member> restoredMembers,
            Collection<Loan> restoredLoans) {

        Map<Integer, Book> newBooks = new HashMap<>();
        for (Book book : restoredBooks) {
            if (book == null
                    || newBooks.putIfAbsent(book.getItemId(), book) != null) {
                throw new IllegalArgumentException("Duplicate or invalid book");
            }
        }

        Map<Integer, Member> newMembers = new HashMap<>();
        Set<String> newEmails = new HashSet<>();

        for (Member member : restoredMembers) {
            if (member == null
                    || newMembers.putIfAbsent(member.getUserId(), member) != null
                    || !newEmails.add(member.getEmail().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Duplicate or invalid member");
            }
        }

        List<Loan> newLoans = new ArrayList<>();
        Set<Integer> loanIds = new HashSet<>();
        Set<Integer> activeBookIds = new HashSet<>();

        for (Loan oldLoan : restoredLoans) {
            if (oldLoan == null || !loanIds.add(oldLoan.getLoanId())) {
                throw new IllegalArgumentException("Duplicate or invalid loan");
            }

            Book book = newBooks.get(oldLoan.getBook().getItemId());
            Member member = newMembers.get(oldLoan.getMember().getUserId());

            if (book == null || member == null) {
                throw new IllegalArgumentException(
                        "Loan refers to a missing book or member");
            }

            Loan loan = new Loan(
                    oldLoan.getLoanId(),
                    book,
                    member,
                    oldLoan.getIssueDate(),
                    oldLoan.getDueDate());

            if (oldLoan.getReturnDate() != null) {
                loan.markReturned(oldLoan.getReturnDate());
            } else if (!activeBookIds.add(book.getItemId())) {
                throw new IllegalArgumentException(
                        "A book has multiple active loans");
            }

            newLoans.add(loan);
        }

        for (Book book : newBooks.values()) {
            book.setAvailable(!activeBookIds.contains(book.getItemId()));
        }

        books.clear();
        books.putAll(newBooks);

        members.clear();
        members.putAll(newMembers);

        registeredEmails.clear();
        registeredEmails.addAll(newEmails);

        loans.clear();
        loans.addAll(newLoans);
    }
}