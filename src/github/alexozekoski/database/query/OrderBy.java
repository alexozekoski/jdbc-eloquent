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
public class OrderBy implements Clause {

    private Object column;
    private String dir;
    private String table;

    public OrderBy(Object column, String dir, String table) {
        this.column = column;
        this.dir = dir;
        this.table = table;
    }

    @Override
    public String query(char type) {
        if (column instanceof String) {
            return Query.parseColumn(table, (String) column) + " " + dir;
        }
        return column.toString() + " " + dir;
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
