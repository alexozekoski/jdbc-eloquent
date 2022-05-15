/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import github.alexozekoski.database.Log;
import github.alexozekoski.database.SQLite;
import github.alexozekoski.database.model.cast.Cast;
import github.alexozekoski.database.model.cast.CastUtil;
import github.alexozekoski.database.query.QueryModel;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alexo
 */
public class ModelUtil {

    public static List<Cast> CASTS = defaultCasts();

    private static List<Cast> defaultCasts() {
        List<Cast> list = new ArrayList();
        list.add(new github.alexozekoski.database.model.cast.CastString());
        list.add(new github.alexozekoski.database.model.cast.CastLong());
        list.add(new github.alexozekoski.database.model.cast.CastInteger());
        list.add(new github.alexozekoski.database.model.cast.CastDouble());
        list.add(new github.alexozekoski.database.model.cast.CastDate());
        list.add(new github.alexozekoski.database.model.cast.CastTimestamp());
        list.add(new github.alexozekoski.database.model.cast.CastJson());
        list.add(new github.alexozekoski.database.model.cast.CastTime());
        list.add(new github.alexozekoski.database.model.cast.CastByte());
        list.add(new github.alexozekoski.database.model.cast.CastBoolean());
        list.add(new github.alexozekoski.database.model.cast.CastCharacter());
        list.add(new github.alexozekoski.database.model.cast.CastFloat());
        list.add(new github.alexozekoski.database.model.cast.CastShort());
        list.add(new github.alexozekoski.database.model.cast.CastDateUtil());
        list.add(new github.alexozekoski.database.model.cast.CastModel());
        return list;
    }

    public static Field[] getAllColumns(Class classe) {
        return getAllColumns(classe, false, false, false, false);
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

    public static boolean isForeign(Column column) {
        return !column.value().isEmpty() && ((ModelUtil.getTable(column.foreignKey()) != null) || !column.foreign().isEmpty() && !column.key().isEmpty());
    }

    public static Field[] getAllColumns(Class model, boolean insert, boolean update, boolean select, boolean fill) {
        Field[] mFields = model.getFields();
        List<Field> fields = new ArrayList<>(mFields.length);
        for (Field field : model.getFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && (!insert || canInsert(column)) && (!update || canUpdate(column)) && (!select || canSelect(column)) && (!fill || canFill(column))) {
                fields.add(field);
            }
        }
        return fields.toArray(new Field[fields.size()]);
    }

