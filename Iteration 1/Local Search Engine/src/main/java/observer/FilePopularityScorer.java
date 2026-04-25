package observer;

import core.ParsedQuery;
import model.FileData;
import repository.FileRepository;

import java.util.List;

public class FilePopularityScorer implements SearchObserver{
    private FileRepository fileRepository;

    public FilePopularityScorer(FileRepository repository) {
        this.fileRepository = repository;
    }

    @Override
    public void onSearchPerformed(String rawQuery, ParsedQuery parsedQuery, List<FileData> results) {
        fileRepository.updateFileHistoryBoost(results);
    }
}
