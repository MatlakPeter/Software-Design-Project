package core.query_builder;

public class BaseQueryBuilder implements QueryBuilder {

    @Override
    public String build(String rawQuery) {
        return rawQuery;
    }
}