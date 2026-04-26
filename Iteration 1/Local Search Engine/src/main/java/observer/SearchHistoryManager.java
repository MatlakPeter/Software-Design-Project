package observer;

import core.ParsedQuery;
import model.FileData;
import repository.FileRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchHistoryManager implements SearchObserver{
    private FileRepository repository;
    private Map<String, Map<String, Integer>> prefixIndex;

    public SearchHistoryManager(FileRepository repository) {
        this.repository = repository;
        this.prefixIndex = new HashMap<>();

        // Load query_predictor into prefixIndex
        for (String[] row : repository.loadAllPredictions()){
            String prefix = row[0];
            String completion = row[1];
            int hits = Integer.parseInt(row[2]);
            prefixIndex.computeIfAbsent(prefix, k -> new HashMap<>()).put(completion, hits);
        }
    }

    @Override
    public void onSearchPerformed(String rawQuery, ParsedQuery parsedQuery, List<FileData> results) {
        // save queries that are not empty
        if (rawQuery == null && rawQuery.trim().isEmpty()) return;

        repository.recordSearchQuery(rawQuery);

        // Update prefixIndex and query_predictor table
        for (int len = 2; len <= rawQuery.length(); len++) {
            String prefix = rawQuery.substring(0, len);
            prefixIndex.computeIfAbsent(prefix, k -> new HashMap<>()).merge(rawQuery, 1, Integer::sum);

            repository.upsertPrediction(prefix, rawQuery);
        }
    }

    public List<String> getPredictions(String prefix, int limit){
        Map<String, Integer> completions = prefixIndex.getOrDefault(prefix, Map.of());
        return completions.entrySet().stream()
                          .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                          .limit(limit)
                          .map(Map.Entry::getKey)
                          .toList();
    }

    public List<String> getTopSuggestions(int limit){
        return repository.getTopSearchQueries(limit);
    }
}
