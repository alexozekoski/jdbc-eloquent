/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.migration;

import github.alexozekoski.database.model.ModelUtil;
import github.alexozekoski.database.model.Serial;

/**
 *
 * @author Usuario
 */
public class Column {

    private String name = null;

    private String type = null;

    private boolean notnull = false;

    private boolean unique = false;

    private boolean autoincrement = false;

    private boolean primary = false;

    private String ondelete = null;

    private String onupdate = null;

    private String foreignTable = null;

    private String foreignColumn = null;

    private String value = null;

    private MigrationType types;

    private boolean modified = true;

    private boolean index = false;

    public Column(String name, String type, MigrationType types) {
        this.name = name;
        this.type = type.toUpperCase();
        this.types = types;
    }

    public Column notnull() {
        return notnull(true);
    }

    public Column notnull(boolean notnull) {
        setNotnull(notnull);
        return this;
    }

    public Column nullable() {
        return nullable(true);
    }

    public Column nullable(boolean nullable) {
        return notnull(nullable);
    }

    public boolean isNotnull() {
        return notnull;
    }

    public void setNotnull(boolean notnull) {
        this.notnull = notnull;
        checkModified();
    }

    public Column unique() {
        return unique(true);
    }

    public Column unique(boolean unique) {
        setUnique(unique);
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
        checkModified();
    }

    public Column autoincrement() {
        return autoincrement(true);
    }

    public Column autoincrement(boolean autoincrement) {
        setAutoincrement(autoincrement);
        return this;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
        checkModified();
    }

    public Column primaryKey() {
        return primaryKey(true);
    }

    public Column primaryKey(boolean primary) {
        setPrimaryKey(primary);
        return this;
    }

    public boolean isPrimaryKey() {
        return primary;
    }

    public void setPrimaryKey(boolean primary) {
        this.primary = primary;
        checkModified();
    }

    public Column onDelete(String ondelete) {
        setOnDelete(ondelete);
        return this;
    }

    public String getOnDelete() {
        return ondelete;
    }

    public void setOnDelete(String ondelete) {
        this.ondelete = ondelete;
        checkModified();
    }

    public Column onUpdate(String onupdate) {
        setOnUpdate(onupdate);
        return this;
    }

    public String getOnUpdate() {
        return onupdate;
    }

    public void setOnUpdate(String onupdate) {
        this.onupdate = onupdate;
        checkModified();
    }

    public Column defaultValue(String value) {
        setDefaultValue(value);
        return this;
    }

    public String getDefaultValue() {
        return value;
    }

    public void setDefaultValue(String value) {
        this.value = value;
        checkModified();
    }

    public Column foreignKey(Class<? extends Serial> model) {
        return foreignKey(model, "id");
    }

    public Column foreignKey(Class<? extends Serial> table, String column) {
        return foreignKey(ModelUtil.getTable(table), column);
    }

    public Column foreignKey(String table, String column) {
        setForeignTable(table);
        setForeignColumn(column);
        return this;
    }

    public String getForeignTable() {
        return foreignTable;
    }

    public void setForeignTable(String foreignTable) {
        this.foreignTable = foreignTable;
        checkModified();
    }

    public String getForeignColumn() {
        return foreignColumn;
    }

    public void setForeignColumn(String foreignColumn) {
        this.foreignColumn = foreignColumn;
        checkModified();
    }

    @Override
    public String toString() {
        return createRow();
    }

    private void checkModified() {

    }

    public String createRow() {
        String row = "\"" + name + "\" " + type;
        if (notnull) {
            row += " NOT NULL";
        }
        if (unique) {
            row += " UNIQUE";
        }
        if (autoincrement) {
            row += " UNIQUE";
        }
        if (primary) {
            row += " PRIMARY KEY";
        }

        if (autoincrement) {
            String incre = types.increment();
            if (incre != null) {
                row += " " + incre;
            }
        }
        if (foreignTable != null && foreignColumn != null) {
            row += " REFERENCES " + foreignTable + "(" + foreignColumn + ")";
        }
        if (ondelete != null) {
            row += " ON DELETE " + ondelete;
        }
        if (onupdate != null) {
            row += " ON UPDATE " + onupdate;
        }
        if (value != null) {
            row += " DEFAULT " + value;
        }
        return row;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        checkModified();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
        checkModified();
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isIndex() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index = index;
        checkModified();
    }

    public Column index() {
        return index(true);
    }

    public Column index(boolean index) {
        setIndex(index);
        return this;
    }
}
