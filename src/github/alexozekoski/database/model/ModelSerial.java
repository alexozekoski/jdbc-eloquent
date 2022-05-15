/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database.model;

import com.google.gson.JsonObject;
import github.alexozekoski.database.Database;
import java.sql.Timestamp;
import java.util.Objects;

/**
 *
 * @author alexo
 * @param <T>
 */
public class ModelSerial<T extends ModelSerial<T>> extends Model<T> implements Serial<T> {

    @Column(primary = true, value = "id", serial = true)
    public Long id;

    @Column(value = "created", fill = false)
    public Timestamp created;

    @Column(value = "updated", fill = false)
    public Timestamp updated;

    public ModelSerial() {
    }

    public ModelSerial(Database database) {
        super(database);
    }

//    public ModelSerial(Object... values) {
//        super(values);
//    }
    public ModelSerial(JsonObject values) {
        super(values);
    }

    @Override
    public void onCreate() {
        created = new Timestamp(new java.util.Date().getTime());
        updated = new Timestamp(new java.util.Date().getTime());
    }

    @Override
    public void onUpdate() {
        updated = new Timestamp(new java.util.Date().getTime());
    }

    @Override
    public boolean update() {
        if (id == null) {
            return false;
        }
        return super.update();
    }

    @Override
    public T get(Long id) {
        return query().where("id", id).first();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Long id) {
        return Objects.equals(this.id, id);
    }
}
