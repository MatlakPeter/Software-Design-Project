package core;

import repository.FileRepository;

import java.io.File;

public class Indexer {
    private static int added = 0;
    private static int updated = 0;
    private static int ignored = 0;
    private static int deleted = 0; // track deletions

    private Crawler crawler;
    private FileRepository repository;

    public Indexer(Crawler crawler, FileRepository repository) {
        this.crawler = crawler;
        this.repository = repository;
    }

    public static void incrementAdded() {
        added++;
    }
    public static void incrementUpdated() {
        updated++;
    }
    public static void incrementIgnored() {
        ignored++;
    }
    public static void incrementDeleted() {
        deleted++;
    }

    public void startIndexing(String rootPath) {
        added = 0; updated = 0; ignored = 0; // Reset statistics
        System.out.println("Starting index process on: " + rootPath);

        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Invalid directory path.");
            return;
        }

        crawler.scanDirectory(rootDir);

        repository.deleteStaleFiles(crawler.getScannedPaths(), rootDir.getAbsolutePath());

        generateReport();
    }

    private void generateReport() {
        System.out.println("\n--- Indexing Complete ---");
        System.out.println("New files added: " + added);
        System.out.println("Existing files updated: " + updated);
        System.out.println("Files deleted from DB: " + deleted);
        System.out.println("Files unmodified (ignored): " + ignored);
        System.out.println("-------------------------\n");
    }
}