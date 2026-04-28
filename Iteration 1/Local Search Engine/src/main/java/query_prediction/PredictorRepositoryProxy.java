package query_prediction;

import repository.FileRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictorRepositoryProxy implements PredictorRepository {
    private FileRepository fileRepository;
    private Map<String, Map<String, Integer>> cache;

    public PredictorRepositoryProxy(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.cache = new HashMap<>();

        // Load query_predictor into cache
        for (String[] row : fileRepository.loadAllPredictions()){
            String prefix = row[0];
            String completion = row[1];
            int hits = Integer.parseInt(row[2]);
            cache.computeIfAbsent(prefix, k -> new HashMap<>()).put(completion, hits);
        }
    }

    @Override
    public List<String> getPredictions(String prefix, int limit){
        Map<String, Integer> completions = cache.getOrDefault(prefix, Map.of());
        return completions.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public void upsertPrediction(String prefix, String completion){
        cache.computeIfAbsent(prefix, k -> new HashMap<>()).merge(completion, 1, Integer::sum);

        fileRepository.upsertPrediction(prefix, completion);
    }
}
