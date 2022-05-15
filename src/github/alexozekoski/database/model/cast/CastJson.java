/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model.cast;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import github.alexozekoski.database.model.Model;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * @author alexo
 */
public class CastJson extends CastPrimitive<JsonElement> {

    public CastJson() {
        super(JsonElement.class);
    }

    @Override
    public JsonElement cast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlvalue) throws Exception {
        if (sqlvalue != null && String.class.isInstance(sqlvalue)) {
            return JsonParser.parseString((java.lang.String) sqlvalue);
        }
        return null;
    }

    @Override
    public Object castSql(Model model, Field field, Class fieldType, JsonElement obValue) throws Exception {
        return obValue != null ? obValue.toString() : null;
    }

    @Override
    public JsonElement json(Model model, Field field, Class fieldType, JsonElement obValue) throws Exception {
        return obValue != null ? obValue : JsonNull.INSTANCE;
    }

}
