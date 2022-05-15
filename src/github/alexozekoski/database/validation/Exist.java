/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.validation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.model.Column;
import github.alexozekoski.database.model.ModelSerial;
import github.alexozekoski.database.model.Serial;
import github.alexozekoski.database.model.Table;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alexo
 */
public class Exist implements CustomValidation<Object, Object> {

    private Database database;

    private Class<? extends ModelSerial> model;

    public Exist(Database database, Class<? extends ModelSerial> model) {
        this.database = database;
        this.model = model;
    }

    @Override
    public boolean validate(Object object, Object value, Field field) {
        Table table = model.getAnnotation(Table.class);

        if (table != null && database != null) {
            if (object.getClass().isArray()) {
                List<Long> ids = new ArrayList();
                for (int i = 0; i < Array.getLength(value); i++) {
                    Object ob = Array.get(value, i);
                    if (ob != null) {
                        if (Serial.class.isInstance(ob)) {
                            ids.add(((Serial) ob).getId());
                        } else {
                            ids.add((Long) ob);
                        }
                    }
                }

                if (database.query().table(table.value()).select("id").whereIn("id", ids).count() > 0) {
                    return false;
                }
                return true;
            }
            if (object instanceof List) {
                return true;
            }
            ;
            if (database.query().table(table.value()).select("id").where("id", value).count() > 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public JsonElement message(Object object, Object value, Field field) {
        return new JsonPrimitive("O valor não existe");
    }

    @Override
    public int code(Object object, Object value, Field field) {
        return 5;
    }

}
