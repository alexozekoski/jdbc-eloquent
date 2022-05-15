/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.query;

import github.alexozekoski.database.Database;
import github.alexozekoski.database.Log;
import github.alexozekoski.database.model.ModelUtil;
import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alexo
 * @param <T>
 */
public class Query<T extends Query> {

    private List<Clause> clauses = new ArrayList();

    private boolean firstWhere = true;

    private Database database;

    private String table = null;

    public Query(Database database) {
        this.database = database;
    }

    public Query(Database database, String table) {
        this.database = database;
        this.table = table;
    }

    protected List<Clause> getClauses() {
        return clauses;
    }

    public T groupBy(Object... columns) {
        for (Object column : columns) {
            groupBy(column);
        }
        return (T) this;
    }

    public T select(String... columns) {
        for (String column : columns) {
            clauses.add(new Column(column, table));
        }
        return (T) this;
    }
    
    public T set(String column, Object value){
        clauses.add(new Set(column, value));
        return (T) this;
    }

    public T groupBy(Object column) {
        clauses.add(new GroupBy(column, table));
        return (T) this;
    }

    public T leftJoin(Class table, String localColumn, String foreignColumn) {
        return join("LEFT JOIN", ModelUtil.getTable(table), localColumn, foreignColumn);
    }

    public T rightJoin(Class table, String localColumn, String foreignColumn) {
        return join("RIGHT JOIN", ModelUtil.getTable(table), localColumn, foreignColumn);
    }

    public T join(Class table, String localColumn, String foreignColumn) {
        return join("INNER JOIN", ModelUtil.getTable(table), localColumn, foreignColumn);
    }

    public T leftJoin(String table, String localColumn, String foreignColumn) {
        return join("LEFT JOIN", table, localColumn, foreignColumn);
    }

    public T rightJoin(String table, String localColumn, String foreignColumn) {
        return join("RIGHT JOIN", table, localColumn, foreignColumn);
    }

    public T join(String table, String localColumn, String foreignColumn) {
        return join("INNER JOIN", table, localColumn, foreignColumn);
    }

    public T join(String join, String table, String localColumn, String foreignColumn) {
        return (T) joinRaw(join, table, localColumn + " = " + foreignColumn);
    }

    public T join(Class table, String query) {
        return joinRaw("INNER JOIN", ModelUtil.getTable(table), query);
    }

    public T leftJoin(Class table, String query) {
        return joinRaw("LEFT JOIN", ModelUtil.getTable(table), query);
    }

    public T rightJoin(Class table, String query) {
        return joinRaw("RIGHT JOIN", ModelUtil.getTable(table), query);
    }

    public T joinRaw(String join, String table, String query) {
        clauses.add(new Join(join, table, query));
        return (T) this;
    }

    public T orWhereRaw(String where, Object... param) {
        return where("OR", where, "RAW", param, true);
    }

    public T whereRaw(String where, Object... param) {
        return where("AND", where, "RAW", param, true);
    }

    public T orBetween(String column, Object a, Object b) {
        return where("OR", column, "BETWEEN", new Object[]{a, b});
    }

    public T between(String column, Object a, Object b) {
        return where("AND", column, "BETWEEN", new Object[]{a, b});
    }

    public T orderBy(Object... columns) {
        for (Object column : columns) {
            orderBy(column);
        }
        return (T) this;
    }

    public T orderBy(Object column) {
        clauses.add(new OrderBy(column, "ASC", table));
        return (T) this;
    }

    public T orderBy(Object column, String order) {
        clauses.add(new OrderBy(column, order == null ? "ASC" : order, table));
        return (T) this;
    }

    public T limit(long offset, long limit) {
        offset(offset);
        return limit(limit);
    }

    public T limit(long value) {
        List<Clause> list = find(Limit.class, clauses);
        if (list.size() > 0) {
            ((Limit) list.get(0)).setValue(value);
        } else {
            clauses.add(new Limit(value));
        }
        return (T) this;
    }

    public T offset(long value) {
        List<Clause> list = find(Offset.class, clauses);
        if (list.size() > 0) {
            ((Offset) list.get(0)).setValue(value);
        } else {
            clauses.add(new Offset(value));
        }
        return (T) this;
    }

    public T table(String table) {
        setTable(table);
        return (T) this;
    }

    public T orWhereNotIn(String column, List value) {
        return orWhere(column, "<>", value);
    }

    public T orWhereNotIn(String column, Object... values) {
        return orWhere(column, "<>", values);
    }

    public T whereNotIn(String column, List value) {
        return where(column, "<>", value);
    }

    public T whereNotIn(String column, Object... values) {
        return where(column, "<>", values);
    }

    public T orWhereIn(String column, List value) {
        return orWhere(column, value);
    }

