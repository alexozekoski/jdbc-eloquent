/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.validation;

import com.google.gson.JsonObject;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.model.ModelSerial;
import java.lang.reflect.Field;

/**
 *
 * @author alexo
 */
public class Validator {

    public static JsonObject validField(Validation validation, Field field, Object model, Object value, Database database) throws IllegalArgumentException, IllegalAccessException, InstantiationException, Exception {
        JsonObject json = new JsonObject();
        if (validation.required()) {
            CustomValidation val = new Required();
            if (!val.validate(model, value, field)) {
                json.add(Integer.toString(val.code(model, value, field)), val.message(model, value, field));
            }
        }
        if (!validation.exist().equals(ModelSerial.class)) {
            CustomValidation val = new Exist(database, validation.exist());
            if (!val.validate(model, value, field)) {
                json.add(Integer.toString(val.code(model, value, field)), val.message(model, value, field));
            }
        }
        if (validation.unique()) {
            CustomValidation val = new Unique(database);
            if (!val.validate(model, value, field)) {
                json.add(Integer.toString(val.code(model, value, field)), val.message(model, value, field));
            }
        }
        if (!Double.isNaN(validation.min())) {
            CustomValidation val = new Min(validation.min());
            if (!val.validate(model, value, field)) {
                json.add(Integer.toString(val.code(model, value, field)), val.message(model, value, field));
            }
        }
        if (!Double.isNaN(validation.max())) {
            CustomValidation val = new Max(validation.max());
            if (!val.validate(model, value, field)) {
                json.add(Integer.toString(val.code(model, value, field)), val.message(model, value, field));
            }
        }
        for (Class<? extends CustomValidation> ob : validation.custom()) {
            CustomValidation custom = ob.newInstance();
            if (!custom.validate(model, value, field)) {
                json.add(Integer.toString(custom.code(model, value, field)), custom.message(model, value, field));
            }
        }
        return json.keySet().size() > 0 ? json : null;
    }
}
