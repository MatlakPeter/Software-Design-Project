package core;

import model.FileData;
import observer.SearchObserver;
import repository.FileRepository;

import java.util.ArrayList;
import java.util.List;

public class QueryProcessor {
    private FileRepository repository;
    private QueryParser queryParser;
    private List<SearchObserver> observers;

    public QueryProcessor(FileRepository repository) {
        this.repository = repository;
        this.queryParser = new QueryParser();
        this.observers = new ArrayList<SearchObserver>();
    }

    public void executeQuery(String query) {
        System.out.println("\nSearching for: '" + query + "'...");

        ParsedQuery parsedQuery = queryParser.parse(query);

        List<FileData> results = repository.searchFiles(parsedQuery);

        if (results.isEmpty()) {
            System.out.println("No results found.");
            for (SearchObserver observer : observers) {
                observer.onSearchPerformed(query, parsedQuery, results);
            }
            return;
        }

        System.out.println("Found " + results.size() + " result(s):\n");
        for (FileData file : results) {
            System.out.println("File: " + file.getFilename());
            System.out.println("Path: " + file.getFilepath());
            System.out.println("Preview:\n" + PreviewGenerator.generatePreview(file.getContent()));
            System.out.println("Path Score: " + file.getPathScore());
            System.out.println("Last Modified: " + file.getFormattedDate());
            System.out.println("-".repeat(40));
        }

        for (SearchObserver observer : observers) {
            observer.onSearchPerformed(query, parsedQuery, results);
        }
    }

    public void addObserver(SearchObserver observer) {
        observers.add(observer);
    }
}