    public static Object getQuery(Model model, List<Model> stack, Field field, JsonElement value) {
        try {
            Class classe = field.getType();
            if (classe.isArray()) {
                classe = classe.getComponentType();
                classe = CastUtil.primitiveToObject(classe);
                for (Cast cast : CASTS) {
                    if (cast.type(model, field, classe, value).isAssignableFrom(classe)) {
                        return cast.castSql(model, field, classe, cast.arrayCast(model, stack, field, classe, value));
                    }
                }

            } else {
                classe = CastUtil.primitiveToObject(classe);
                for (Cast cast : CASTS) {

                    if (cast.type(model, field, classe, value).isAssignableFrom(classe)) {
                        return cast.castSql(model, field, classe, cast.cast(model, stack, field, classe, value));
                    }
                }
            }
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return null;
    }

    public static void set(Model model, List<Model> stack, Field field, JsonElement value) throws IllegalArgumentException, IllegalAccessException {
        Object ob = getField(model, stack, field, value);
        field.set(model, ob);
    }

    public static Object getField(Model model, List<Model> stack, Field field, JsonElement value) {
        try {
            Class classe = field.getType();
            if (classe.isArray()) {
                classe = classe.getComponentType();
                classe = CastUtil.primitiveToObject(classe);
                for (Cast cast : CASTS) {
                    if (cast.type(model, field, classe, value).isAssignableFrom(classe)) {
                        return cast.arrayCast(model, stack, field, classe, value);
                    }
                }
            } else {
                classe = CastUtil.primitiveToObject(classe);

                for (Cast cast : CASTS) {
                    if (cast.type(model, field, classe, value).isAssignableFrom(classe)) {
                        return cast.cast(model, stack, field, classe, value);
                    }
                }
            }
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return null;
    }

    public static Object getQuery(Model model, Field field) {
        try {
            Class classe = field.getType();
            Object value = ModelUtil.getObject(model, field);
            if (classe.isArray()) {
                classe = classe.getComponentType();
                classe = CastUtil.primitiveToObject(classe);
                for (Cast cast : CASTS) {
                    if (cast.type(model, field, classe, value).isAssignableFrom(classe)) {
                        return cast.castSql(model, field, classe, value);
                    }
                }
            } else {
                classe = CastUtil.primitiveToObject(classe);
                for (Cast cast : CASTS) {
                    if (cast.type(model, field, classe, value).isAssignableFrom(classe)) {
                        return cast.castSql(model, field, classe, value);
                    }
                }
            }
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return null;
    }

    public static <T extends Model<T>> JsonObject toJson(Model<T> model, boolean force) {
        JsonObject json = new JsonObject();
        Field[] campos = getAllColumns(model.getClass());
        List<Model> stack = new ArrayList();
        stack.add(model);
        for (Field campo : campos) {
            try {
                Column col = campo.getAnnotation(Column.class);
                String coluna = col.name().isEmpty() ? col.value() : col.name();
                if (force || col.json()) {
                    Object valor = campo.get(model);
                    Class type = campo.getType();

                    if (type.isArray()) {
                        type = type.getComponentType();
                        type = CastUtil.primitiveToObject(type);
                        for (Cast cast : CASTS) {
                            if (cast.type(model, campo, type, valor).isAssignableFrom(type)) {

                                JsonElement value = cast.arrayJson(model, campo, type, (Object[]) valor);
                                json.add(coluna, value == null ? JsonNull.INSTANCE : value);
                                break;
                            }
                        }
                    } else {
                        type = CastUtil.primitiveToObject(type);
                        for (Cast cast : CASTS) {
                            if (cast.type(model, campo, type, valor).isAssignableFrom(type)) {

                                JsonElement value = cast.json(model, campo, type, valor);
                                json.add(coluna, value == null ? JsonNull.INSTANCE : value);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.printError(e);
            }
        }

        return json;
    }

    public static void set(QueryModel query, Model model, Field field, Column col, Object value) throws Exception {
        String coluna = col.value();
        for (Cast cast : CASTS) {
            Class classe = CastUtil.primitiveToObject(field.getType());
            if (cast.type(model, field, classe, value).isAssignableFrom(classe)) {
                if (classe.isArray()) {
                    classe = CastUtil.primitiveToObject(classe.getComponentType());
                    query.set(coluna, cast.castSql(model, field, classe, value));
                } else {
                    classe = CastUtil.primitiveToObject(classe);
                    query.set(coluna, cast.castSql(model, field, classe, value));
                }
                break;
            }
        }
    }

    public static boolean insert(Model model) {
        try {
            QueryModel query = model.query();
            model.onCreate();
            if (model.getAction() != null) {
                model.getAction().onCreate(model);
            }
            Field[] campos = getNormalColumns(model.getClass());

            for (Field campo : campos) {
                Column column = campo.getAnnotation(Column.class);
                if (column.insert()) {
                    set(query, model, campo, column, campo.get(model));
                }
            }
            campos = getPrimaryColumns(model.getClass());
            for (Field campo : campos) {
                Column column = campo.getAnnotation(Column.class);
                Object value = campo.get(model);
                if (column.insert() && value != null) {
                    set(query, model, campo, column, value);
                }
            }
            ResultSet res = query.tryExecuteInsert();
            if (res.next()) {
                model.fill(res);
            }
            model.afterCreate();
            if (model.getAction() != null) {
                model.getAction().afterCreate(model);
            }
            return true;
        } catch (Exception e) {
            Log.printError(e);
        }
        return false;
    }

    public static boolean update(Model model) {
        try {
            QueryModel query = model.query();
            model.onUpdate();
            if (model.getAction() != null) {
                model.getAction().onUpdate(model);
            }
            Field[] campos = model.getNormalColumns();
            for (Field campo : campos) {
                Column column = campo.getAnnotation(Column.class);
                if (column.update()) {
                    set(query, model, campo, column, campo.get(model));
                }
            }
            campos = model.getPrimaryColumns();
            for (Field campo : campos) {
                Column column = campo.getAnnotation(Column.class);
                Object value = campo.get(model);
                if (column.update() && value != null) {
                    set(query, model, campo, column, campo.get(model));
                }
                query.where(column.value(), campo.get(model));
            }
            ResultSet res = query.tryExecuteUpdate();
            if (res != null && res.next()) {
                model.fill(res);
                model.afterUpdate();
                if (model.getAction() != null) {
                    model.getAction().afterUpdate(model);
                }
                return true;
            }
        } catch (Exception e) {
            Log.printError(e);
        }
        return false;
    }

    public static void fill(Field field, Model model, List<Model> stack, Object value) {
        try {
            Class<?> clasee = field.getType();
            if (clasee.isArray()) {
                clasee = clasee.getComponentType();
                clasee = CastUtil.primitiveToObject(clasee);
                for (Cast cast : CASTS) {
                    if (cast.type(model, field, clasee, value).isAssignableFrom(clasee)) {
                        field.set(model, cast.arrayCast(model, stack, field, clasee, (Object[]) value));
                        break;
                    }
                }
            } else {
                boolean primitive = clasee.isPrimitive();
                clasee = CastUtil.primitiveToObject(clasee);
                for (Cast cast : CASTS) {
                    if (cast.type(model, field, clasee, value).isAssignableFrom(clasee)) {
                        Object val = cast.cast(model, stack, field, clasee, value);
                        if (primitive) {
                            if (val != null) {
                                field.set(model, val);
                            }
                        } else {
                            field.set(model, val);
                        }

                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Log.printError(ex);
        }
    }

    public static void fill(Model model, ResultSet res, List<Model> stack) throws SQLException {

        ResultSetMetaData resultSetMetaData = res.getMetaData();
        Field[] campos = model.getAllColumns(false, false, true, false);

        if (resultSetMetaData.getColumnCount() == 1 && model.getDatabase().getJdbc().equals(SQLite.JDBC) && resultSetMetaData.getColumnLabel(1).equals("last_insert_rowid()")) {
            for (Field campo : campos) {
                String nome = campo.getAnnotation(Column.class).value();
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
        for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
            Field campo = campos[col - 1];
            Object val = res.getObject(col);
            fill(campo, model, stack, val);
        }

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
}
