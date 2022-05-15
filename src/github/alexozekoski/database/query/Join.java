/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.query;

/**
 *
 * @author alexo
 */
public class Join implements Clause {

    private String join;
    private String table;
    private String query;

    public Join(String join, String table, String query) {
        this.join = join;
        this.table = table;
        this.query = query;
    }

    @Override
    public String query(char type) {
        return join + " " + this.table + " ON " + query;
    }

    @Override
    public Object value(char type) {
        return query(type);
    }

    @Override
    public void setTable(String table) {

    }
}
