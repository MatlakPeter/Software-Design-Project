package observer;

import core.ParsedQuery;
import model.FileData;
import repository.FileRepository;

import java.util.List;

public class SearchHistoryManager implements SearchObserver{
    private FileRepository repository;

    public SearchHistoryManager(FileRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onSearchPerformed(String rawQuery, ParsedQuery parsedQuery, List<FileData> results) {
        // save queries that are not empty
        if (rawQuery != null && !rawQuery.trim().isEmpty()) {
            repository.recordSearchQuery(rawQuery);
        }
    }

    public List<String> getTopSuggestions(int limit){
        return repository.getTopSearchQueries(limit);
    }
}
