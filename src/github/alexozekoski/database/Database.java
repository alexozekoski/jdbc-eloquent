/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package github.alexozekoski.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.alexozekoski.database.model.Column;
import github.alexozekoski.database.model.Model;
import github.alexozekoski.database.model.cast.Cast;
import github.alexozekoski.database.migration.Table;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; 
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.lang.reflect.Field;
import github.alexozekoski.database.migration.MigrationType;
import github.alexozekoski.database.model.ModelUtil;
import static github.alexozekoski.database.model.ModelUtil.CASTS;
import github.alexozekoski.database.model.Serial;
import github.alexozekoski.database.model.cast.CastUtil;
import github.alexozekoski.database.query.Query;
import github.alexozekoski.database.query.QueryModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alexozekoski generic abstract class to connect to the database JDBC
 */
public abstract class Database {

    private Connection con;
    private String jdbc;
    private String host;
    private String user;
    private String password;
    private String database;
    private Integer port;
    private boolean debugger = false;

    static {

        try {
            Class.forName("org.postgresql.Driver");
            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.firebirdsql.jdbc.FirebirdDriver");
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Log.printError(ex);
        }
    }

    /**
     * Returns generic connection.
     *
     * @param jdbc JSDB default jdbc name. example, firebirdsql, mysql,
     * postgresql, sqlite
     * @param host connection host. example localhost or 192.168.0.100
     * @param port connection port. example 5432 or 3306
     * @param user connection user. example root
     * @param password connection password. example 123456
     * @param database connection database or schema. example publix
     * @return returns null if jdbc name is not listed.
     */
    public static Database connect(String jdbc, String host, Integer port, String user, String password, String database) {
        switch (jdbc) {
            case PostgreSQL.JDBC: {
                return new PostgreSQL(host, port, user, password, database);
            }
            case MySQL.JDBC: {
                return new MySQL(host, port, user, password, database);
            }
            case SQLite.JDBC: {
                return new SQLite(host, port, user, password, database);
            }
            case FirebirdSQL.JDBC: {
                return new FirebirdSQL(host, port, user, password, database);
            }
        }
        return null;
    }

    public PreparedStatement createPreparedStatement(String query, boolean returnKeys, Object... param) throws SQLException {

        PreparedStatement stmt;
        if (returnKeys) {
            stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        } else {
            stmt = con.prepareStatement(query);
        }
        stmt.closeOnCompletion();

        if (param != null) {
            for (int i = 0; i < param.length; i++) {
                stmt.setObject(i + 1, param[i]);
            }
        }
        if (debugger) {
            System.out.println(query);
        }
        return stmt;
    }

    /**
     * Returns generic connection.
     *
     * @param json the json object must contain the keys: jdbc, port, user,
     * password, database
     * @return returns null if jdbc name is not listed.
     */
    public static Database connect(JsonObject json) {
        switch (json.get("jdbc").getAsString()) {
            case PostgreSQL.JDBC: {
                return new PostgreSQL(json);
            }
            case MySQL.JDBC: {
                return new MySQL(json);
            }
            case SQLite.JDBC: {
                return new SQLite(json);
            }
            case FirebirdSQL.JDBC: {
                return new FirebirdSQL(json);
            }
        }
        return null;
    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }

    protected Database(JsonObject json) {
        setJsonConfig(json);
    }

    protected Database(String jdbc, String host, Integer port, String user, String password, String database) {
        this.jdbc = jdbc;
        this.host = host;
        this.user = user;
        this.password = password;
        this.database = database;
        this.port = port;
    }

    /**
     * Returns generic connection.
     *
     * returns a new database instance without configuration, usage is not
     * recommended, the sets must be called after this
     *
     */
    public Database() {
    }

    public void setJsonConfig(JsonObject json) {
        this.jdbc = json.get("jdbc").getAsString();
        this.host = getAsString(json.get("host"));
        this.user = getAsString(json.get("user"));
        this.password = getAsString(json.get("password"));
        this.database = getAsString(json.get("database"));
        this.port = getAsInt(json.get("port"));
    }

