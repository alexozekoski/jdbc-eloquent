/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model.cast;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;

/**
 *
 * @author alexo
 */
public class CastUtil {

    public static DateFormat DATE_JSON = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static DateFormat DATE_SQL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static JsonElement sqlToJson(Object sqlObject) {
        if(sqlObject == null){
           return JsonNull.INSTANCE; 
        }
        if(Number.class.isInstance(sqlObject)){
            return new JsonPrimitive((Number)sqlObject);
        }
        if(Boolean.class.isInstance(sqlObject)){
            return new JsonPrimitive((Number)sqlObject);
        }
        return new JsonPrimitive(sqlObject.toString());
        
    }

    public static Date timeToDateUtil(long time) {
        time -= TimeZone.getDefault().getOffset(time);
        return new Date(time);
    }

    public static java.sql.Date timeToDate(long time) {
        time -= TimeZone.getDefault().getOffset(time);
        return new java.sql.Date(time);
    }

    public static Date stringToDateUtil(String jsonDate) throws ParseException {
        return DATE_JSON.parse(jsonDate);
    }

    public static Date jsonDateUtil(JsonElement json) throws ParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        if (json.getAsJsonPrimitive().isNumber()) {
            return new Date(json.getAsLong());
        }
        return stringToDateUtil(json.getAsString());
    }

    public static java.sql.Date jsonDate(JsonElement json) throws ParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        if (json.getAsJsonPrimitive().isNumber()) {
            return new java.sql.Date(json.getAsLong());
        }
        return new java.sql.Date(stringToDateUtil(json.getAsString()).getTime());
    }

    public static Timestamp jsonTimestamp(JsonElement json) throws ParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        if (json.getAsJsonPrimitive().isNumber()) {
            return new Timestamp(json.getAsLong());
        }
        return new Timestamp(stringToDateUtil(json.getAsString()).getTime());
    }

    public static Time jsonTime(JsonElement json) throws ParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        if (json.getAsJsonPrimitive().isNumber()) {
            return new Time(json.getAsLong());
        }
        return new Time(stringToDateUtil(json.getAsString()).getTime());
    }

    public static JsonElement toJson(Date date) {
        if (date == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(DATE_JSON.format(date));
    }

    public static Date toDateUtil(Object sqlvalue) throws ParseException {
        if (sqlvalue == null) {
            return null;
        }
        if (Date.class.isInstance(sqlvalue)) {
            return (Date) sqlvalue;
        }
        if (String.class.isInstance(sqlvalue)) {
            return DATE_SQL.parse((String) sqlvalue);
        }
        if (Number.class.isInstance(sqlvalue)) {
            return new Date((Long) sqlvalue);
        }
        return null;
    }

    public static java.sql.Date toDate(Object sqlvalue) throws ParseException {
        if (sqlvalue == null) {
            return null;
        }
        if (java.sql.Date.class.isInstance(sqlvalue)) {
            return (java.sql.Date) sqlvalue;
        }
        if (Date.class.isInstance(sqlvalue)) {
            return new java.sql.Date(((Date) sqlvalue).getTime());
        }
        if (String.class.isInstance(sqlvalue)) {
            return new java.sql.Date(DATE_SQL.parse((String) sqlvalue).getTime());
        }
        if (Number.class.isInstance(sqlvalue)) {
            return new java.sql.Date((Long) sqlvalue);
        }
        return null;
    }

    public static Timestamp toTimestamp(Object sqlvalue) throws ParseException {
        if (sqlvalue == null) {
            return null;
        }
        if (Timestamp.class.isInstance(sqlvalue)) {
            return (Timestamp) sqlvalue;
        }
        if (String.class.isInstance(sqlvalue)) {
            return new Timestamp(DATE_SQL.parse((String) sqlvalue).getTime());
        }
        if (Number.class.isInstance(sqlvalue)) {
            return new Timestamp((Long) sqlvalue);
        }
        if (Date.class.isInstance(sqlvalue)) {
            return new Timestamp(((Date) sqlvalue).getTime());
        }
        return null;
    }

    public static Time toTime(Object sqlvalue) throws ParseException {
        if (sqlvalue == null) {
            return null;
        }
        if (Time.class.isInstance(sqlvalue)) {
            return (Time) sqlvalue;
        }
        if (Date.class.isInstance(sqlvalue)) {
            return new Time(((Date) sqlvalue).getTime());
        }
        if (String.class.isInstance(sqlvalue)) {
            return new Time(DATE_SQL.parse((String) sqlvalue).getTime());
        }
        if (Number.class.isInstance(sqlvalue)) {
            return new Time((Long) sqlvalue);
        }
        return null;
    }

    public static Class primitiveToObject(Class classe) {
        if (classe.isPrimitive()) {
            if (classe == long.class) {
                return Long.class;
            }
            if (classe == int.class) {
                return Integer.class;
            }
            if (classe == boolean.class) {
                return Boolean.class;
            }
            if (classe == double.class) {
                return Double.class;
            }
            if (classe == float.class) {
                return Float.class;
            }
            if (classe == byte.class) {
                return Byte.class;
            }
            if (classe == char.class) {
                return Character.class;
            }
            if (classe == short.class) {
                return Short.class;
            }
        }
        return classe;
    }
}
