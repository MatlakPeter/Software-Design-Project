package core;

import model.FileData;
import repository.FileRepository;

import java.io.File;
import java.util.List;

public class Indexer {

    // ── NEW: return this instead of printing to stdout ─────────────────────
    public static class IndexReport {
        public int added, updated, ignored, deleted;
    }

    private Crawler        crawler;
    private FileRepository repository;

    public Indexer(Crawler crawler, FileRepository repository) {
        this.crawler    = crawler;
        this.repository = repository;
    }

    /**
     * Indexes the given root path.
     *
     * CHANGED: now returns an IndexReport instead of printing.
     * The CLI (Main.java) can still print; the REST layer (WebServer.java)
     * can return it as JSON.
     */
    public IndexReport startIndexing(String rootPath) {
        IndexReport report = new IndexReport();

        System.out.println("Starting index process on: " + rootPath);

        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Invalid directory path.");
            return report;
        }

        List<FileData> discovered = crawler.scanDirectory(rootDir);

        for (FileData fileData : discovered) {
            if (fileData == null) continue;                // null from unreadable files
            int pathScore = PathScorer.score(fileData);
            fileData.setPathScore(pathScore);

            FileRepository.SaveStatus status = repository.saveOrUpdateFile(fileData);
            if (status == null) continue;
            switch (status) {
                case ADDED   -> report.added++;
                case UPDATED -> report.updated++;
                case IGNORED -> report.ignored++;
            }
        }

        report.deleted = repository.deleteStaleFiles(
                crawler.getScannedPaths(), rootDir.getAbsolutePath());

        printReport(report);
        return report;
    }

    // Kept for CLI convenience
    private void printReport(IndexReport r) {
        System.out.println("\n--- Indexing Complete ---");
        System.out.println("New files added:        " + r.added);
        System.out.println("Existing files updated:  " + r.updated);
        System.out.println("Files deleted from DB:   " + r.deleted);
        System.out.println("Files unmodified:        " + r.ignored);
        System.out.println("-------------------------\n");
    }
}