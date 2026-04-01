import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FileRepository repository = new FileRepository();
        QueryProcessor queryProcessor = new QueryProcessor(repository);

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