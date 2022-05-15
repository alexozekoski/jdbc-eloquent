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
public class Min implements CustomValidation<Object, Object> {

    private double value;

    public Min(double value) {
        this.value = value;
    }

    @Override
    public boolean validate(Object object, Object value, Field field) {
        if (value != null) {
            if (value instanceof String) {
                return ((String) value).length() >= this.value;
            }
            if (value instanceof Integer) {
                return (Integer) value >= this.value;
            }
            if (value instanceof Double) {
                return (Double) value >= this.value;
            }
            if (value instanceof Long) {
                return (Long) value >= this.value;
            }
            if (value instanceof Byte) {
                return (Byte) value >= this.value;
            }
            if (value instanceof Short) {
                return (Short) value >= this.value;
            }
            if (value instanceof Float) {
                return (Float) value >= this.value;
            }
        }
        return true;
    }

    @Override
    public JsonElement message(Object object, Object value, Field field) {
        String v = "Valor min " + this.value;
        if (v.endsWith(".0")) {
            v = v.replace(".0", "");
        }
        return new JsonPrimitive(v);
    }

    @Override
    public int code(Object object, Object value, Field field) {
        return 3;
    }

}
