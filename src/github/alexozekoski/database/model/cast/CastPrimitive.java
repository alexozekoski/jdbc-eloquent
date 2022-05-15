/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model.cast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.Log;
import github.alexozekoski.database.model.Model;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * @author alexo
 */
public class CastPrimitive<T> implements Cast<T> {

    private Class classe;

    public CastPrimitive(Class classe) {
        this.classe = classe;
    }

    @Override
    public T cast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlvalue) throws Exception {
        return (T) sqlvalue;
    }

    @Override
    public Object castSql(Model model, Field field, Class fieldType, T obValue) throws Exception {
        return obValue;
    }

    @Override
    public Class type(Model model, Field field, Class fieldType, Object obValue) throws Exception {
        return classe;
    }

    @Override
    public JsonElement json(Model model, Field field, Class fieldType, T obValue) throws Exception {
        return null;
    }

    @Override
    public T cast(Model model, List<Model> stack, Field field, Class fieldType, JsonElement value) throws Exception {
        return null;
    }

    @Override
    public String dataType(Field field, Class fieldType, Database database) throws Exception {
        return database.getMigrationType().text();
    }

    @Override
    public JsonElement arrayJson(Model model, Field field, Class fieldType, T[] values) throws Exception {
        if (values == null) {
            return null;
        }

        JsonArray array = new JsonArray(values.length);
        for (T value : values) {
            array.add(json(model, field, fieldType, value));
        }
        return array;
    }

    @Override
    public T[] arrayCast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlValue) throws Exception {
        if (String.class.isInstance(sqlValue)) {
            try {
                JsonElement json = JsonParser.parseString((String) sqlValue);
                return arrayCast(model, stack, field, fieldType, json);
            } catch (Exception ex) {
                Log.printWarning(ex);
            }
        }
        T[] array = (T[]) Array.newInstance(classe, 1);
        array[0] = cast(model, stack, field, fieldType, sqlValue);
        return array;

    }

    @Override
    public T[] arrayCast(Model model, List<Model> stack, Field field, Class fieldType, JsonElement values) throws Exception {
        if (values.isJsonNull()) {
            return null;
        }
        if (values.isJsonArray()) {
            JsonArray array = values.getAsJsonArray();
            T[] obs = (T[]) Array.newInstance(classe, 1);
            for (int i = 0; i < array.size(); i++) {
                JsonElement el = array.get(i);
                obs[i] = cast(model, stack, field, fieldType, el);
            }
            return obs;
        }
        T[] array = (T[]) Array.newInstance(classe, 1);
        array[0] = cast(model, stack, field, fieldType, values);
        return array;
    }

    @Override
    public Object castSql(Model model, Field field, Class fieldType, T[] obValue) throws Exception {
        return arrayJson(model, field, fieldType, obValue).toString();
    }

//    @Override
//    public void onCreate(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void onUpdate(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void onDelete(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void onSelect(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void afterSelect(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void afterUpdate(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void afterCreate(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void afterDelete(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void onCreate(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void onUpdate(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void onDelete(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void onSelect(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void afterSelect(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void afterUpdate(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void afterCreate(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception {
//        
//    }
//
//    @Override
//    public void afterDelete(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception {
//        
//    };

}
