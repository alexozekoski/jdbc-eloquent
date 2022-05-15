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
public class CastByte extends CastPrimitive<Byte> {

    public CastByte() {
        super(Byte.class);
    }

    @Override
    public Byte cast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlvalue) throws Exception {
        if (sqlvalue == null) {
            return null;
        }
        if (Number.class.isInstance(sqlvalue)) {
            return ((Number) sqlvalue).byteValue();
        }
        if (String.class.isInstance(sqlvalue)) {
            return Byte.parseByte((String) sqlvalue);
        }
        return null;
    }

    @Override
    public JsonElement json(Model model, Field field, Class fieldType, Byte obValue) throws Exception {
        return obValue == null ? JsonNull.INSTANCE : new JsonPrimitive(obValue);
    }

    @Override
    public Byte cast(Model model, List<Model> stack, Field field, Class fieldType, JsonElement value) throws Exception {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsByte();
    }

    @Override
    public String dataType(Field field, Class fieldType, Database database) throws Exception {
        return fieldType.isArray() ? super.dataType(field, fieldType, database) : database.getMigrationType().smallint();
    }

}
