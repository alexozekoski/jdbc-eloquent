/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.validation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.alexozekoski.database.Database;
import github.alexozekoski.database.model.Column;
import github.alexozekoski.database.model.ModelSerial;
import github.alexozekoski.database.model.Table;
import github.alexozekoski.database.query.Query;
import java.lang.reflect.Field;

/**
 *
 * @author alexo
 */
public class Unique implements CustomValidation<Object, Object> {

    private Database database;

    public Unique(Database database) {
        this.database = database;
    }

    @Override
    public boolean validate(Object object, Object value, Field field) {
        Column column = field.getAnnotation(Column.class);
        Table table = object.getClass().getAnnotation(Table.class);
        if (column != null && table != null && database != null) {
            Long count;
            if (object instanceof ModelSerial) {
                Query query = database.query().table(table.value()).select(column.value()).where(column.value(), value);
                Long id = ((ModelSerial) object).id;
                if (id != null) {
                    query = query.where("id", "!=", id);
                }
                count = query.count();
            } else {
                count = database.query().table(table.value()).select(column.value()).where(column.value(), value).count();
            }

            if (count > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public JsonElement message(Object object, Object value, Field field) {
        return new JsonPrimitive("O valor já existe");
    }

    @Override
    public int code(Object object, Object value, Field field) {
        return 2;
    }

}
