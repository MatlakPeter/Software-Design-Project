import java.util.List;

public class QueryProcessor {
    private FileRepository repository;

    public QueryProcessor(FileRepository repository) {
        this.repository = repository;
    }

    public void executeQuery(String query) {
        System.out.println("\nSearching for: '" + query + "'...");
        List<FileData> results = repository.searchFiles(query);

        if (results.isEmpty()) {
            System.out.println("No results found.");
            return;
        }

        System.out.println("Found " + results.size() + " result(s):\n");
        for (FileData file : results) {
            System.out.println("File: " + file.getFilename());
            System.out.println("Path: " + file.getFilepath());
            System.out.println("Preview:\n" + PreviewGenerator.generatePreview(file.getContent()));
            System.out.println("-".repeat(40));
        }
    }
}