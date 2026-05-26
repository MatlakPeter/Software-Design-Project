package core;

import model.FileData;
import repository.FileRepository;
import ui.SearchEngineUI;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer {
    private static final int QUEUE_CAPACITY = 500;
    private static final int NUM_READERS = Runtime.getRuntime().availableProcessors();

    // Placed on the queue after all readers finish, so the writer thread knows there is nothing left to process
    private static final FileData POISON_PILL = new FileData("", "", "", 0);

    private final Crawler crawler;
    private final FileRepository repository;

    public Indexer(Crawler crawler, FileRepository repository) {
        this.crawler = crawler;
        this.repository = repository;
    }

    public SearchEngineUI.IndexReport runIndexing(String rootPath) {
        System.out.println("Starting index process on: " + rootPath);

        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Invalid directory path.");
            return null;
        }

        AtomicInteger added   = new AtomicInteger();
        AtomicInteger updated = new AtomicInteger();
        AtomicInteger ignored = new AtomicInteger();

        BlockingQueue<FileData> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        ExecutorService readers = Executors.newFixedThreadPool(NUM_READERS);

        // Writer thread (single consumer): owns all database writes
        Thread writer = startWriterThread(queue, added, updated, ignored);

        // Reader threads (multiple producers): Each task puts its result onto the queue; the writer picks them up as they arrive.
        System.out.println("Scanning with " + NUM_READERS + " reader threads...");
        crawler.scanDirectoryParallel(rootDir, queue, readers);


        readers.shutdown(); // wait for all reader tasks to finish, then wake the writer so it can exit
        try {
            readers.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            queue.put(POISON_PILL); // signals the writer that the queue is exhausted
            writer.join(); // wait for this thread to die
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Indexing interrupted.");
        }

        int deleted = repository.deleteStaleFiles(crawler.getScannedPaths(), rootDir.getAbsolutePath());

        generateReport(added.get(), updated.get(), deleted, ignored.get());

        SearchEngineUI.IndexReport report = new SearchEngineUI.IndexReport();
        report.added   = added.get();
        report.updated = updated.get();
        report.deleted = deleted;
        report.ignored = ignored.get();
        return report;
    }

    private Thread startWriterThread(BlockingQueue<FileData> queue, AtomicInteger added, AtomicInteger updated, AtomicInteger ignored) {
        Thread writer = new Thread(() -> {
            int saveCount = 0;
            System.out.println("Saving to the database...");
            try {
                while (true) {
                    FileData fileData = queue.take(); // blocks until a file is ready
                    if (fileData == POISON_PILL) break;

                    FileRepository.SaveStatus status = repository.saveOrUpdateFile(fileData);
                    switch (status) {
                        case ADDED   -> added.incrementAndGet();
                        case UPDATED -> updated.incrementAndGet();
                        case IGNORED -> ignored.incrementAndGet();
                    }

                    saveCount++;
                    if (saveCount % 1000 == 0) {
                        System.out.println("Saved " + saveCount + " files so far...");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Writer thread interrupted.");
            }
        });

        writer.start();
        return writer;
    }

    private void generateReport(int added, int updated, int deleted, int ignored) {
        System.out.println("\n--- Indexing Complete ---");
        System.out.println("New files added: " + added);
        System.out.println("Existing files updated: " + updated);
        System.out.println("Files deleted from DB: " + deleted);
        System.out.println("Files unmodified (ignored): " + ignored);
        System.out.println("-------------------------\n");
    }
}