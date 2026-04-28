package observer;

import core.ParsedQuery;
import model.FileData;
import query_prediction.PredictorRepository;
import query_prediction.PredictorRepositoryProxy;
import repository.FileRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchHistoryManager implements SearchObserver{
    private FileRepository repository;
    private PredictorRepository predictorRepository;

    public SearchHistoryManager(FileRepository repository) {
        this.repository = repository;
        this.predictorRepository = new PredictorRepositoryProxy(repository);
    }

    @Override
    public void onSearchPerformed(String rawQuery, ParsedQuery parsedQuery, List<FileData> results) {
        // save queries that are not empty
        if (rawQuery == null || rawQuery.trim().isEmpty()) return;

        repository.recordSearchQuery(rawQuery);

        // Update prefixIndex and query_predictor table
        for (int len = 2; len <= rawQuery.length(); len++) {
            String prefix = rawQuery.substring(0, len);
            predictorRepository.upsertPrediction(prefix, rawQuery);
        }
    }

    public List<String> getPredictions(String prefix, int limit){
        return predictorRepository.getPredictions(prefix, limit);
    }

    public List<String> getTopSuggestions(int limit){
        return repository.getTopSearchQueries(limit);
    }
}