    public T orWhereIn(String column, Object... values) {
        return orWhere(column, values);
    }

    public T whereIn(String column, List value) {
        return where(column, value);
    }

    public T whereIn(String column, Object... values) {
        return where("AND", column, "=", values);
    }

    public T orWhere(String column, Object... values) {
        return where("OR", column, "=", values);
    }

    public T where(String column, Object value) {
        return where(column, "=", value);
    }

    public T where(String column, String operator, Object value) {
        return where("AND", column, operator, value);
    }

    public T where(String prefix, String column, String operator, Object value, boolean raw) {
        if (value != null && value.getClass().isArray() && Array.getLength(value) == 0) {
            return (T) this;
        }
        if (value instanceof List && ((List) value).isEmpty()) {
            return (T) this;
        }
        if (firstWhere) {
            firstWhere = false;
            prefix = null;
        }
        clauses.add(new Where(prefix, column, operator, value, raw, table));
        return (T) this;
    }

    public T where(String prefix, String column, String operator, Object value) {
        return where(prefix, column, operator, value, false);
    }

    public Object[] buildParam(char type) {
        List<Object> param = new ArrayList<>();
        switch (type) {
            case 'U': {
                buildParam(type, Set.class, clauses, param);
                break;
            }
            case 'I': {
                buildParam(type, Set.class, clauses, param);
                break;
            }

        }

        buildParam(type, Where.class, clauses, param);
        //addParam(param, find(GroupBy.class));
        //addParam(param, find(OrderBy.class));
        buildParam(type, Limit.class, clauses, param);
        buildParam(type, Offset.class, clauses, param);
        return param.toArray();
    }

    protected void buildJoin(StringBuilder sql, char type) {
        buildQuery(type, sql, " ", Join.class, clauses, " ", " ");
    }

    protected void buildGroupBy(StringBuilder sql, char type) {
        buildQuery(type, sql, " GROUP BY", GroupBy.class, clauses, " ", ", ");
    }

    protected void buildWhere(StringBuilder sql, char type) {
        buildQuery(type, sql, " WHERE", Where.class, clauses, " ", " ");
    }

    protected void buildOffset(StringBuilder sql, char type) {
        buildQuery(type, sql, " OFFSET", Offset.class, clauses, " ", " ");
    }

    protected void buildLimit(StringBuilder sql, char type) {
        buildQuery(type, sql, " LIMIT", Limit.class, clauses, " ", " ");
    }

    protected void buildOrderBy(StringBuilder sql, char type) {
        buildQuery(type, sql, " ORDER BY", OrderBy.class, clauses, " ", ", ");
    }

    protected void buildTable(StringBuilder sql) {;
        sql.append(" FROM ");
        sql.append("`").append(table).append("`");
    }

    protected void buildSelect(StringBuilder sql) {
        int t = buildQuery('S', sql, "SELECT", Column.class, clauses, " ", ", ");
        if (t == 0) {
            sql.append("SELECT *");
        }
    }

    protected void buildUpdate(StringBuilder sql) {
        sql.append("UPDATE ");
       sql.append("`").append(table).append("`");
        sql.append(" SET");
        buildQuery('U', sql, null, Set.class, clauses, " ", ", ");

    }

    protected void buildInsert(StringBuilder sql) {
        sql.append("INSERT INTO ");
        sql.append("`").append(table).append("`");
        int t = buildQuery('I', sql, " (", Set.class, clauses, "", ", ");
        sql.append(") VALUES (");
        for (int i = 0; i < t; i++) {
            if (i == 0) {
                sql.append("?");
            } else {
                sql.append(", ?");
            }
        }
        sql.append(")");
    }

    protected void buildDelete(StringBuilder sql) {
        sql.append("DELETE");
    }

    public StringBuilder build(char type) {
        StringBuilder sql = new StringBuilder();

        switch (type) {
            case 'U': {
                buildUpdate(sql);
                break;
            }
            case 'D': {
                buildDelete(sql);
                buildTable(sql);
                break;
            }
            case 'I': {
                buildInsert(sql);
                break;
            }
            default: {
                buildSelect(sql);
                buildTable(sql);
            }
        }

        buildJoin(sql, type);

        buildWhere(sql, type);

        buildGroupBy(sql, type);

        buildOrderBy(sql, type);

        buildLimit(sql, type);

        buildOffset(sql, type);

        return sql;
    }

    public PreparedStatement toStatement(char type) throws SQLException {
        PreparedStatement stmt = database.createStatement(build(type).toString(), buildParam(type));
        stmt.closeOnCompletion();
        return stmt;
    }

    public String toString(char type) {
        try {
            PreparedStatement stmt = toStatement(type);
            stmt.close();
            return stmt.toString();
        } catch (SQLException ex) {
            Log.printError(ex);
            return super.toString();
        }
    }

