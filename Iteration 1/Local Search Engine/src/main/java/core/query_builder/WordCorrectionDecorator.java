package core.query_builder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WordCorrectionDecorator extends QueryBuilderDecorator {

    private static final Map<String, String> CORRECTIONS = new HashMap<>();
    static {
        CORRECTIONS.put("img",     "image");
        CORRECTIONS.put("imge",    "image");
        CORRECTIONS.put("iamge",   "image");
        CORRECTIONS.put("phto",    "photo");
        CORRECTIONS.put("pohto",   "photo");
        CORRECTIONS.put("vidoe",   "video");
        CORRECTIONS.put("doc",     "document");
        CORRECTIONS.put("flie",    "file");
        CORRECTIONS.put("teh",     "the");
        CORRECTIONS.put("adn",     "and");
        CORRECTIONS.put("wiht",    "with");
        CORRECTIONS.put("petre",   "peter");
    }

    public WordCorrectionDecorator(QueryBuilder wrapped) {
        super(wrapped);
    }

    @Override
    protected String transform(String query) {
        return Arrays.stream(query.split("\\s+"))
                .map(token -> CORRECTIONS.getOrDefault(token.toLowerCase(), token))
                .collect(Collectors.joining(" "));
    }
}