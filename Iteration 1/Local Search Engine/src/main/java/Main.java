import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FileRepository repository = new FileRepository();
        QueryProcessor queryProcessor = new QueryProcessor(repository);

        System.out.println("=== Local File Search Engine ===");

        // Configuration
        System.out.print("Enter root directory to index (e.g., C:/Projects or /Users/me/Docs): ");
        String rootDir = scanner.nextLine();

        System.out.print("Enter file extension to ignore (e.g., .log) or press Enter to skip: ");
        String ignoreExt = scanner.nextLine();
        if (ignoreExt.isEmpty()) ignoreExt = ".NONE";

        // Initialization
        Crawler crawler = new Crawler(ignoreExt, repository);
        Indexer indexer = new Indexer(crawler, repository);

        // Build Index
        indexer.startIndexing(rootDir);

        // Search Loop
        while (true) {
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