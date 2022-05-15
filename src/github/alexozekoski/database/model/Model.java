/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.Log;
import github.alexozekoski.database.query.Query;
import github.alexozekoski.database.query.QueryModel;
import github.alexozekoski.database.validation.Validation;
import github.alexozekoski.database.validation.Validator;
import java.lang.reflect.Constructor;

/**
 *
 * @author Usuario
 * @param <T>
 */
public class Model<T extends Model<T>> {

    public static Database DEFAULT_DATABASE = null;

    private Database database = DEFAULT_DATABASE;

    private ModelAction<T> action = null;

    private boolean debugger;

    public Model() {

    }

    public Model(Database database) {
        this.database = database;
    }

//    public Model(Object... values) {
//        create(values);
//    }
    public Model(JsonObject values) {
        create(values);
    }

    public static <M extends Model<M>> QueryModel<M> query(Class<M> classe) {
        return Model.query(classe, DEFAULT_DATABASE);
    }

    public static <M extends Model<M>> QueryModel<M> query(Class<M> classe, Database database) {
        return database.query(classe);
    }

    public static <M extends Model> M newInstance(Class<M> classe) {
        return newInstance(classe, null);
    }

    public static <M extends Model> M newInstance(Class<M> classe, Database database) {
        try {
            return tryNewInstance(classe, database);
        } catch (Exception e) {
            Log.printError(e);
        }
        return null;
    }

    public static <M extends Model> M tryNewInstance(Class<M> classe) throws Exception {
        return tryNewInstance(classe, null);
    }

    public static <M extends Model> M tryNewInstance(Class<M> classe, Database database) throws Exception {
        Constructor constructor;
        if (database != null) {
            constructor = classe.getConstructor(Database.class);
            if (constructor != null) {
                return (M) constructor.newInstance(database);
            }
        }
        constructor = classe.getConstructor();
        if (constructor != null) {
            M model = (M) constructor.newInstance();
            if (database != null) {
                model.setDatabase(database);
            }
            return model;
        }
        throw new Exception("A constructor without arguments or with database was not found");
    }

    public String table() {
        return ModelUtil.getTable(this.getClass());
    }

    public String[] columns() {
        int total = 0;

        for (Field field : this.getClass().getFields()) {
            if (field.getAnnotation(Column.class
            ) != null) {
                total++;
            }
        }
        String[] valores = new String[total];
        total = 0;
        String table = table();

        for (Field field : this.getClass().getFields()) {
            if (field.getAnnotation(Column.class
            ) != null) {
                valores[total++] = Query.parseColumn(table, ((Column) field.getAnnotation(Column.class
                )).value(), false);
            }
        }
        return valores;
    }

    public Field[] getPrimaryColumns() {
        return ModelUtil.getPrimaryColumns(getClass());
    }

    public Field[] getNormalColumns() throws IllegalArgumentException, IllegalAccessException {
        return ModelUtil.getNormalColumns(getClass());
    }

    public T set(JsonObject json) {
        return set(json, false, false, false, true);
    }

    public T update(JsonObject json) {

        if (set(json, false, true, false, true) != null && update()) {
            return (T) this;
        } else {
            return null;
        }
    }

    public T create(JsonObject json) {
        if (set(json, true, false, false, true) != null && insert()) {
            return (T) this;
        } else {
            return null;
        }
    }

//    private void set(Field field, JsonElement value) throws IllegalArgumentException, IllegalAccessException {
//        Object ob = ModelUtil.getField(field, value);
//        field.set(this, ob);
//    }
//    public boolean canFill(Field field, Column column) {
//        return column.fill();
//    }
    public void onFill(JsonObject json) {

    }

    public T set(JsonObject json, boolean insert, boolean update, boolean select, boolean fill) {
        try {
            Field[] campos = ModelUtil.getAllColumns(getClass(), insert, update, select, fill);
            List<Model> stack = new ArrayList();

            for (Field campo : campos) {
                Column col = campo.getAnnotation(Column.class
                );
                String prop = !col.name().isEmpty() ? col.name() : col.value();
                if (json.has(prop)) {
                    JsonElement valor = json.get(prop);
                    ModelUtil.set(this, stack, campo, valor);
                }
            }
        } catch (Exception e) {
            Log.printError(e);
            return null;
        }
        onFill(json);
        return (T) this;
    }

//    public T create(Object... values) {
//        try {
//            set(values);
//            insert();
//        } catch (Exception e) {
//            Log.printError(e);
//            return null;
//        }
//        return (T) this;
//    }
//    public T set(Object... valores) {
//        try {
//            for (int i = 0; i < valores.length; i += 2) {
//                String column = (String) valores[i];
//                Object value = valores[i + 1];
//                set(column, value);
//            }
//        } catch (Exception e) {
//            Log.printError(e);
//        }
//        return (T) this;
//    }
    public String toJsonString() {
        return toJsonString(false);
    }

    public String toJsonString(boolean formated) {
        if (formated) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .create();
            return gson.toJson(toJson());
        } else {
            return toJson().toString();
        }
    }

    public JsonObject toJson() {
        return toJson(false);
    }

    public JsonObject toJson(boolean force) {
        return ModelUtil.toJson(this, force);
    }

    public T save() {
        if (!update()) {
            if (!insert()) {
                return null;
            }
        }
        return (T) this;
    }