    @Override
    public String toString() {
        return toString('I');
    }

//    @Override
//    public String toString() {
//        String query = build();
//
//        Object[] param = buildParam(buildTable());
//        //    int skip = find(Column.class).size() + find(Table.class).size();
//        int max = whereSize();
//        int p = 0;
//        for (Object object : param) {
//            String val = object == null ? "null" : object.toString();
//            if ((object instanceof String && p < max) || object instanceof Timestamp || object instanceof Date || object instanceof JsonElement) {
//                val = "'" + val.replace("'", "''") + "'";
//            }
//            p++;
//            query = query.replaceFirst("\\?", val);
//        }
//        return query;
//    }
    public void reset() {
        clauses.clear();
        firstWhere = true;
    }

    public static String parseColumn(String table, String col) {;
        return parseColumn(table, col, true);
    }

    public static String parseColumn(String table, String col, boolean carrot) {
        if (!col.matches("\\w+")) {
            return col;
        } else {
            String pref = table != null ? table + "." : "";
            if (carrot) {
                col = pref + "`" + col + "`";
            } else {
                col = pref + col;
            }
            return col;
        }
    }

    private static void buildParam(char type, List<Clause> clauses, List<Object> objects) {
        for (Clause clause : clauses) {

            Object value = clause.value(type);
            if (value != null) {
                if (value.getClass().isArray()) {
                    for (int i = 0; i < Array.getLength(value); i++) {
                        objects.add(Array.get(value, i));
                    }
                } else if (value instanceof List) {
                    List list = (List) value;

                    for (int i = 0; i < list.size(); i++) {
                        objects.add(list.get(i));
                    }
                } else {
                    objects.add(value);
                }
            } else {
                objects.add(value);
            }
        }

    }

    private static void buildParam(char type, Class<? extends Clause> classe, List<Clause> clauses, List<Object> objects) {
        buildParam(type, find(classe, clauses), objects);
    }

    private static List<Clause> find(Class<? extends Clause> classe, List<Clause> clauses) {
        List<Clause> list = new ArrayList<>();
        for (Clause clause : clauses) {
            if (clause.getClass().equals(classe)) {
                list.add(clause);
            }
        }
        return list;
    }

    private static List<Clause> remove(Class<? extends Clause> classe, List<Clause> clauses) {
        List<Clause> list = new ArrayList<>();
        clauses.removeIf(classe::isInstance);
        return list;
    }

    private static int buildQuery(char type, StringBuilder sql, String clause, Class<? extends Clause> cla, List<Clause> clauses, String p1, String p2) {
        List<Clause> wheres = find(cla, clauses);
        if (wheres.isEmpty()) {
            return 0;
        }
        if (clause != null) {
            sql.append(clause);
        }

        return buildQuery(type, sql, wheres, p1, p2);
    }

    private static int buildQuery(char type, StringBuilder sql, List<Clause> clauses, String p1, String p2) {
        boolean first = true;
        for (Clause clause : clauses) {
            if (first) {
                sql.append(p1);
                sql.append(clause.query(type));
                first = false;
            } else {
                sql.append(p2);
                sql.append(clause.query(type));
            }
        }
        return clauses.size();
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        for (Clause clause : clauses) {
            clause.setTable(table);
        }
        this.table = table;
    }

    public void clearSelects() {
        remove(Column.class, clauses);
    }

    public void clearSets() {
        remove(Set.class, clauses);
    }

    public ResultSet tryExecuteInsert() throws SQLException {
        return database.tryExecuteReturnigGeneratedKeys(build('I').toString(), buildParam('I'));
    }

    public ResultSet tryExecuteUpdate() throws SQLException {
        return database.tryExecuteReturnigGeneratedKeys(build('U').toString(), buildParam('U'));
    }

    public ResultSet tryExecuteSelect() throws SQLException {
        return database.tryExecute(build('S').toString(), buildParam('S'));
    }

    public long tryExecuteDelete() throws SQLException {
        return database.tryExecuteUpdate(build('D').toString(), buildParam('D'));
    }

    public long tryCountDistinct(String column) throws SQLException {
        long value = 0;
        clearSelects();
        select("SELECT DISTINCT count(\"" + column + "\")");
        ResultSet res = tryExecuteSelect();
        if (res.next()) {
            value = res.getLong(1);
        }
        res.close();
        return value;
    }

    public long tryCount() throws SQLException {
        long value = 0;
        clearSelects();
        select("SELECT count(*)");
        ResultSet res = tryExecuteSelect();
        if (res.next()) {
            value = res.getLong(1);
        }
        res.close();
        return value;
    }

    public long countDistinct(String column) {
        try {
            return tryCountDistinct(column);
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return -1;
    }

    public long count() {
        try {
            return tryCount();
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return -1;
    }
}
