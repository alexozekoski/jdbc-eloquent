/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.alexozekoski.database.model.Model;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.model.ModelList;
import github.alexozekoski.database.model.ModelUtil;
import java.lang.reflect.Field;
import java.util.List;
import github.alexozekoski.database.Log;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author alexo
 * @param <M>
 * @param <L>
 */
public class QueryModel<M extends Model<M>> extends Query<QueryModel<M>> {

    private Class<? extends Model<M>> classe;

    private Database database;

    public QueryModel(Class<? extends Model<M>> classe, Database database) {
        super(database, ModelUtil.getTable(classe));
        this.database = database;
        setTable(ModelUtil.getTable(classe));
        setClasse(classe);
    }

    public ModelList<M> get() {
        return get(new ModelList(classe));
    }

    public ModelList<M> get(ModelList<M> list) {

        int pos = 0;
        ResultSet resultado = null;
        try {
            resultado = tryExecuteSelect();
        } catch (Exception e) {
            Log.printError(e);
        }
        try {
            while (resultado.next()) {
                boolean novo = !(list.size() < pos);
                M model = novo ? (M) Model.newInstance(classe) : list.get(pos);
                model.setDatabase(getDatabase());
                model.onSelect();
                if (model.getAction() != null) {
                    model.getAction().onSelect(model);
                }
                model.setDatabase(getDatabase());
                model.fill(resultado);
                model.afterSelect();
                if (model.getAction() != null) {
                    model.getAction().afterSelect(model);
                }
                if (novo) {
                    list.add(model);
                }
                pos++;
            }
        } catch (Exception e) {
            Log.printError(e);
        }
        try {
            resultado.close();
        } catch (Exception e) {
            Log.printError(e);
        }
        while (pos < list.size()) {
            list.remove(pos);
        }
        return list;
    }

    @Override
    public long tryCountDistinct(String column) throws SQLException {
        Field[] fields = ModelUtil.getAllColumns(classe);
        if (canSelectColumn(column, fields) != null) {
            return super.tryCountDistinct(column);
        }
        return -1;
    }

    public M first() {
        ModelList<M> list = limit(1).get();
        return list.isEmpty() ? null : list.get(0);
    }

    public Database getDatabase() {
        return database;
    }

    private Field canSelectColumn(String column, Field[] fields) {
        for (Field field : fields) {
            github.alexozekoski.database.model.Column c = field.getAnnotation(github.alexozekoski.database.model.Column.class);
            if (c.select()) {
                if (c.name().isEmpty()) {
                    if (!c.value().isEmpty() && c.value().equals(column)) {
                        return field;
                    }
                } else {
                    if (c.name().equals(column) || (!c.value().isEmpty() && c.value().equals(column))) {
                        return field;
                    }
                }
            }
        }
        return null;
    }

    public QueryModel<M> where(JsonObject json) throws IllegalArgumentException, IllegalAccessException {
        try {
            Field[] fields = ModelUtil.getAllColumns(classe);
            List<Model> stack = new ArrayList();
            for (String key : json.keySet()) {
                Field field = canSelectColumn(key, fields);
                if (field != null) {

                    github.alexozekoski.database.model.Column column = field.getAnnotation(github.alexozekoski.database.model.Column.class);
                    if (column != null) {
                        JsonElement js = json.get(key);
                        Object value;
                        String operator = "=";
                        boolean and = true;
                        if (js.isJsonObject()) {
                            value = ModelUtil.getField(null, stack, field, js.getAsJsonObject().get("value"));
                            JsonElement row = js.getAsJsonObject().get("row");
                            if (row != null && !row.isJsonNull()) {
                                if (row.getAsString().toLowerCase().equals("or")) {
                                    and = false;
                                }
                            }
                            JsonElement op = js.getAsJsonObject().get("operator");
                            if (op != null && !op.isJsonNull()) {
                                operator = op.getAsString();
                            }
                        } else {
                            value = ModelUtil.getQuery(null, stack, field, json.get(key));
                        }

                        if (value != null) {
                            if (and) {
                                where(column.value(), operator, value);
                            } else {
                                orWhere(column.value(), operator, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.printError(e);
        }
        return this;
    }

    public QueryModel<M> limit(JsonObject json) throws IllegalArgumentException, IllegalAccessException {
        try {
            if (json.has("offset")) {
                offset(json.get("offset").getAsLong());
            }
            if (json.has("limit")) {
                limit(json.get("limit").getAsLong());
            }
        } catch (Exception e) {
            Log.printError(e);
        }
        return this;
    }

    public QueryModel<M> orderBy(JsonObject json) throws IllegalArgumentException, IllegalAccessException {
        try {
            Field[] fields = ModelUtil.getAllColumns(classe);
            for (String k : json.keySet()) {
                JsonElement ele = json.get(k);
                if (ele.isJsonPrimitive()) {

                    String dir = ele.getAsString();
                    Field field = canSelectColumn(k, fields);

                    if (field != null) {
                        github.alexozekoski.database.model.Column column = field.getAnnotation(github.alexozekoski.database.model.Column.class);
                        orderBy(column.value(), dir.toLowerCase().equals("desc") ? "DESC" : "ASC");
                    }

                }
            }
        } catch (Exception e) {
            Log.printError(e);
        }
        return this;
    }

    public Class<? extends Model<M>> getClasse() {
        return classe;
    }

    public void setClasse(Class<? extends Model<M>> classe) {

        this.classe = classe;
        setDefaultColumns();
    }

    public void setDefaultColumns() {
        Field[] fields = ModelUtil.getAllColumns(classe, false, false, true, false);
        for (Field field : fields) {
            github.alexozekoski.database.model.Column column = field.getAnnotation(github.alexozekoski.database.model.Column.class);
            select(column.value());
        }
    }

}
