package net.notifly.core.sql;

/**
 * util class for creating sql queries.
 * use this builder with the {@code SQLiteDatabase.query} method
 */
public class QueryBuilder
{
  boolean distinct;
  String table;
  String[] columns;
  String selection;
  String[] selectionArgs;
  String groupBy;
  String having;
  String orderBy;

  public QueryBuilder(String... columns)
  {
    this.columns = columns;
  }

  public QueryBuilder(boolean distinct, String... columns)
  {
    this.distinct = distinct;
    this.columns = columns;
  }

  public static QueryBuilder select(String... columns)
  {
    return new QueryBuilder(columns);
  }

  public static QueryBuilder selectDistinct(String... columns)
  {
    return new QueryBuilder(true, columns);
  }

  public QueryBuilder from(String table)
  {
    this.table = table;
    return this;
  }

  public QueryBuilder where(String selection, String... selectionArgs)
  {
    this.selection = selection;
    this.selectionArgs = selectionArgs;
    return this;
  }

  public QueryBuilder groupBy(String groupBy)
  {
    this.groupBy = groupBy;
    return this;
  }

  public QueryBuilder having(String having)
  {
    this.having = having;
    return this;
  }

  public QueryBuilder orderBy(String orderBy)
  {
    this.orderBy = orderBy;
    return this;
  }
}
