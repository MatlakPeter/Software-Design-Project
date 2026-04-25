package observer;

import core.ParsedQuery;
import model.FileData;
import java.util.List;

public interface SearchObserver {
    void onSearchPerformed(String rawQuery, ParsedQuery parsedQuery, List<FileData> results);
}