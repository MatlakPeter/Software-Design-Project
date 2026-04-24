package core;

public class QueryParser {

    public ParsedQuery parse(String rawQuery){
        ParsedQuery parsed = new ParsedQuery();
        String[] tokens = rawQuery.trim().split("\\s+"); // split on whitespace

        for (String token : tokens) {
            if (token.startsWith("content:")){
                parsed.addContentTerm(token.substring("content:".length()));
            } else if (token.startsWith("path:")){
                parsed.addPathTerm(token.substring("path:".length()));
            } else if (token.startsWith("sort:")){
                String substring = token.substring("sort:".length());
                if (substring.toLowerCase().startsWith("score")){
                    parsed.setSortStrategy(ParsedQuery.SortStrategy.SCORE);
                } else if (substring.toLowerCase().startsWith("name")){
                    parsed.setSortStrategy(ParsedQuery.SortStrategy.NAME);
                } else if (substring.toLowerCase().startsWith("date")){
                    parsed.setSortStrategy(ParsedQuery.SortStrategy.DATE_MODIFIED);
                } // otherwise it remains the default
            } else {
                parsed.addFreeTerm(token);
            }
        }
        return parsed;
    }
}
