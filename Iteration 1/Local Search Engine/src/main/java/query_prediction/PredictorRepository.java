package query_prediction;

import java.util.List;

public interface PredictorRepository {
    void upsertPrediction(String prefix, String completion);
    public List<String> getPredictions(String prefix, int limit);
}