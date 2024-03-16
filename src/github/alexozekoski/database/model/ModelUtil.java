/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.Log;
import github.alexozekoski.database.SQLite;
import github.alexozekoski.database.model.cast.Cast;
import github.alexozekoski.database.model.field.FileBase64;
import github.alexozekoski.database.query.Join;
import github.alexozekoski.database.query.Query;
import github.alexozekoski.database.query.QueryModel;
import github.alexozekoski.database.validation.Validator;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alexo
 */
public class ModelUtil {

    public static Map<Class, Cast> CASTS = defaultCasts();

    private static Map<Class, Cast> defaultCasts() {
        Map<Class, Cast> map = new HashMap();
        map.put(String.class, new github.alexozekoski.database.model.cast.CastString());

        addObjectPrimitive(long.class, Long.class, map, new github.alexozekoski.database.model.cast.CastLong());
        addObjectPrimitive(int.class, Integer.class, map, new github.alexozekoski.database.model.cast.CastInteger());
        addObjectPrimitive(double.class, Double.class, map, new github.alexozekoski.database.model.cast.CastDouble());
        addObjectPrimitive(byte.class, Byte.class, map, new github.alexozekoski.database.model.cast.CastByte());
        addObjectPrimitive(boolean.class, Boolean.class, map, new github.alexozekoski.database.model.cast.CastBoolean());
        addObjectPrimitive(char.class, Character.class, map, new github.alexozekoski.database.model.cast.CastCharacter());
        addObjectPrimitive(float.class, Float.class, map, new github.alexozekoski.database.model.cast.CastFloat());
        addObjectPrimitive(short.class, Short.class, map, new github.alexozekoski.database.model.cast.CastShort());

        map.put(JsonElement.class, new github.alexozekoski.database.model.cast.CastJson());
        map.put(JsonObject.class, new github.alexozekoski.database.model.cast.CastJson());
        map.put(JsonArray.class, new github.alexozekoski.database.model.cast.CastJson());

        map.put(java.sql.Date.class, new github.alexozekoski.database.model.cast.CastDate());
        map.put(java.sql.Timestamp.class, new github.alexozekoski.database.model.cast.CastTimestamp());
        map.put(java.sql.Time.class, new github.alexozekoski.database.model.cast.CastTime());
        map.put(java.util.Date.class, new github.alexozekoski.database.model.cast.CastDateUtil());
        map.put(FileBase64.class, new github.alexozekoski.database.model.cast.CastFileBase64());
        map.put(Model.class, new github.alexozekoski.database.model.cast.CastModel());
        map.put(Blob.class, new github.alexozekoski.database.model.cast.CastBlob());

        return map;
    }

    private static void addObjectPrimitive(Class prim, Class obj, Map<Class, Cast> map, Cast cast) {
        map.put(prim, cast);
        map.put(obj, cast);
    }

    public static Cast getCast(Class type) {
        Cast cast = CASTS.get(type);
        if (cast != null) {
            return cast;
        }
        for (Class classe : CASTS.keySet()) {
            if (classe.isAssignableFrom(type)) {
                return CASTS.get(classe);
            }
        }
        return cast;
    }

    public static Field[] getAllColumns(Class classe) {
        return getAllColumns(classe, false, false, false, false, false);
    }

    public static Field[] getValidateColumns(Class classe) {
        return getAllColumns(classe, false, false, false, false, true);
    }

    public static Field getColumn(Class classe, String column) {
        Field[] fields = getAllColumns(classe);
        for (Field field : fields) {
            Column col = field.getAnnotation(Column.class);
            if (col.value().equals(column)) {
                return field;
            }
        }
        return null;
    }

    public static Field[] getPrimaryColumns(Class model) {
        int total = 0;
        for (Field field : model.getFields()) {
            Column coluna = (Column) field.getAnnotation(Column.class);
            if (coluna != null && coluna.primary()) {
                total++;
            }
        }
        Field[] valores = new Field[total];
        total = 0;
        for (Field field : model.getFields()) {
            Column coluna = (Column) field.getAnnotation(Column.class);
            if (coluna != null && coluna.primary()) {
                valores[total++] = field;
            }
        }
        return valores;
    }

    public static Field[] getNormalColumns(Class model) throws IllegalArgumentException, IllegalAccessException {
        int total = 0;
        for (Field field : model.getFields()) {
            Column coluna = (Column) field.getAnnotation(Column.class);
            if (coluna != null && !coluna.primary()) {
                total++;
            }
        }
        Field[] valores = new Field[total];
        total = 0;
        for (Field field : model.getFields()) {
            Column coluna = (Column) field.getAnnotation(Column.class);
            if (coluna != null && !coluna.primary()) {
                valores[total++] = field;
            }
        }
        return valores;
    }

