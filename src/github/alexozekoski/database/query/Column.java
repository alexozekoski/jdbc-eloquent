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
public class Column implements Clause {

    private String name;
    
    private String table;

    public Column(String name, String table) {
        this.name = name;
        this.table = table;
    }

    @Override
    public String query(char type) {
        return Query.parseColumn(table, name);
    }

    @Override
    public Object value(char type) {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setTable(String table) {
        this.table = table;
    }

}
