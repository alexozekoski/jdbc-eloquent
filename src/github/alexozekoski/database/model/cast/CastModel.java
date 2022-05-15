/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model.cast;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.model.Column;
import github.alexozekoski.database.model.Model;
import github.alexozekoski.database.model.ModelUtil;
import github.alexozekoski.database.model.Serial;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * @author alexo
 */
public class CastModel extends CastPrimitive<Model> {

    public CastModel() {
        super(Model.class);
    }

    @Override
    public Model cast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlvalue) throws Exception {
        if (sqlvalue == null) {
            return null;
        }
        Column col = field.getAnnotation(Column.class);

        Model m;
        if (Serial.class.isAssignableFrom(fieldType) && ModelUtil.isForeign(col)) {
            Long id;
            if (Number.class.isInstance(sqlvalue)) {
                id = ((Number) sqlvalue).longValue();
            } else {
                id = Long.parseLong((String) sqlvalue);
            }
            m = findOrCreate(fieldType, id, stack);
        } else {
            m = ((Model) Model.newInstance(fieldType)).set(JsonParser.parseString((String) sqlvalue).getAsJsonObject());
            stack.add(m);
        }
        return m;
    }

    @Override
    public JsonElement json(Model model, Field field, Class fieldType, Model obValue) throws Exception {
        return obValue != null ? obValue.toJson() : JsonNull.INSTANCE;
    }

    @Override
    public Model cast(Model model, List<Model> stack, Field field, Class fieldType, JsonElement value) throws Exception {
        if (value == null || value.isJsonNull() || !value.isJsonObject() || !value.isJsonPrimitive()) {
            return null;
        }
        Model m;
        Column col = field.getAnnotation(Column.class);
        if (Serial.class.isAssignableFrom(fieldType) && ModelUtil.isForeign(col)) {

            Long id;
            if (value.isJsonObject()) {
                m = (Model) field.get(model);
                if (m != null) {
                    if (col.foreignUnique()) {
                        value.getAsJsonObject().remove("id");
                    }
                    m.set(value.getAsJsonObject());
                    stack.add(m);
                    return m;
                }
                JsonElement jsonid = value.getAsJsonObject().get("id");
                id = jsonid == null || jsonid.isJsonNull() ? null : jsonid.getAsLong();
            } else {
                id = value.getAsLong();
            }
            m = findOrCreate(fieldType, id, stack);
            if (value.isJsonObject() && (id == null || col.foreignFill())) {
                m.set(value.getAsJsonObject());
            }
        } else {
            m = findOrCreate(fieldType, null, stack);
        }
        return m;
    }

    @Override
    public String dataType(Field field, Class fieldType, Database database) throws Exception {
        if (Serial.class.isAssignableFrom(fieldType) && ModelUtil.isForeign(field.getAnnotation(Column.class))) {
            return database.getMigrationType().bigint();
        } else {
            return database.getMigrationType().text();
        }
    }

    @Override
    public Object castSql(Model model, Field field, Class fieldType, Model obValue) throws Exception {
        if (obValue == null) {
            return null;
        }
        Column column = field.getAnnotation(Column.class);
        if (Serial.class.isAssignableFrom(fieldType)) {
            Long id = ((Serial) obValue).getId();
            if (id == null) {
                if (column.foreignInsert()) {
                    obValue.insert();
                    return ((Serial) obValue).getId();
                }
            } else {
                if (column.foreignUpdate()) {
                    obValue.save();
                    return ((Serial) obValue).getId();
                }
            }
        }
        return Serial.class.isAssignableFrom(fieldType) && ModelUtil.isForeign(column) ? ((Serial) obValue).getId() : obValue.toJsonString(true);
    }

    public static Model findOrCreate(Class<? extends Serial> classe, Long id, List<Model> list) throws Exception {
        Model model = null;
        if (id != null) {
            model = find(classe, id, list);
            if (model == null) {
                model = ((Serial) Model.newInstance((Class<? extends Model>) classe)).get(id);
                list.add(model);
            }
        }

        if (model == null) {
            model = Model.newInstance((Class<? extends Model>) classe);
            list.add(model);
        }
        return model;
    }

    public static Model find(Class<? extends Serial> classe, Long id, List<Model> list) {
        for (Model model : list) {
            if (classe.isInstance(model) && ((Serial) model).equals(id)) {
                return model;
            }
        }
        return null;
    }

}