    private String getAsString(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        } else {
            return json.getAsString();
        }
    }

    private Integer getAsInt(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return null;
        } else {
            return json.getAsInt();
        }
    }

    public String getJdbc() {
        return jdbc;
    }

    protected void setJdbc(String jdbc) {
        this.jdbc = jdbc;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public int getPort() {
        return port;
    }

    /**
     * @param query SQL defaul query,
     * @return returns an array of json objects, not a safe method SQL inject
     */
    public JsonArray executeAsJsonArray(String query) {
        try {
            return tryExecuteAsJsonArray(query);
        } catch (SQLException ex) {
            Log.printError(ex);
            return null;
        }
    }

    /**
     * @param query SQL defaul query,
     * @return returns an array of json objects, not a safe method SQL inject
     * @throws java.sql.SQLException
     */
    public JsonArray tryExecuteAsJsonArray(String query) throws SQLException {
        JsonObject ob = executeAsJsonObject(query);
        return ob != null ? ob.get("body").getAsJsonArray() : null;
    }

    /**
     * @return returns if this connection is active
     */
    public boolean isConnected() {
        try {
            return con != null && !con.isClosed();
        } catch (SQLException ex) {
            Log.printError(ex);
            return false;
        }
    }

    /**
     * tries to connect to database, generates JDBC default error
     *
     * @throws java.sql.SQLException
     */
    public void tryConnect() throws SQLException {
        tryConnect(false);
    }

    /**
     * tries to connect to database, generates JDBC default error
     *
     * @param readOnly
     * @throws java.sql.SQLException
     */
    public void tryConnect(boolean readOnly) throws SQLException {
        String url = "jdbc:" + jdbc + ":";

        if (host != null && !host.isEmpty()) {
            url += "//" + host;
        }
        if (port != null) {
            url += ":" + port + "/";
        }
        if (database != null && !database.isEmpty()) {
            url += database;
        }
        if (con != null && !con.isClosed()) {
            con.close();
        }
        con = DriverManager.getConnection(url, user, password);
        con.setReadOnly(readOnly);
    }

    public void tryConnectOrCreateDatabase() throws SQLException {
        try {
            tryConnect();
        } catch (Exception ex) {
            String database = getDatabase();
            if (database == null) {
                throw ex;
            }
            this.database = null;
            tryConnect();
            tryCreateDatabase(database);
            tryDisconnect();
            this.database = database;
            tryConnect();
        }
    }

    public boolean connectOrCreateDatabase() {
        try {
            tryConnectOrCreateDatabase();
            return true;
        } catch (Exception ex) {
            Log.printError(ex);
        }
        return false;
    }

    /**
     * tries to connect to database without generating error
     *
     * @param readOnly
     * @return false if not connected
     */
    public boolean connect(boolean readOnly) {
        try {
            tryConnect(readOnly);
        } catch (SQLException e) {
            Log.printError(e);
            return false;
        }
        return true;
    }

    /**
     * tries to connect to database without generating error
     *
     * @return false if not connected
     */
    public boolean connect() {
        try {
            tryConnect();
        } catch (SQLException e) {
            Log.printError(e);
            return false;
        }
        return true;
    }

    /**
     * tries to disconnect to database, generates JDBC default error
     *
     * @throws java.sql.SQLException
     */
    public void tryDisconnect() throws SQLException {
        if (con != null) {
            con.close();
        }
    }

    /**
     * tries to disconnect to database without generating error
     *
     * @return false if not connected
     */
    public boolean disconnect() {
        try {
            tryDisconnect();
        } catch (SQLException ex) {
            Log.printError(ex);
            return false;
        }
        return true;
    }

    /**
     * @param query SQL default query,
     * @return returns an object of json array head and body, not a safe method
     * SQL inject
     */
    public JsonObject executeAsJsonObject(String query) {
        try {
            return tryExecuteAsJsonObject(query);
        } catch (SQLException e) {
            Log.printError(e);
            return null;
        }
    }

    /**
     * @param query SQL default query,
     * @return returns an object of json array head and body, not a safe method
     * SQL inject
     * @throws java.sql.SQLException
     */
    public JsonObject tryExecuteAsJsonObject(String query) throws SQLException {
        JsonArray json = new JsonArray();
        JsonArray head = new JsonArray();
        ResultSet resultSet = execute(query);
        if (resultSet != null) {
            try {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int[] tipos = new int[resultSetMetaData.getColumnCount()];
                for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
                    head.add(resultSetMetaData.getColumnLabel(col));
                    //  String type = resultSetMetaData.getColumnClassName(col);
//                  if(Number.class.isAssignableFrom(resultSetMetaData));;
//                    if (type.equals(Integer.class.getCanonicalName()) || type.equals(int.class.getCanonicalName())
//                            || type.equals(Byte.class.getCanonicalName()) || type.equals(byte.class.getCanonicalName())
//                            || type.equals(Short.class.getCanonicalName()) || type.equals(short.class.getCanonicalName())
//                            || type.equals(Long.class.getCanonicalName()) || type.equals(long.class.getCanonicalName())) {
//                        tipos[col - 1] = 1;
//                    } else if (type.equals(Float.class.getCanonicalName()) || type.equals(Float.class.getCanonicalName())
//                            || type.equals(Double.class.getCanonicalName()) || type.equals(Double.class.getCanonicalName())) {
//                        tipos[col - 1] = 2;
//                    } else if (type.equals(Boolean.class.getCanonicalName()) || type.equals(boolean.class.getCanonicalName())) {
//                        tipos[col - 1] = 3;
//                    }
                }
                while (resultSet.next()) {
                    JsonObject linha = new JsonObject();

                    for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
                        Object value = resultSet.getObject(col);
                        json.add(CastUtil.sqlToJson(value));
//                        switch (tipos[col - 1]) {;
//                            case 1:
//                                linha.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getLong(col));
//                                break;
//                            case 2:
//                                linha.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getDouble(col));
//                                break;
//                            case 3:
//                                linha.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getBoolean(col));
//                                break;
//                            default:
//                                linha.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getString(col));
//                                break;
//                        }

                    }
                    json.add(linha);
                }
            } catch (SQLException e) {
                Log.printError(e);
            }
            try {
                resultSet.close();
            } catch (SQLException e) {
                Log.printError(e);
            }
        }

        JsonObject ob = new JsonObject();
        ob.add("head", head);
        ob.add("body", json);
        return ob;
    }

    public JsonArray executeAsJsonArray(String query, Object... objects) {
        JsonObject ob = executeAsJsonObject(query, objects);
        return ob != null ? ob.get("body").getAsJsonArray() : null;
    }

    /**
     * @param objects are the standard JDBC SQL primitive objects, String, int,
     * double, byte, Timestamp, Time, long
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     *
     * @return returns an object of json array head and body, safe method SQL
     * inject
     */
    public JsonObject executeAsJsonObject(String query, Object... objects) {

        try {
            return tryExecuteAsJsonObject(query, objects);
        } catch (SQLException ex) {
            Log.printError(ex);
            return null;
        }

    }

    /**
     * @param objects are the standard JDBC SQL primitive objects, String, int,
     * double, byte, Timestamp, Time, long
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     *
     * @return returns an object of json array head and body, safe method SQL
     * inject
     * @throws java.sql.SQLException
     */
    public JsonObject tryExecuteAsJsonObject(String query, Object... objects) throws SQLException {
        JsonArray json = new JsonArray();
        JsonArray head = new JsonArray();

        ResultSet resultSet = execute(query, objects);
        if (resultSet != null) {
            try {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int[] tipos = new int[resultSetMetaData.getColumnCount()];
                for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
                    head.add(resultSetMetaData.getColumnLabel(col));
                    String type = resultSetMetaData.getColumnClassName(col);
                    if (type.equals(Integer.class.getCanonicalName()) || type.equals(int.class.getCanonicalName())
                            || type.equals(Byte.class.getCanonicalName()) || type.equals(byte.class.getCanonicalName())
                            || type.equals(Short.class.getCanonicalName()) || type.equals(short.class.getCanonicalName())
                            || type.equals(Long.class.getCanonicalName()) || type.equals(long.class.getCanonicalName())) {
                        tipos[col - 1] = 1;
                    } else if (type.equals(Float.class.getCanonicalName()) || type.equals(Float.class.getCanonicalName())
                            || type.equals(Double.class.getCanonicalName()) || type.equals(Double.class.getCanonicalName())) {
                        tipos[col - 1] = 2;
                    } else if (type.equals(Boolean.class.getCanonicalName()) || type.equals(boolean.class.getCanonicalName())) {
                        tipos[col - 1] = 3;
                    }
                }
                while (resultSet.next()) {
                    JsonObject linha = new JsonObject();

                    for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
                        switch (tipos[col - 1]) {
                            case 1:
                                linha.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getLong(col));
                                break;
                            case 2:
                                linha.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getDouble(col));
                                break;
                            case 3:
                                linha.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getBoolean(col));
                                break;
                            default:
                                linha.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getString(col));
                                break;
                        }

                    }
                    json.add(linha);
                }
            } catch (SQLException e) {
                resultSet.close();
                throw e;
            }

            try {
                resultSet.close();
            } catch (SQLException e) {
                throw e;
            }
        }

        JsonObject ob = new JsonObject();
        ob.add("head", head);
        ob.add("body", json);
        return ob;
    }

    /**
     * @param query SQL defaul query,
     * @return returns an ResultSet of keys generated can be null if nothing is
     * generated, not a safe method SQL inject, the resulset must be closed
     * after use.
     */
    public ResultSet executeReturnigGeneratedKeys(String query) {
        try {
            return tryExecuteReturnigGeneratedKeys(query);
        } catch (SQLException ex) {
            Log.printError(ex);
            return null;
        }
    }

    /**
     * @param query SQL defaul query,
     * @return returns an ResultSet of keys generated can be null if nothing is
     * generated, not a safe method SQL inject, the resulset must be closed
     * after use.
     * @throws java.sql.SQLException
     */
    public ResultSet tryExecuteReturnigGeneratedKeys(String query) throws SQLException {
        try {
            Statement stmt = con.createStatement();
            stmt.closeOnCompletion();
            ResultSet resultSet = null;
            try {
                if (stmt.execute(query)) {
                    resultSet = stmt.getGeneratedKeys();
                }
            } catch (SQLException e) {
                stmt.close();
                throw e;
            }
            if (resultSet == null) {
                stmt.close();
            }
            return resultSet;

        } catch (SQLException ex) {
            throw ex;
        }
    }

    /**
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     * @return returns an JsonObject of keys generated,not safe method SQL
     * inject
     */
    public JsonObject executeReturnigGeneratedKeysAsJson(String query) {
        try {
            return tryExecuteReturnigGeneratedKeysAsJson(query);
        } catch (SQLException e) {
            Log.printError(e);
            return null;
        }
    }

    /**
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     * @return returns an JsonObject of keys generated,not safe method SQL
     * inject
     * @throws java.sql.SQLException
     */
    public JsonObject tryExecuteReturnigGeneratedKeysAsJson(String query) throws SQLException {
        JsonObject json = new JsonObject();
        ResultSet resultSet = executeReturnigGeneratedKeys(query);
        if (resultSet != null) {

            try {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int[] tipos = new int[resultSetMetaData.getColumnCount()];
                for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
                    String type = resultSetMetaData.getColumnClassName(col);
                    if (type.equals(Integer.class.getCanonicalName()) || type.equals(int.class.getCanonicalName())
                            || type.equals(Byte.class.getCanonicalName()) || type.equals(byte.class.getCanonicalName())
                            || type.equals(Short.class.getCanonicalName()) || type.equals(short.class.getCanonicalName())
                            || type.equals(Long.class.getCanonicalName()) || type.equals(long.class.getCanonicalName())) {
                        tipos[col - 1] = 1;
                    } else if (type.equals(Float.class.getCanonicalName()) || type.equals(Float.class.getCanonicalName())
                            || type.equals(Double.class.getCanonicalName()) || type.equals(Double.class.getCanonicalName())) {
                        tipos[col - 1] = 2;
                    } else if (type.equals(Boolean.class.getCanonicalName()) || type.equals(boolean.class.getCanonicalName())) {
                        tipos[col - 1] = 3;
                    }
                }
                if (resultSet.next()) {

                    for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
                        switch (tipos[col - 1]) {
                            case 1:
                                json.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getLong(col));
                                break;
                            case 2:
                                json.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getDouble(col));
                                break;
                            case 3:
                                json.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getBoolean(col));
                                break;
                            default:
                                json.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getString(col));
                                break;
                        }
                    }
                }
            } catch (SQLException e) {
                resultSet.close();
                throw e;
            }
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw e;
            }
        }
        return json;
    }

    /**
     * @param param are the standard JDBC SQL primitive objects, String, int,
     * double, byte, Timestamp, Time, long
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     * @return returns an JsonObject of keys generated,safe method SQL inject,
     * the resulset must be closed after use.
     */
    public JsonObject executeReturnigGeneratedKeysAsJson(String query, Object... param) {
        try {
            return tryExecuteReturnigGeneratedKeysAsJson(query, param);
        } catch (SQLException e) {
            Log.printError(e);
            return null;
        }
    }

    /**
     * @param param are the standard JDBC SQL primitive objects, String, int,
     * double, byte, Timestamp, Time, long
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     * @return returns an JsonObject of keys generated,safe method SQL inject,
     * the resulset must be closed after use.
     * @throws java.sql.SQLException
     */
    public JsonObject tryExecuteReturnigGeneratedKeysAsJson(String query, Object... param) throws SQLException {
        JsonObject json = new JsonObject();
        ResultSet resultSet = tryExecuteReturnigGeneratedKeys(query, param);
        if (resultSet != null) {

            try {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
//                int[] tipos = new int[resultSetMetaData.getColumnCount()];;
//                for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
//                    String type = resultSetMetaData.getColumnClassName(col);
//                    if (type.equals(Integer.class.getCanonicalName()) || type.equals(int.class.getCanonicalName())
//                            || type.equals(Byte.class.getCanonicalName()) || type.equals(byte.class.getCanonicalName())
//                            || type.equals(Short.class.getCanonicalName()) || type.equals(short.class.getCanonicalName())
//                            || type.equals(Long.class.getCanonicalName()) || type.equals(long.class.getCanonicalName())) {
//                        tipos[col - 1] = 1;
//                    } else if (type.equals(Float.class.getCanonicalName()) || type.equals(Float.class.getCanonicalName())
//                            || type.equals(Double.class.getCanonicalName()) || type.equals(Double.class.getCanonicalName())) {
//                        tipos[col - 1] = 2;
//                    } else if (type.equals(Boolean.class.getCanonicalName()) || type.equals(boolean.class.getCanonicalName())) {
//                        tipos[col - 1] = 3;
//                    }
//                }
                if (resultSet.next()) {

                    for (int col = 1; col <= resultSetMetaData.getColumnCount(); col++) {
                        Object value = resultSet.getObject(col);
                        json.add(resultSetMetaData.getColumnLabel(col), CastUtil.sqlToJson(value));
//                        switch (tipos[col - 1]) {
//                            case 1:
//                                json.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getLong(col));
//                                break;
//                            case 2:
//                                json.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getDouble(col));
//                                break;
//                            case 3:
//                                json.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getBoolean(col));
//                                break;
//                            default:
//                                json.addProperty(resultSetMetaData.getColumnLabel(col), resultSet.getString(col));
//                                break;
//                        };
                    }
                }
            } catch (SQLException e) {
                resultSet.close();
                throw e;
            }
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw e;
            }
        }
        return json;
    }

    public ResultSet tryExecuteReturnigGeneratedKeys(String query, Object... param) throws SQLException {
        try {

            PreparedStatement stmt = createPreparedStatement(query, true, param);
            ResultSet resultSet = null;
            try {

                if (stmt.executeUpdate() > 0) {
                    resultSet = stmt.getGeneratedKeys();
                    while (resultSet.next()) {;
                        for (int i = 1; i < resultSet.getMetaData().getColumnCount(); i++) {
                            System.out.print(resultSet.getObject(i));
                        }
                        System.out.println("-----");
                    }
                    resultSet.first();
                }

            } catch (SQLException e) {
                stmt.close();
                throw e;
            }
            if (resultSet == null) {
                stmt.close();
            }
            return resultSet;

        } catch (SQLException ex) {
            throw ex;
        }

    }

    /**
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     * @return returns an JsonObject of keys generated,not safe method SQL
     * inject, the resulset must be closed after use.
     */
    public ResultSet execute(String query) {
        try {
            return tryExecute(query);
        } catch (SQLException e) {
            Log.printError(e);
            return null;
        }
    }

    /**
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     * @return returns an JsonObject of keys generated,not safe method SQL
     * inject, the resulset must be closed after use.
     * @throws java.sql.SQLException
     */
    public ResultSet tryExecute(String query) throws SQLException {
        try {
            Statement stmt = con.createStatement();
            stmt.closeOnCompletion();
            ResultSet resultSet = null;
            try {
                resultSet = stmt.executeQuery(query);
            } catch (SQLException e) {
                stmt.close();
                throw e;
            }
            if (resultSet == null) {
                stmt.close();
            }
            return resultSet;

        } catch (SQLException ex) {
            throw ex;
        }
    }

    /**
     * @param param are the standard JDBC SQL primitive objects, String, int,
     * double, byte, Timestamp, Time, long
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     * @return returns an JsonObject of keys generated,safe method SQL inject,
     * the resulset must be closed after use.
     */
    public ResultSet execute(String query, Object... param) {
        try {
            return tryExecute(query, param);
        } catch (SQLException e) {
            Log.printError(e);
            return null;
        }
    }

    /**
     * @param param are the standard JDBC SQL primitive objects, String, int,
     * double, byte, Timestamp, Time, long
     * @param query SQL default query, use ? in the query to reference an object
     * of the objects,
     * @return returns an JsonObject of keys generated,safe method SQL inject,
     * the resulset must be closed after use.
     * @throws java.sql.SQLException
     */
    public ResultSet tryExecute(String query, Object... param) throws SQLException {
        try {
            PreparedStatement stmt = createPreparedStatement(query, false, param);

            ResultSet resultSet = null;
            try {
                resultSet = stmt.executeQuery();
            } catch (SQLException e) {
                stmt.close();
                throw e;
            }
            if (resultSet == null) {
                stmt.close();
            }
            return resultSet;

        } catch (SQLException ex) {
            throw ex;
        }
    }

    public int executeUpdate(String query, Object[] param) {
        try {
            return tryExecuteUpdate(query, param);
        } catch (Exception e) {
            Log.printError(e);
            return -1;
        }
    }

    public int executeUpdate(String query) {
        try {
            return tryExecuteUpdate(query);
        } catch (Exception e) {
            Log.printError(e);
            return -1;
        }
    }

    public int tryExecuteUpdate(String query, Object[] param) throws SQLException {
        try {
            PreparedStatement stmt = createPreparedStatement(query, false, param);

            int total = 0;
            try {
                total = stmt.executeUpdate();
            } catch (SQLException e) {
                stmt.close();
                throw e;
            }
            stmt.close();
            return total;

        } catch (SQLException ex) {
            throw ex;
        }
    }

    public boolean executeVoid(String query) {
        try {
            return tryExecuteVoid(query);

        } catch (SQLException ex) {
            Log.printError(ex);
            return false;
        }
    }

    public boolean tryExecuteVoid(String query) throws SQLException {
        try {
            Statement stmt = con.createStatement();
            stmt.closeOnCompletion();
            boolean exe = false;
            try {
                exe = stmt.execute(query);
            } catch (SQLException e) {
                stmt.close();
                throw e;
            }
            stmt.close();
            return exe;

        } catch (SQLException ex) {
            throw ex;
        }
    }

    public boolean tryExecuteVoid(String query, Object[] param) throws SQLException {
        try {
            PreparedStatement stmt = createPreparedStatement(query, false, param);
            boolean res = false;
            try {
                stmt.execute();
            } catch (SQLException e) {
                stmt.close();
                throw e;
            }
            stmt.close();
            return res;

        } catch (SQLException ex) {
            throw ex;
        }
    }

    public int tryExecuteUpdate(String query) throws SQLException {
        try {
            Statement stmt = con.createStatement();
            stmt.closeOnCompletion();
            int total = 0;
            try {
                total = stmt.executeUpdate(query);
            } catch (SQLException e) {
                stmt.close();
                throw e;
            }
            stmt.close();
            return total;

        } catch (SQLException ex) {
            throw ex;
        }
    }

    /**
     * Returns an json list os tables in this database connection. Exmaple: [
     * "table 1", "table 2" ]
     *
     * @return JsonArray
     */
    public JsonArray getTables() throws SQLException {
        String[] types = {"TABLE"};
        ResultSet tables = con.getMetaData().getTables(null, null, null, types);
        JsonArray array = new JsonArray();
        try {
            while (tables.next()) {
                String name = tables.getString("table_name");
                array.add(name);
            }
        } catch (SQLException ex) {
            tables.close();
            throw ex;
        }
        tables.close();
        return array;
    }

    public String[] getTablesAsArray() throws SQLException {
        JsonArray json = getTables();
        String[] tables = new String[json.size()];
        for (int i = 0; i < tables.length; i++) {
            tables[i] = json.get(i).getAsString();
        }
        return tables;
    }

    /**
     * Returns an json list os columns in a table, name an type. Example: [
     * {"name": "column 1", type: "varchar"}, {"name": "column 1", type:
     * "varchar"} ]
     *
     * @param table table in this database
     * @return JsonArray
     */
    public JsonObject columnsAsJsonArray(String table) throws SQLException {
        ResultSet meta = con.getMetaData().getColumns(null, null, table, null);
        JsonObject json = new JsonObject();
        try {
            while (meta.next()) {
                String column = meta.getString("COLUMN_NAME");
                JsonObject data = new JsonObject();
                String type = meta.getString("TYPE_NAME").toUpperCase();
                long size = meta.getLong("COLUMN_SIZE");
                long dec = meta.getLong("DECIMAL_DIGITS");
                long prec = meta.getLong("NUM_PREC_RADIX");
                boolean nullable = meta.getBoolean("NULLABLE");
                String value = meta.getString("COLUMN_DEF");
                // System.out.println(type);
                int dataType = meta.getInt("DATA_TYPE");
                boolean autoincrement = meta.getString("IS_AUTOINCREMENT").equals("YES");
                String jdbctype = java.sql.JDBCType.valueOf(dataType).getName();
                data.addProperty("type", type);
                data.addProperty("size", size);
                data.addProperty("decimal", dec);
                data.addProperty("precision", prec);
                data.addProperty("nullable", nullable);
                data.addProperty("default_value", value);
                data.addProperty("data_type", dataType);
                data.addProperty("jdbc_type", jdbctype);
                data.addProperty("autoincrement", autoincrement);
                json.add(column, data);
                // cols.add(getMigrationType().castTypeSQL(column, type, dataType, size, prec, dec, nullable, autoincrement, value, null, null));
            }
        } catch (SQLException ex) {
            meta.close();
            throw ex;
        }
        meta.close();
        return json;
    }

    public github.alexozekoski.database.migration.Column[] getColumns(String table) throws SQLException {
        ResultSet meta = con.getMetaData().getColumns(null, null, table, null);
        List<github.alexozekoski.database.migration.Column> cols = new ArrayList();
        try {
            while (meta.next()) {
                String column = meta.getString("COLUMN_NAME");
                String type = meta.getString("TYPE_NAME").toUpperCase();
                long size = meta.getLong("COLUMN_SIZE");
                long dec = meta.getLong("DECIMAL_DIGITS");
                long prec = meta.getLong("NUM_PREC_RADIX");
                boolean nullable = meta.getBoolean("NULLABLE");
                String value = meta.getString("COLUMN_DEF");
                int dataType = meta.getInt("DATA_TYPE");
                boolean autoincrement = meta.getString("IS_AUTOINCREMENT").equals("YES");
                cols.add(getMigrationType().castTypeSQL(column, type, dataType, size, prec, dec, nullable, autoincrement, value, null, null));
            }
        } catch (SQLException ex) {
            meta.close();
            throw ex;
        }
        meta.close();
        return cols.toArray(new github.alexozekoski.database.migration.Column[cols.size()]);
    }

    public void createTableIfNotExist(Class<? extends Model>... models) throws SQLException {
        JsonArray lista = getTables();
        if (lista == null) {
            return;
        }
        for (Class<? extends Model> model : models) {
            boolean exist = false;
            String table = ModelUtil.getTable(model);
            for (int i = 0; i < lista.size(); i++) {
                JsonElement nm = lista.get(i);
                if (nm != null && !nm.isJsonNull()) {
                    String tabela = nm.getAsString();
                    if (tabela.equals(table)) {
                        exist = true;
                        break;
                    }
                }
            }
            if (!exist) {
                migrate(model).create();
            }
        }
    }

    public boolean hasTable(String table) throws SQLException {
        JsonArray lista = getTables();
        if (lista == null) {
            return false;
        }
        for (int i = 0; i < lista.size(); i++) {
            JsonElement nm = lista.get(i);
            if (nm != null && !nm.isJsonNull()) {
                String tabela = nm.getAsString();
                if (tabela.equals(table)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setReadOnly(boolean readOnly) {
        try {
            trySetReadOnly(readOnly);
        } catch (SQLException e) {
            Log.printError(e);
        }
    }

    public void trySetReadOnly(boolean readOnly) throws SQLException {
        con.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        return con.isReadOnly();
    }

    public Query query() {
        return new Query(this);
    }

    public Table table(String name) {
        return new Table(name, getMigrationType(), this);
    }

    public Table migrate(Class<? extends Model> model) {
        Table table = table(ModelUtil.getTable(model));
        for (Field field : ModelUtil.getMigrationColumns(model)) {
            Column col = field.getAnnotation(Column.class);

            github.alexozekoski.database.migration.Column colmig = null;
            if (col.serial()) {
                table.bigserial(col.value());
                continue;
            }
            if (!col.type().isEmpty()) {
                colmig = table.text(col.value());
            } else {
                if (col.string() > 0) {
                    colmig = table.string(col.value(), col.string());
                } else {
                    if (col.text()) {
                        colmig = table.custom(col.value(), col.type());
                    } else {
                        String coluna = col.value();
                        try {
                            Class type = CastUtil.primitiveToObject(field.getType());
                            for (Cast cast : CASTS) {
                                if (cast.type(null, field, type, table).isAssignableFrom(type)) {
                                    String t = cast.dataType(field, type, this);
                                    colmig = table.custom(coluna, t);
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            Log.printError(ex);
                        }
                    }
                }
            }

            if (colmig == null) {
                continue;
            }
            if (col.notnull()) {
                colmig = colmig.notnull();
            }
            if (col.unique()) {
                colmig = colmig.unique();
            }
            if (!col.foreignKey().equals(Serial.class)) {
                colmig = colmig.foreignKey(col.foreignKey());
            } else {
                if (!col.foreign().isEmpty() && !col.key().isEmpty()) {
                    colmig.foreignKey(col.foreign(), col.key());
                }
            }
            if (!col.defaultValue().isEmpty()) {
                colmig = colmig.defaultValue(col.defaultValue());
            }
            if (!col.onDelete().isEmpty()) {
                colmig.onDelete(col.onDelete());
            }

        }
        return table;
    }

    public MigrationType getMigrationType() {
        return null;
    }

    public boolean tryExecuteFile(File sql) throws IOException, SQLException {

        FileInputStream in = new FileInputStream(sql);
        byte[] buffer = new byte[(int) sql.length()];
        in.read(buffer);
        in.close();
        return tryExecuteVoid(new String(buffer));
    }

    public boolean executeFile(File sql) {
        try {
            return tryExecuteFile(sql);
        } catch (Exception ex) {
            Log.printError(ex);
            return false;
        }
    }

    public boolean tryExecuteFile(String dirSql) throws IOException, SQLException {
        return tryExecuteFile(new File(dirSql));
    }

    public boolean executeFile(String dir) {
        try {
            return tryExecuteFile(dir);
        } catch (Exception ex) {
            Log.printError(ex);
            return false;
        }
    }

    public boolean tryCreateDatabase(String database) throws SQLException {
        String query = getMigrationType().createDatabase(database);
        return tryExecuteVoid(query);
    }

    public boolean createDatabase(String database) {
        try {
            return tryCreateDatabase(database);
        } catch (Exception e) {
            Log.printError(e);
        }
        return false;
    }

    public boolean tryDropDatabase(String database) throws SQLException {
        String query = getMigrationType().dropDatabase(database);
        return tryExecuteVoid(query);
    }

    public boolean dropDatabase(String database) {
        try {
            return tryDropDatabase(database);
        } catch (Exception e) {
            Log.printError(e);
        }
        return false;
    }

    public boolean tryDropDatabase() throws SQLException {
        return tryDropDatabase(getDatabase());
    }

    public boolean dropDatabase() {
        try {
            return tryDropDatabase();
        } catch (Exception e) {
            Log.printError(e);
        }
        return false;
    }

    public void dropAllTables() throws SQLException {
        String[] tables = getTablesAsArray();
        for (String table : tables) {
            table(table).dropTable();
        }
    }

    public JsonObject foreingKeys(String table) throws SQLException {
        DatabaseMetaData dm = con.getMetaData();
        ResultSet rs = dm.getImportedKeys(null, null, table);
        JsonObject json = new JsonObject();
        try {
            while (rs.next()) {
                JsonObject data = new JsonObject();
                data.addProperty("table", rs.getString("fktable_name"));
                data.addProperty("fk_name", rs.getString("fk_name"));
                data.addProperty("pk_name", rs.getString("pk_name"));
                data.addProperty("update_rule", rs.getString("update_rule"));
                data.addProperty("delete_rule", rs.getString("delete_rule"));
                json.add(rs.getString("fkcolumn_name"), data);
            }
        } catch (SQLException ex) {
            rs.close();
            throw ex;
        }
        rs.close();
        return json;
    }

    public JsonObject foreingKeys() throws SQLException {
        String[] tables = getTablesAsArray();
        JsonObject json = new JsonObject();
        for (String table : tables) {
            JsonObject data = foreingKeys(table);
            if (!data.keySet().isEmpty()) {
                json.add(table, data);
            }
        }
        return json;
    }

    public String[] getDatabasesAsArray() throws SQLException {
        JsonArray json = getDatabases();
        String[] databases = new String[json.size()];
        for (int i = 0; i < json.size(); i++) {
            databases[i] = json.get(i).getAsString();
        }
        return databases;
    }

    public JsonArray getDatabases() throws SQLException {
        DatabaseMetaData dm = con.getMetaData();
        ResultSet rs = dm.getCatalogs();
        JsonArray json = new JsonArray();
        try {
            while (rs.next()) {
                json.add(rs.getString("TABLE_CAT"));
            }
        } catch (SQLException ex) {
            rs.close();
            throw ex;
        }
        rs.close();
        return json;
    }

    public PreparedStatement createStatement(String query, Object[] param) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(query);
        if (param != null) {
            for (int i = 0; i < param.length; i++) {
                stmt.setObject(i + 1, param[i]);
            }
        }
        return stmt;
    }

    public Connection getConnection() {
        return con;
    }

    public <M extends Model<M>> QueryModel<M> query(Class<M> classe) {
        return new QueryModel<>(classe, this);
    }

    public boolean isDebugger() {
        return debugger;
    }

    public void setDebugger(boolean debugger) {
        this.debugger = debugger;
    }

}
