package com.LibraryManagementSystem.app;

import java.util.List;

import com.LibraryManagementSystem.concurrency.LoanService;
import com.LibraryManagementSystem.model.Book;
import com.LibraryManagementSystem.model.Loan;
import com.LibraryManagementSystem.model.Member;
import com.LibraryManagementSystem.persistence.JsonManager;
import com.LibraryManagementSystem.service.LibraryManager;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private final LibraryManager library = new LibraryManager();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        boolean running = true;

        while (running) {
            showMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    library.printList(library.getAvailableBooks());
                    break;
                case 3:
                    addMember();
                    break;
                case 4:
                    library.getMembers().values().forEach(System.out::println);
                    break;
                case 5:
                    searchBooks();
                    break;
                case 6:
                    issueBook();
                    break;
                case 7:
                    returnBook();
                    break;
                case 8:
                    library.printList(library.getLoans());
                    break;
                case 9:
                    library.printList(library.sortBooksByTitle());
                    break;
                case 10:
                    library.printList(library.sortBooksByAuthor());
                    break;
                case 11:
                    saveData();
                    break;
                case 12:
                    loadData();
                    break;
                case 13:
                    concurrentIssueDemo();
                    break;
                case 0:
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        scanner.close();
    }

    private void showMenu() {
        System.out.println("\n=== LIBRARY MANAGEMENT SYSTEM ===");
        System.out.println("1. Add book");
        System.out.println("2. List available books");
        System.out.println("3. Add member");
        System.out.println("4. List members");
        System.out.println("5. Search books by title or author");
        System.out.println("6. Issue book");
        System.out.println("7. Return book");
        System.out.println("8. List loans");
        System.out.println("9. Sort books by title");
        System.out.println("10. Sort books by author");
        System.out.println("11. Save data to JSON");
        System.out.println("12. Load data from JSON");
        System.out.println("13. Concurrent issue demo");
        System.out.println("0. Exit");
    }

    private void addBook() {
        int id = readInt("Book ID: ");
        String title = readLine("Title: ");
        String author = readLine("Author: ");
        String isbn = readLine("ISBN: ");

        try {
            Book book = new Book(id, title, author, isbn);
            if (library.addBook(book)) {
                System.out.println("Book added.");
            } else {
                System.out.println("That book ID already exists.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add book: " + e.getMessage());
        }
    }

    private void addMember() {
        int id = readInt("Member ID: ");
        String name = readLine("Name: ");
        String email = readLine("Email: ");
        String phone = readLine("Phone: ");

        try {
            Member member = new Member(id, name, email, phone);
            if (library.addMember(member)) {
                System.out.println("Member added.");
            } else {
                System.out.println("That member ID or email is already registered.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add member: " + e.getMessage());
        }
    }

    private void searchBooks() {
        String query = readLine("Enter a title or author to search: ");
        library.printList(library.searchBooks(query));
    }

    private void issueBook() {
        int loanId = readInt("Loan ID: ");
        int memberId = readInt("Member ID: ");
        int bookId = readInt("Book ID: ");

        try {
            Loan loan = library.issueBook(loanId, memberId, bookId);
            System.out.println("Book issued:");
            System.out.println(loan);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not issue book: " + e.getMessage());
        }
    }

    private void returnBook() {
        int loanId = readInt("Loan ID to return: ");

        if (library.returnBook(loanId)) {
            System.out.println("Book returned.");
        } else {
            System.out.println("Loan not found or the book was already returned.");
        }
    }

    private void concurrentIssueDemo() {
        int memberId = readInt("Active member ID: ");
        int bookId = readInt("Available book ID: ");
        int firstLoanId = readInt("First unused loan ID: ");

        try (LoanService service = new LoanService(2)) {
            List<Boolean> results = service.runConcurrentIssueDemo(
                    library, memberId, bookId, firstLoanId);

            long successfulIssues = results.stream()
                    .filter(Boolean::booleanValue)
                    .count();

            System.out.println("Results (Runnable, Callable, Thread): " + results);
            System.out.println("Successful issues: " + successfulIssues);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Concurrent demo was interrupted.");
        }
    }
    private void saveData() {
        try {
            JsonManager.saveState(library, "data/library-state.json");
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            JsonManager.loadState("data/library-state.json", library);
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    private int readInt(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt));
        } catch (NumberFormatException e) {
            System.out.println("Please enter a whole number.");
            return -1;
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}