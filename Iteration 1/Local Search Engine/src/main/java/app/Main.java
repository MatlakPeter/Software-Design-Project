package app;

import core.Crawler;
import core.Indexer;
import core.QueryProcessor;
import observer.FilePopularityScorer;
import observer.SearchHistoryManager;
import repository.FileRepository;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        FileRepository repository = new FileRepository();

        // ── NEW: pass --server (or -s) to start the REST API instead of CLI ──
        boolean serverMode = args.length > 0 &&
                (args[0].equals("--server") || args[0].equals("-s"));

        if (serverMode) {
            // Start the REST server; the UI is served from index.html separately
            WebServer server = new WebServer(repository);
            server.start(8080);

            // Keep the main thread alive
            try { Thread.currentThread().join(); }
            catch (InterruptedException ignored) {}
            return;
        }

        // ── Original CLI mode (unchanged) ─────────────────────────────────
        Scanner scanner = new Scanner(System.in);
        QueryProcessor queryProcessor = new QueryProcessor(repository);

        SearchHistoryManager historyManager = new SearchHistoryManager(repository);
        queryProcessor.addObserver(historyManager);
        FilePopularityScorer popularityScorer = new FilePopularityScorer(repository);
        queryProcessor.addObserver(popularityScorer);

        System.out.println("=== Local File Search Engine ===");

        System.out.print("Do you want to update the file index before searching? (y/n): ");
        String updateChoice = scanner.nextLine().trim().toLowerCase();

        if (updateChoice.equals("y") || updateChoice.equals("yes")) {
            System.out.print("Enter root directory to index: ");
            String rootDir = scanner.nextLine();

            System.out.print("Enter file extension to ignore (e.g., .log) or press Enter to skip: ");
            String ignoreExt = scanner.nextLine();
            if (ignoreExt.isEmpty()) ignoreExt = ".NONE";

            Crawler crawler = new Crawler(ignoreExt, repository);
            Indexer indexer = new Indexer(crawler, repository);

            System.out.println("\nIndexing started...");
            indexer.startIndexing(rootDir); // report is printed inside startIndexing
        } else {
            System.out.println("Skipping indexing. Using existing database for searches.");
        }

        System.out.println("\n--- Search Mode ---");
        while (true) {
            System.out.println("=== NEW SEARCH ===");
            List<String> suggestions = historyManager.getTopSuggestions(3);
            if (!suggestions.isEmpty()) {
                System.out.println("Popular searches: " + String.join(", ", suggestions));
            }

            System.out.print("Enter search query (or type 'exit' to quit): ");
            String query = scanner.nextLine();

            if ("exit".equalsIgnoreCase(query)) {
                System.out.println("Goodbye!");
                break;
            }

            if (!query.trim().isEmpty()) {
                queryProcessor.executeQuery(query);
            }
        }
        scanner.close();
    }
}