/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author alexozekoski Class to create an conection to FirebaseSQL
 */
public class FirebirdSQL extends Database {

    public static final String JDBC = "firebirdsql";

    public FirebirdSQL(JsonObject json) {
        super(json);
        setJdbc(JDBC);
    }

    public FirebirdSQL(String host, Integer port, String user, String password, String database) {
        super(JDBC, host, port, user, password, database);
    }

    public FirebirdSQL() {
        setJdbc(JDBC);
    }
    
    public FirebirdSQL(String database) {
        super(JDBC, null, null, null, null, database);
    }

    public JsonArray listOfTables() {
        JsonArray array = new JsonArray();
        try {
            array = executeAsJsonArray("SELECT trim(RDB$RELATION_NAME) as \"nome\" FROM RDB$RELATIONS WHERE (RDB$SYSTEM_FLAG <> 1 OR RDB$SYSTEM_FLAG IS NULL) AND RDB$VIEW_BLR IS NULL ORDER BY RDB$RELATION_NAME;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }

    public JsonArray listOfColumns(String table) {
        JsonArray array = new JsonArray();
        try {
            array = executeAsJsonArray("pragma table_info('" + table + "')");
            JsonArray novaLista = new JsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonObject velho = array.get(i).getAsJsonObject();
                JsonObject novo = new JsonObject();
                novo.add("nome", velho.get("name"));
                novo.add("tipo", velho.get("type"));
                novaLista.add(novo);
            }
            array = novaLista;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }

}
