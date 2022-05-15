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
import java.util.List;

/**
 *
 * @author alexo
 * @param <T>
 */
public interface Cast<T> {

    public T cast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlvalue) throws Exception;

    public Object castSql(Model model, Field field, Class fieldType, T obValue) throws Exception;

    public Object castSql(Model model, Field field, Class fieldType, T[] obValue) throws Exception;

    public Class type(Model model, Field field, Class fieldType, Object obValue) throws Exception;

    public JsonElement json(Model model, Field field, Class fieldType, T obValue) throws Exception;

    public JsonElement arrayJson(Model model, Field field, Class fieldType, T[] values) throws Exception;

    public T cast(Model model, List<Model> stack, Field field, Class fieldType, JsonElement value) throws Exception;

    public String dataType(Field field, Class fieldType, Database database) throws Exception;

    public T[] arrayCast(Model model, List<Model> stack, Field field, Class fieldType, Object sqlValue) throws Exception;

    public T[] arrayCast(Model model, List<Model> stack, Field field, Class fieldType, JsonElement values) throws Exception;

//    public void onCreate(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception;
//
//    public void onUpdate(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception;
//
//    public void onDelete(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception;
//
//    public void onSelect(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception;
//
//    public void afterSelect(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception;
//
//    public void afterUpdate(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception;
//
//    public void afterCreate(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception;
//
//    public void afterDelete(Model model, List<Model> stack, Field field, Class fieldType, T obValue) throws Exception;
//
//    public void onCreate(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception;
//
//    public void onUpdate(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception;
//
//    public void onDelete(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception;
//
//    public void onSelect(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception;
//
//    public void afterSelect(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception;
//
//    public void afterUpdate(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception;
//
//    public void afterCreate(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception;
//
//    public void afterDelete(Model model, List<Model> stack, Field field, Class fieldType, T[] obValue) throws Exception;;
}
