/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.alexozekoski.database.migration.PostgresSQLMigration;
import github.alexozekoski.database.migration.MigrationType;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

/**
 *
 * @author alexozekoski
 */
public class PostgreSQL extends Database {

    public static final String JDBC = "postgresql";

    public static final MigrationType MIGRATION_TYPE = new PostgresSQLMigration();

    public PostgreSQL(JsonObject json) {
        super(json);
        setJdbc(JDBC);
    }

    public PostgreSQL(String host, Integer port, String user, String password, String database) {
        super(JDBC, host, port, user, password, database);
    }

    public PostgreSQL() {
    }

//    @Override
//    public JsonArray tables() {
//        return executeAsJsonArray("SELECT table_name as \"name\" FROM information_schema.tables WHERE table_schema='public'");
//    }
//    @Override
//    public JsonArray columns(String tabela) {
//        return executeAsJsonArray("SELECT column_name as \"name\", data_type as \"type\" FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '" + tabela + "';");
//    }
    @Override
    public MigrationType getMigrationType() {
        return MIGRATION_TYPE;
    }

    public void tryUbuntuDump(File file) throws IOException {
        String user = getUser();
        String password = getPassword();
        String host = getHost();
        String port = Integer.toString(getPort());
        String database = getDatabase();
        if (file.exists()) {
            if (file.length() > 0) {
                file.delete();
            }

        }

        Process p = null;
        String linha = "";
        ProcessBuilder pb = new ProcessBuilder("pg_dump", "-h", host, "-U", database, "-p", port, "-F", "c", "-b", "-v", "-f", file.getCanonicalPath(), user);
        pb.environment().put("PGPASSWORD", password);

        pb.redirectErrorStream(true);
        p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((linha = reader.readLine()) != null) {
            System.out.println(linha);
        }
    }

    @Override
    public JsonArray getDatabases() throws SQLException {
        JsonArray databases = executeAsJsonArray("SELECT datname as name FROM pg_database;");
        for (int i = 0; i < databases.size(); i++) {
            databases.set(i, databases.get(i).getAsJsonObject().get("name"));
        }
        return databases;
    }

}
