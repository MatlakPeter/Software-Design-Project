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
            } else {
                parsed.addFreeTerm(token);
            }
        }
        return parsed;
    }
}
