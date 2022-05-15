/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.query;

/**
 *
 * @author alexozekoski
 */
public class GroupBy implements Clause {

    private Object column;

    private String table;

    public GroupBy(Object column, String table) {
        this.column = column;
        this.table = table;
    }

    @Override
    public String query(char type) {
        if (column instanceof String) {
            return Query.parseColumn(table, (String) column);
        }
        return column.toString();
    }

    @Override
    public Object value(char type) {
        return column;
    }

    public Object getColumn() {
        return column;
    }

    public void setColumn(Object column) {
        this.column = column;
    }

    @Override
    public void setTable(String table) {
        this.table = table;
    }
}
