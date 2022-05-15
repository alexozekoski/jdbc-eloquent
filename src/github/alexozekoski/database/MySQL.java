/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database;

import com.google.gson.JsonObject;

/**
 *
 * @author alexozekoski Class to create an conection to MySQL
 */
public class MySQL extends Database {

    public static final String JDBC = "mysql";

    public MySQL(JsonObject json) {
        super(json);
        setJdbc(JDBC);
    }

    public MySQL(String host, Integer port, String user, String password, String database) {
        super(JDBC, host, port, user, password, database);
    }

    public MySQL() {
        setJdbc(JDBC);
    }
}