//    public T set(String column, Object value) {
//        Field[] campos = getAllColumns();
//        for (Field campo : campos) {
//            Column col = ((Column) campo.getAnnotation(Column.class));
//            String coluna = col.value();
//            if (coluna.equals(column)) {
//                try {
//                    return set(campo, col, value);
//                } catch (Exception ex) {
//                    Log.printError(ex);
//                }
//            }
//        }
//        return (T) this;
//    }
//    public T set(QueryModel query, Field field, Column col, Object value) throws Exception {
//        String coluna = col.value();
//        for (Cast cast : CASTS) {
//            Class classe = field.getType();
//            if (cast.type(field, classe, value).isAssignableFrom(classe)) {
//                query.set(coluna, cast.castSql(field, value));
//                break;
//            }
//        }
//        return (T) this;
//    }
    public boolean insert() {
        return ModelUtil.insert(this);
    }

    public boolean update() {
        return ModelUtil.update(this);
    }

    public void fill(ResultSet res) throws SQLException, IllegalArgumentException, IllegalAccessException {
        ModelUtil.fill(this, res, new ArrayList());
    }

    public boolean delete() {
        try {
            QueryModel query = query();
            Field[] primary = getPrimaryColumns();

            for (Field key : primary) {
                Column col = key.getAnnotation(Column.class
                );
                query.where(col.value(), ModelUtil.getQuery(this, key));
            }
            onDelete();
            if (action != null) {
                action.onDelete((T) this);
            }
            boolean res = query.tryExecuteDelete() > 0;
            if (res) {
                afterDelete();
                if (action != null) {
                    action.afterDelete((T) this);
                }
            }
            return res;
        } catch (Exception e) {
            Log.printError(e);
        }
        return false;
    }

    public QueryModel<T> query() {
        return database.query((Class<T>) getClass());
    }

    public void onUpdate() {

    }

    public void onCreate() {

    }

    public void onDelete() {

    }

    public void onSelect() {

    }

    public void afterSelect() {

    }

    public void afterUpdate() {

    }

    public void afterCreate() {

    }

    public void afterDelete() {

    }

    public Database getDatabase() {
        return database;
    }

    public T setDatabase(Database database) {
        this.database = database;
        return (T) this;
    }

    public long count() {
        return query().count();
    }

    public long countDistinct(String colmun) {
        return query().countDistinct(colmun);
    }

//    @Override
//    public String build() {
//        String res;
//        switch (mode) {
//            case 'S':
//                res = buildSelect(buildTable()) + super.build();
//                break;
//            case 'I':
//                res = buildInsert() + super.build();
//                break;
//            case 'D':
//                res = buildDelete() + super.build();
//                break;
//            case 'C':
//                res = buildCount() + super.build();
//                break;
//            default:
//                res = buildUpdate(buildTable()) + super.build();
//                break;
//        }
//        if (debugger) {
//            System.out.println(res);
//            Object[] param = buildParam();
//            for (Object ob : param) {
//                System.out.println(" --" + ob);
//            }
//        }
//        return res;
//    }
    public boolean isDebugger() {
        return debugger;
    }

    public T setDebugger(boolean debugger) {
        this.debugger = debugger;
        return (T) this;
    }

    public github.alexozekoski.database.migration.Table migrate() {
        return getDatabase().migrate(this.getClass());
    }

    public boolean validationFiled(String[] values, String type, Field field) {
        for (String value : values) {
            return type == null || value.isEmpty() || type.equals("*") || value.equals("*") || value.contains(type);
        }
        return false;
    }

    private Field[] allValidationColumns(String type) {
        int total = 0;

        for (Field field : getClass().getFields()) {
            Validation[] validations = field.getAnnotationsByType(Validation.class
            );
            for (Validation validation : validations) {
                boolean vlid = validationFiled(validation.value(), type, field);
                if (vlid) {
                    total++;
                }
            }
        }
        Field[] valores = new Field[total];
        total = 0;

        for (Field field : getClass().getFields()) {
            Validation[] validations = field.getAnnotationsByType(Validation.class
            );
            for (Validation validation : validations) {
                boolean vlid = validationFiled(validation.value(), type, field);
                if (vlid) {
                    valores[total++] = field;
                }
            }
        }
        return valores;
    }

    public JsonObject getErrors(String type) {
        Field[] campos = allValidationColumns(type);
        JsonObject validJson = new JsonObject();
        try {
            for (Field campo : campos) {
                String nome = campo.getName();
                Column col = campo.getAnnotation(Column.class
                );
                if (col != null) {
                    nome = col.value();
                }
                Object value = ModelUtil.getObject(this, campo);

                try {
                    Validation[] validations = campo.getAnnotationsByType(Validation.class
                    );
                    for (Validation validation : validations) {
                        String[] val = validation.value();
                        if (validationFiled(val, type, campo)) {
                            JsonObject erro = Validator.validField(validation, campo, this, value, getDatabase());
                            if (erro != null) {
                                validJson.add(nome, erro);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.printError(ex);
                }
            }
        } catch (Exception e) {
            Log.printError(e);
        }
        if (action != null) {
            action.getErrors((T) this, validJson, type);
        }
        return validJson.keySet().size() > 0 ? validJson : null;
    }

    public String getErrorsAsString(String type) {
        return getErrors(type).toString();
    }

    public String getErrorsAsString(String type, boolean formated) {
        if (formated) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .disableHtmlEscaping()
                    .create();
            return gson.toJson(getErrors(type));
        } else {
            return getErrorsAsString(type);
        }
    }

    public ModelAction<T> getAction() {
        return action;
    }

    public void setAction(ModelAction<T> action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return toJsonString(true);
    }

    public Field[] getAllColumns(boolean insert, boolean update, boolean select, boolean fill) {
        return ModelUtil.getAllColumns(this.getClass(), insert, update, select, fill);
    }

}
