/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model.cast;

import com.google.gson.JsonElement;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.model.Model;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 *
 * @author alexo
 */
public class CastDateUtil extends CastPrimitive {

    @Override
    public Object sqlToField(Model model, List<Model> stack, Field field, Class fieldType, Object sqlvalue) throws Exception {
        return CastUtil.toDate(sqlvalue);
    }

    @Override
    public Object fieldToSql(Model model, Field field, Class fieldType, Object obValue, boolean where) throws Exception {
        return new java.sql.Date(((Date) obValue).getTime());
    }

    @Override
    public JsonElement fieldToJson(Model model, Field field, Class fieldType, Object obValue) throws Exception {
        return CastUtil.toJson((Date) obValue);
    }

    @Override
    public Object jsonToField(Model model, List<Model> stack, Field field, Class fieldType, JsonElement value) throws Exception {
        return CastUtil.jsonDateUtil(value);
    }

    @Override
    public String dataType(Field field, Class fieldType, Database database) throws Exception {
        return arrayOrList(field, database.getMigrationType().datetime(), database);
    }

}
