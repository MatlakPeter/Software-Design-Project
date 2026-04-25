package core;

import model.FileData;
import repository.FileRepository;
import ui.SearchEngineUI;

import java.io.File;
import java.util.List;

public class Indexer {
    private int added = 0;
    private int updated = 0;
    private int ignored = 0;
    private int deleted = 0; // track deletions

    private Crawler crawler;
    private FileRepository repository;

    public Indexer(Crawler crawler, FileRepository repository) {
        this.crawler = crawler;
        this.repository = repository;
    }

    public SearchEngineUI.IndexReport runIndexing(String rootPath) {
        added = 0; updated = 0; ignored = 0; deleted = 0; // Reset statistics
        System.out.println("Starting index process on: " + rootPath);

        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Invalid directory path.");
            return null;
        }

        List<FileData> discovered = crawler.scanDirectory(rootDir);

        for (FileData fileData : discovered) {
            int path_score = PathScorer.score(fileData);
            fileData.setPathScore(path_score);

            FileRepository.SaveStatus saveStatus= repository.saveOrUpdateFile(fileData);
            switch (saveStatus) {
                case ADDED   -> added++;
                case UPDATED -> updated++;
                case IGNORED -> ignored++;
            }
        }

        int nr_deleted = repository.deleteStaleFiles(crawler.getScannedPaths(), rootDir.getAbsolutePath());
        deleted += nr_deleted;

        generateReport();

        SearchEngineUI.IndexReport report = new SearchEngineUI.IndexReport();
        report.added   = added;
        report.updated = updated;
        report.deleted = deleted;
        report.ignored = ignored;
        return report;
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