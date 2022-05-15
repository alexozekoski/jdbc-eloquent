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
public class CastDateUtil extends CastPrimitive<Date> {

    public CastDateUtil() {
        super(Date.class);
    }

    @Override
    public Date cast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlvalue) throws Exception {
        return CastUtil.toDate(sqlvalue);
    }

    @Override
    public Object castSql(Model model, Field field, Class fieldType, Date obValue) throws Exception {
        return new java.sql.Date(obValue.getTime());
    }

    @Override
    public JsonElement json(Model model, Field field, Class fieldType, Date obValue) throws Exception {
        return CastUtil.toJson(obValue);
    }

    @Override
    public Date cast(Model model, List<Model> stack, Field field, Class fieldType, JsonElement value) throws Exception {
        return CastUtil.jsonDateUtil(value);
    }

    @Override
    public String dataType(Field field, Class fieldType, Database database) throws Exception {
        return fieldType.isArray() ? super.dataType(field, fieldType, database) : database.getMigrationType().datetime();
    }

}
