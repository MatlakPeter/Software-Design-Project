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
        Scanner scanner = new Scanner(System.in);
        FileRepository repository = new FileRepository();
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
            indexer.startIndexing(rootDir);
        } else {
            System.out.println("Skipping indexing. Using existing database for searches.");
        }

        // Search Loop
        System.out.println("\n--- Search Mode ---");
        while (true) {
            System.out.println("=== NEW SEARCH ===");
            // fetch and display the top 3 suggested queries from the database
            List<String> suggestions = historyManager.getTopSuggestions(3);
            if (!suggestions.isEmpty()) {
                System.out.println("Popular searches: " +String.join(", ", suggestions));
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