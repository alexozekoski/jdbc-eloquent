/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.validation;

import com.google.gson.JsonElement;
import java.lang.reflect.Field;

/**
 *
 * @author alexo
 * @param <O>
 * @param <V>
 */
public interface CustomValidation<O, V> {

    public boolean validate(O object, V value, Field field) throws Exception;

    public JsonElement message(O object, V value, Field field) throws Exception;

    public int code(O object, V value, Field field) throws Exception;

}
