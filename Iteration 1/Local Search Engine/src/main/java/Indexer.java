import java.io.File;

public class Indexer {
    private static int added = 0;
    private static int updated = 0;
    private static int ignored = 0;

    private Crawler crawler;

    public Indexer(Crawler crawler) {
        this.crawler = crawler;
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

    public void startIndexing(String rootPath) {
        added = 0; updated = 0; ignored = 0; // Reset statistics
        System.out.println("Starting index process on: " + rootPath);

        File rootDir = new File(rootPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.out.println("Invalid directory path.");
            return;
        }

        crawler.scanDirectory(rootDir);
        generateReport();
    }

    private void generateReport() {
        System.out.println("\n--- Indexing Complete ---");
        System.out.println("New files added: " + added);
        System.out.println("Existing files updated: " + updated);
        System.out.println("Files unmodified (ignored): " + ignored);
        System.out.println("-------------------------\n");
    }
}