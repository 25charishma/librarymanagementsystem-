package com.LibraryManagementSystem.concurrency;

import com.LibraryManagementSystem.service.LibraryManager;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoanService implements AutoCloseable {
    private final ExecutorService executor;

    public LoanService(int numberOfThreads) {
        if (numberOfThreads <= 0) {
            throw new IllegalArgumentException("Thread count must be positive");
        }
        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    public List<Boolean> runConcurrentIssueDemo(
            LibraryManager library, int memberId, int bookId, int firstLoanId)
            throws InterruptedException {

        CountDownLatch ready = new CountDownLatch(3);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch runnableDone = new CountDownLatch(1);

        AtomicBoolean runnableResult = new AtomicBoolean(false);
        AtomicBoolean threadResult = new AtomicBoolean(false);

        Runnable runnableTask = () -> {
            ready.countDown();
            try {
                if (awaitStart(start)) {
                    runnableResult.set(tryIssue(
                            library, firstLoanId, memberId, bookId));
                }
            } finally {
                runnableDone.countDown();
            }
        };

        Callable<Boolean> callableTask = () -> {
            ready.countDown();
            if (awaitStart(start)) {
                return tryIssue(library, firstLoanId + 1, memberId, bookId);
            }
            return false;
        };

        Future<Boolean> callableFuture = executor.submit(callableTask);
        executor.execute(runnableTask);

        Thread thread = new Thread(() -> {
            ready.countDown();
            if (awaitStart(start)) {
                threadResult.set(tryIssue(
                        library, firstLoanId + 2, memberId, bookId));
            }
        }, "direct-loan-thread");

        thread.start();

        ready.await();
        start.countDown();

        thread.join();
        runnableDone.await();

        try {
            return List.of(
                    runnableResult.get(),
                    callableFuture.get(),
                    threadResult.get());
        } catch (ExecutionException e) {
            throw new IllegalStateException("A loan task failed", e.getCause());
        }
    }

    private static boolean tryIssue(
            LibraryManager library, int loanId, int memberId, int bookId) {
        try {
            library.issueBook(loanId, memberId, bookId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean awaitStart(CountDownLatch start) {
        try {
            start.await();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}