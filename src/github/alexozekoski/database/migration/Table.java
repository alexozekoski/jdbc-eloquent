/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.migration;

import github.alexozekoski.database.Database;

/**
 *
 * @author Usuario
 */
public class Table {

    private String name;

    private Column[] colunas = new Column[0];

    private MigrationType migration;

    private Database database;

    public Table(String name, MigrationType migration, Database database) {
        this.name = name;
        this.migration = migration;
        this.database = database;
    }

    public void serialModel() {
        serialID();
        datetime("created").notnull();
        datetime("updated").notnull();
    }

    public Column string(String column, int size) {
        return add(column, migration.varchar(size));
    }

    public Column string(String column) {
        return string(column, 255);
    }

    public Column text(String column) {
        return add(column, migration.text());
    }

    public Column booleano(String column) {
        return add(column, migration.booleano());
    }

    public Column integer(String column) {
        return add(column, migration.integer());
    }

    public Column bigint(String column) {
        return add(column, migration.bigint());
    }

    public Column serial(String column) {
        return add(column, migration.serial());
    }

    public Column custom(String column, String type) {
        return add(column, type);
    }

    public Column custom(String column, int JDBCtype) {
        return add(column, java.sql.JDBCType.valueOf(JDBCtype).getName());
    }

    public Column bigserial(String column) {
        return add(column, migration.bigserial()).autoincrement().primaryKey();
    }

    public Column numeric(String column) {
        return add(column, migration.numeric());
    }

    public Column numeric(String column, int inteiro, int decimal) {
        return add(column, migration.numeric(inteiro, decimal));
    }

    public Column decimal(String column) {
        return add(column, migration.decimal());
    }

    public Column decimal(String column, int inteiro, int decimal) {
        return add(column, migration.decimal(inteiro, decimal));
    }

    public Column date(String column) {
        return add(column, migration.date());
    }

    public Column datetime(String column) {
        return add(column, migration.datetime());
    }

    public Column time(String column) {
        return add(column, migration.time());
    }

    public Column character(String column, int size) {
        return add(column, migration.character(size));
    }

    public Column character(String column) {
        return character(column, 1);
    }

    private Column add(String col, String type) {
        Column coluna = new Column(col, type, database.getMigrationType());
        Column[] colunasTemp = new Column[colunas.length + 1];
        for (int i = 0; i < colunas.length; i++) {
            colunasTemp[i] = colunas[i];
        }
        colunasTemp[colunas.length] = coluna;
        colunas = colunasTemp;
        return coluna;
    }

    public String getName() {
        return name;
    }

    public Column[] getColumns() {
        return colunas;
    }

    public Column serialID() {
        return bigserial("id");
    }

    @Override
    public String toString() {
        String text = "";
        for (Column c : colunas) {
            if (!text.isEmpty()) {
                text += ",\n" + c.toString();
            } else {
                text += c.toString();
            }
        }
        text = name + " (\n" + text + "\n);";
        return text;
    }

    public String createQuery() {
        return migration.createTable(name) + " " + toString();
    }

    public String dropTableQuery() {

        return migration.dropTable(name);
    }

    public boolean create() {
        return database.executeUpdate(createQuery()) != 0;
    }

    public boolean dropTable() {
        return database.executeUpdate(dropTableQuery()) != -1;
    }
}