    public static Field[] getMigrationColumns(Class model) {
        Field[] mFields = model.getFields();
        List<Field> fields = new ArrayList<>(mFields.length);
        for (Field field : model.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && !column.value().isEmpty() && column.migration()) {
                fields.add(field);
            }
        }
        return fields.toArray(new Field[fields.size()]);

    }

    public static boolean canSelect(Column column) {
        return column.select() && !column.value().isEmpty();
    }

    public static boolean canUpdate(Column column) {
        return column.update() && !column.value().isEmpty();
    }

    public static boolean canInsert(Column column) {
        return column.insert() && !column.value().isEmpty();
    }

    public static boolean canFill(Column column) {
        return column.fill();
    }

    public static JsonObject toJson(Column column) {
        JsonObject json = new JsonObject();
        json.addProperty("fill", column.fill());
        json.addProperty("insert", column.insert());
        json.addProperty("update", column.update());
        json.addProperty("text", column.text());
        json.addProperty("string", column.varchar());
        json.addProperty("text", column.text());
        json.addProperty("notnull", column.notnull());
        return json;
    }

    public static boolean isForeign(Column column) {
        return column != null && !column.value().isEmpty() && ((getTable(column.foreignKey()) != null) || !column.foreign().isEmpty() && !column.key().isEmpty());
    }

    public static String getForeignTable(Column column) {
        String table = ModelUtil.getTable(column.foreignKey());
        if (table != null) {
            return table;
        } else {
            if (!column.foreign().isEmpty()) {
                return column.foreign();
            }
        }
        return null;
    }

    public static JsonObject validate(Model model, String... columns) {
        return Validator.validate(model, columns);
    }

    public static Join[] getJoinColumn(Class model, Database database) {
        Field[] mFields = model.getFields();
        List<Join> joins = new ArrayList<>(mFields.length);
        for (Field field : model.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (isForeign(column) && column.join()) {
                String table = getTable(column.foreignKey());
                String key = "id";
                if (table == null) {
                    table = column.foreign();
                    key = column.key();
                }
                joins.add(new Join("INNER JOIN", table, Query.parseColumn(table, key, database.getMigrationType()) + " = " + column.value(), database.getMigrationType()));
            }
        }
        return joins.toArray(new Join[joins.size()]);
    }

    public static Field[] getAllColumns(Class model, boolean insert, boolean update, boolean select, boolean fill, boolean validate) {
        Field[] mFields = model.getFields();
        List<Field> fields = new ArrayList<>(mFields.length);
        for (Field field : model.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && (!insert || canInsert(column)) && (!update || canUpdate(column)) && (!select || canSelect(column)) && (!fill || canFill(column)) && (!validate || column.validate())) {
                fields.add(field);
            }
        }
        return fields.toArray(new Field[fields.size()]);
    }

    public static Object getQuery(Model model, List<Model> stack, Field field, JsonElement value, boolean where) {
        try {
            Class classe = field.getType();
            if (classe.isArray()) {
                classe = classe.getComponentType();
                Cast cast = getCast(classe);
                if (cast != null) {
                    return cast.fieldArrayToSql(model, field, classe, cast.jsonArrayToFieldArray(model, stack, field, classe, value), where);
                }
            } else if (List.class.isAssignableFrom(classe)) {
                Cast cast = getCast(classe);
                if (cast != null) {
                    Arrays.asList(cast.fieldArrayToSql(model, field, classe, cast.jsonArrayToFieldArray(model, stack, field, classe, value), where));
                }
            } else {
                Cast cast = getCast(classe);
                if (cast != null) {
                    return cast.fieldToSql(model, field, classe, cast.jsonToField(model, stack, field, classe, value), where);
                }
            }
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return null;
    }

    public static void set(Model model, List<Model> stack, Field field, JsonElement value) throws IllegalArgumentException, IllegalAccessException {
        Object ob = getField(model, stack, field, value);
        if (field.getType().isPrimitive() && ob == null) {
            return;
        }
        field.set(model, ob);
    }

    public static Object getField(Model model, List<Model> stack, Field field, JsonElement value) {
        try {
            Class classe = field.getType();
            if (classe.isArray()) {
                classe = classe.getComponentType();
                Cast cast = getCast(classe);
                if (cast != null) {
                    return cast.jsonArrayToFieldArray(model, stack, field, classe, value);
                }
            } else if (List.class.isAssignableFrom(classe)) {
                List list = ModelList.class.isAssignableFrom(classe) ? new ModelList(classe) : new ArrayList();
                Cast cast = getCast(classe);
                if (cast != null) {
                    Object array = cast.jsonArrayToFieldArray(model, stack, field, classe, value);
                    if (array != null) {
                        int size = Array.getLength(array);
                        for (int i = 0; i < size; i++) {
                            list.add(Array.get(array, i));
                        }
                    }
                }
            } else {
                Cast cast = getCast(classe);
                if (cast != null) {
                    return cast.jsonToField(model, stack, field, classe, value);
                }
            }
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return null;
    }

    public static Object getQuery(Model model, Field field, boolean where) {
        try {
            Class classe = field.getType();
            Object value = ModelUtil.getObject(model, field);
            if (classe.isArray()) {
                classe = classe.getComponentType();
                Cast cast = getCast(classe);
                if (cast != null) {
                    return cast.fieldArrayToSql(model, field, classe, value, where);
                }
            } else if (List.class.isAssignableFrom(classe)) {
                Cast cast = getCast(classe);
                if (cast != null) {
                    return cast.fieldArrayToSql(model, field, classe, value, where);
                }
            } else {
                Cast cast = getCast(classe);
                if (cast != null) {
                    return cast.fieldToSql(model, field, classe, value, where);
                }
            }
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return null;
    }

    public static <T extends Model<T>> JsonObject toJson(Model<T> model, boolean force, String... columns) {
        JsonObject json = new JsonObject();
        Field[] campos = getAllColumns(model.getClass());
        List<String> select = columns == null ? new ArrayList<>() : Arrays.asList(columns);
        if (select.contains("*")) {
            select = new ArrayList<>();
        }
        for (Field campo : campos) {
            try {
                Column col = campo.getAnnotation(Column.class);
                String coluna = col.name().isEmpty() ? col.value() : col.name();
                if (force || (col.json() && (select.isEmpty() || select.contains(col.value())))) {
                    Object valor = campo.get(model);
                    Class type = campo.getType();
                    if (type.isArray()) {
                        type = type.getComponentType();
                        Cast cast = getCast(type);
                        if (cast != null) {
                            JsonElement value = cast.fieldArrayToJsonArray(model, campo, type, valor);
                            json.add(coluna, value == null ? JsonNull.INSTANCE : value);
                        }

                    } else if (List.class.isAssignableFrom(type)) {
                        type = campo.getAnnotation(Column.class).listType();
                        Cast cast = getCast(type);
                        if (cast != null) {
                            JsonElement value = cast.fieldArrayToJsonArray(model, campo, type, valor);
                            json.add(coluna, value == null ? JsonNull.INSTANCE : value);
                        }
                    } else {
                        Cast cast = getCast(type);
                        if (cast != null) {
                            JsonElement value = cast.fieldToJson(model, campo, type, valor);
                            json.add(coluna, value == null ? JsonNull.INSTANCE : value);
                        }
                    }
                }
            } catch (Exception e) {
                Log.printError(e);
            }
        }

        return model.onToJson(json, force, columns);
    }

    public static void set(QueryModel query, Model model, Field field, Column col, Object value) throws Exception {
        String coluna = col.value();
        Class classe = field.getType();

        if (classe.isArray()) {
            classe = classe.getComponentType();
            Cast cast = getCast(classe);
            if (cast != null) {
                query.set(coluna, cast.fieldArrayToSql(model, field, classe, value, false));
            }
        } else if (List.class.isAssignableFrom(classe)) {
            Cast cast = getCast(classe);
            if (cast != null) {
                query.set(coluna, cast.fieldArrayToSql(model, field, classe, value, false));
            }
        } else {
            Cast cast = getCast(classe);
            if (cast != null) {
                query.set(coluna, cast.fieldToSql(model, field, classe, value, false));
            }
        }
    }

    public static void insert(Model model, String... columns) throws Exception {
        QueryModel query = model.query();
        model.onCreate();
//        if (model.getAction() != null) {
//            model.getAction().onCreate(model);
//        }
        Field[] campos = getNormalColumns(model.getClass());
        List<String> list = columns != null ? Arrays.asList(columns) : new ArrayList();
        for (Field campo : campos) {
            Column column = campo.getAnnotation(Column.class);
            if ((column.insert() && list.isEmpty()) || list.contains(column.value())) {
                set(query, model, campo, column, campo.get(model));
            }
        }
        campos = getPrimaryColumns(model.getClass());

        for (Field campo : campos) {
            Column column = campo.getAnnotation(Column.class);
            Object value = campo.get(model);
            if (!column.serial() && (value != null || !column.notnull()) && ((column.insert() && list.isEmpty()) || list.contains(column.value()))) {
                set(query, model, campo, column, value);
            }
        }
        ResultSet res = query.tryExecuteInsert();
        try {
            if (res.next()) {
                model.fill(res);
            }
        } catch (Exception ex) {
            res.close();
            throw ex;
        }
        res.close();
        model.afterCreate();
//        if (model.getAction() != null) {
//            model.getAction().afterCreate(model);
//        }

    }

    public static void refresh(Model model, String... columns) throws Exception {

        QueryModel query = null;
        if (columns == null || columns.length == 0) {
            query = model.query(model.getClass(), model.getDatabase());
        } else {
            query = model.query();
            query.select(columns);
        }

        Field[] campos = model.getPrimaryColumns();

        for (Field campo : campos) {
            Column column = campo.getAnnotation(Column.class);
            query.where(column.value(), campo.get(model));
        }
        ResultSet res = query.tryExecuteSelect();
        try {
            if (res.next()) {
                model.fill(res);
            }
        } catch (Exception ex) {
            throw ex;
        }
        res.close();

    }

    public static boolean update(Model model, String... columns) throws Exception {

        QueryModel query = model.query();
        model.onUpdate();
//        if (model.getAction() != null) {
//            model.getAction().onUpdate(model);
//        }
        Field[] campos = model.getNormalColumns();
        List<String> list = columns != null ? Arrays.asList(columns) : new ArrayList();
        for (Field campo : campos) {
            Column column = campo.getAnnotation(Column.class
            );
            if ((column.update() && list.isEmpty()) || list.contains(column.value())) {
                set(query, model, campo, column, campo.get(model));
            }
        }
        campos = model.getPrimaryColumns();

        for (Field campo : campos) {
            Column column = campo.getAnnotation(Column.class);
            if ((column.update() && list.isEmpty()) || list.contains(column.value())) {
                set(query, model, campo, column, campo.get(model));
            }
            query.where(column.value(), campo.get(model));
        }
        long res = query.tryExecuteUpdate();
        model.afterUpdate();
//        if (model.getAction() != null) {
//            model.getAction().afterUpdate(model);
//        }
        return res > 0;
    }

    public static void fill(Field field, Model model, List<Model> stack, Object value) {
        try {
            Class<?> clasee = field.getType();
            boolean primitive = clasee.isPrimitive();
            if (clasee.isArray()) {
                clasee = clasee.getComponentType();
                Cast cast = getCast(clasee);
                if (cast != null) {
                    field.set(model, cast.sqlToFieldArray(model, stack, field, clasee, value));
                }
            } else if (clasee.isArray()) {
                clasee = clasee.getComponentType();
                Cast cast = getCast(clasee);
                if (cast != null) {
                    field.set(model, cast.sqlToFieldArray(model, stack, field, clasee, value));
                }
            } else {

                Cast cast = getCast(clasee);
                if (cast != null) {
                    Object val = cast.sqlToField(model, stack, field, clasee, value);
                    if (primitive) {
                        if (val != null) {
                            field.set(model, val);
                        }
                    } else {
                        field.set(model, val);
                    }
                }
            }
        } catch (Exception ex) {
            Log.printError(ex);
        }
    }

    public static void fill(Model model, ResultSet res, List<Model> stack) throws SQLException {

        ResultSetMetaData resultSetMetaData = res.getMetaData();
        Field[] campos = model.getAllColumns(false, false, true, false, false);

        if (resultSetMetaData.getColumnCount() == 1 && model.getDatabase().getJdbc().equals(SQLite.JDBC) && resultSetMetaData.getColumnLabel(1).equals("last_insert_rowid()")) {
            for (Field campo : campos) {
                String nome = campo.getAnnotation(Column.class
                ).value();
                if (nome.equals("id")) {
                    Long val = res.getLong(1);
                    if (val == 0) {
                        return;
                    }
                    fill(campo, model, stack, val);
                }
            }
            return;
        }
        for (Field campo : campos) {
            Column column = campo.getAnnotation(Column.class);
            Object val = res.getObject(column.value());
            fill(campo, model, stack, val);
        }

    }

    public static int hashCode(Model model) {
        Field[] fields = getAllColumns(model.getClass());
        int hash = 0;
        for (Field field : fields) {
            Object value;
            try {
                value = field.get(model);
                if (value != null) {
                    hash += field.hashCode();
                }
            } catch (Exception ex) {
                Log.printError(ex);
            }
        }
        return hash;
    }

    public static Object getObject(Model model, Field field) throws IllegalArgumentException, IllegalAccessException {
        return field.get(model);
    }

    public static String getTable(Class classe) {
        Table table = (Table) classe.getAnnotation(Table.class);
        if (table != null) {
            return table.value();
        }
        return null;
    }

    public static String getDatabase(Class classe) {
        Table table = (Table) classe.getAnnotation(Table.class);
        if (table != null) {
            return table.database();
        }
        return null;
    }

    public static String toJsonFormatted(JsonElement json) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .serializeNulls()
                .create();
        return gson.toJson(json);
    }
}
