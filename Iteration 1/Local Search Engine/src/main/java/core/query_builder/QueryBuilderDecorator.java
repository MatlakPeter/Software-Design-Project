package core.query_builder;

public abstract class QueryBuilderDecorator implements QueryBuilder {
    private final QueryBuilder wrapped;

    public QueryBuilderDecorator(QueryBuilder wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String build(String rawQuery) {
        return transform(wrapped.build(rawQuery));
    }

    protected abstract String transform(String query);
}