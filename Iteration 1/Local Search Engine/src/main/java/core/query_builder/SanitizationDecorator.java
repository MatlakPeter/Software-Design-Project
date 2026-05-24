package core.query_builder;

public class SanitizationDecorator extends QueryBuilderDecorator {

    // Characters that have special meaning in FTS should be stripped
    private static final String SPECIAL_CHARS_REGEX = "[\"'\\\\(){}<>|&!^~@#$%=+]";

    public SanitizationDecorator(QueryBuilder wrapped) {
        super(wrapped);
    }

    @Override
    protected String transform(String query) {
        return query.replaceAll(SPECIAL_CHARS_REGEX, " ").trim().replaceAll("\\s+", " ");
    }
}