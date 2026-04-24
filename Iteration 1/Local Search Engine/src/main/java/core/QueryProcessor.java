package core;

import model.FileData;
import repository.FileRepository;

import java.util.List;

public class QueryProcessor {
    private FileRepository repository;
    private QueryParser queryParser;

    public QueryProcessor(FileRepository repository) {
        this.repository = repository;
        this.queryParser = new QueryParser();
    }

    public void executeQuery(String query) {
        System.out.println("\nSearching for: '" + query + "'...");

        ParsedQuery parsedQuery = queryParser.parse(query);

        List<FileData> results = repository.searchFiles(parsedQuery);

        if (results.isEmpty()) {
            System.out.println("No results found.");
            return;
        }

        System.out.println("Found " + results.size() + " result(s):\n");
        for (FileData file : results) {
            System.out.println("File: " + file.getFilename());
            System.out.println("Path: " + file.getFilepath());
            System.out.println("Preview:\n" + PreviewGenerator.generatePreview(file.getContent()));
            System.out.println("Path Score: " + file.getPathScore());
            System.out.println("-".repeat(40));
        }
    }
}