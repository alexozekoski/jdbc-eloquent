/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.validation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Field;

/**
 *
 * @author alexo
 */
public class Required implements CustomValidation<Object, Object> {

    @Override
    public boolean validate(Object object, Object value, Field field) {
        return value != null;
    }

    @Override
    public JsonElement message(Object object, Object value, Field field) {
        return new JsonPrimitive("Valor requerido");
    }

    @Override
    public int code(Object object, Object value, Field field) {
        return 1;
    }

}
