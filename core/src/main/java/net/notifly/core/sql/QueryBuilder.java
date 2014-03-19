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

  public QueryBuilder(String table, String... columns)
  {
    this.table = table;
    this.columns = columns;
  }

  public QueryBuilder(boolean distinct, String table, String[] columns)
  {
    this.distinct = distinct;
    this.table = table;
    this.columns = columns;
  }

  public static QueryBuilder select(String table, String... columns)
  {
    return new QueryBuilder(table, columns);
  }

  public static QueryBuilder selectDistinct(String table, String... columns)
  {
    return new QueryBuilder(true, table, columns);
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
