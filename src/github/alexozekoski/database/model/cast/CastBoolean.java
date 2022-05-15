/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model.cast;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.model.Model;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * @author alexo
 */
public class CastBoolean extends CastPrimitive<Boolean> {

    public CastBoolean() {
        super(Boolean.class);
    }

    @Override
    public Boolean cast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlvalue) throws Exception {
        if (sqlvalue == null) {
            return null;
        }
        if (Boolean.class.isInstance(sqlvalue)) {
            return (Boolean) sqlvalue;
        }
        if (Number.class.isInstance(sqlvalue)) {
            return ((Number) sqlvalue).longValue() == 1;
        }
        if (String.class.isInstance(sqlvalue)) {
            return Boolean.parseBoolean((java.lang.String) sqlvalue);
        }
        return null;
    }

    @Override
    public JsonElement json(Model model, Field field, Class fieldType, Boolean obValue) throws Exception {
        return obValue == null ? JsonNull.INSTANCE : new JsonPrimitive(obValue);
    }

    @Override
    public Boolean cast(Model model, List<Model> stack, Field field, Class fieldType, JsonElement value) throws Exception {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsBoolean();
    }

    @Override
    public String dataType(Field field, Class fieldType, Database database) throws Exception {
        return fieldType.isArray() ? super.dataType(field, fieldType, database) : database.getMigrationType().booleano();
    }

}
