/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database;

import com.google.gson.JsonObject;
import github.alexozekoski.database.migration.SQLiteMigration;
import github.alexozekoski.database.migration.MigrationType;
import java.io.File;

/**
 *
 * @author alexozekoski
 */
public class SQLite extends Database {

    public static final String JDBC = "sqlite";

    public static final MigrationType MIGRATION_TYPE = new SQLiteMigration();

    public SQLite(JsonObject json) {
        super(json);
        setJdbc(JDBC);
    }

    public SQLite(String database) {
        super(JDBC, null, null, null, null, database);
    }
    public SQLite(File file) {
        super(JDBC, null, null, null, null, file.getAbsolutePath());
    }

    public SQLite(String host, Integer port, String user, String password, String database) {
        super(JDBC, host, port, user, password, database);
    }

    public SQLite() {
        setJdbc(JDBC);
    }

//    @Override
//    public int tryExecuteUpdate(QueryBuild query) throws SQLException {
//        return tryExecuteUpdate(query.toString());
//    }
//    
    @Override
    public MigrationType getMigrationType() {
        return MIGRATION_TYPE; 
    }
}
