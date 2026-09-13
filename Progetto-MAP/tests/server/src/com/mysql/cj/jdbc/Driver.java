package com.mysql.cj.jdbc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

/** Test-only JDBC fixture. Never include this source root in the application build. */
public class Driver implements java.sql.Driver {
    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(Driver.class.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static class Table {
        final List<String> names = new ArrayList<String>();
        final List<String> types = new ArrayList<String>();
        final List<List<Object>> rows = new ArrayList<List<Object>>();
    }

    private static Table table(String name) throws SQLException {
        Table table = new Table();
        if (name.equals("zerocolumn")) {
            return table;
        }
        if (name.equals("alltypes")) {
            table.names.addAll(Arrays.asList("C_CHAR", "C_VARCHAR", "C_LONGVARCHAR", "C_BIT",
                    "C_SHORT", "C_INT", "C_LONG", "C_FLOAT", "C_DOUBLE", "IGNORED"));
            table.types.addAll(Arrays.asList("CHAR", "VARCHAR", "LONGVARCHAR", "BIT",
                    "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "DECIMAL"));
            table.rows.add(Arrays.<Object>asList("a", "b", "c", "d", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
            return table;
        }
        if (name.equals("unsupportedtarget") || name.equals("decimal")) {
            table.names.addAll(Arrays.asList("X", "Y", "C"));
            table.types.addAll(Arrays.asList("VARCHAR", "DOUBLE", name.equals("decimal") ? "DECIMAL" : "TEXT"));
            table.rows.add(Arrays.<Object>asList("A", 2.0, name.equals("decimal") ? 100.0 : "not numeric"));
            table.rows.add(Arrays.<Object>asList("B", 4.0, name.equals("decimal") ? 200.0 : "also not numeric"));
            return table;
        }
        if (name.equals("onecolumn")) {
            table.names.add("C");
            table.types.add("DOUBLE");
            table.rows.add(Arrays.<Object>asList(1.0));
            return table;
        }
        if (name.equals("varchartarget")) {
            table.names.addAll(Arrays.asList("X", "C"));
            table.types.addAll(Arrays.asList("VARCHAR", "VARCHAR"));
            table.rows.add(Arrays.<Object>asList("A", "not numeric"));
            return table;
        }
        try {
            for (String line : Files.readAllLines(Paths.get(System.getProperty("map.fixtures")).resolve(name + ".dat"), StandardCharsets.UTF_8)) {
                if (line.startsWith("@desc ") || line.startsWith("@target ")) {
                    String[] parts = line.split(" ");
                    table.names.add(parts[1]);
                    table.types.add(parts.length == 3 ? "VARCHAR" : "DOUBLE");
                } else if (!line.startsWith("@")) {
                    List<Object> row = new ArrayList<Object>();
                    String[] values = line.split(",");
                    for (int i = 0; i < values.length; i++) {
                        row.add(table.types.get(i).equals("VARCHAR") ? values[i] : Double.valueOf(values[i]));
                    }
                    table.rows.add(row);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Fixture table not found: " + name, e);
        }
        if (name.equals("provaC")) {
            // The SQL sample calls its target C and uses FLOAT, unlike the file schema.
            table.names.set(2, "C");
            table.types.set(1, "FLOAT");
            table.types.set(2, "FLOAT");
        }
        return table;
    }

    private static ResultSet rows(final List<String> names, final List<List<Object>> rows) {
        final int[] index = {-1};
        return proxy(ResultSet.class, (p, m, args) -> {
            switch (m.getName()) {
            case "next":
                if (Boolean.getBoolean("map.resultSetFailure")) throw new SQLException("Test result-set failure");
                return ++index[0] < rows.size();
            case "close":
                if (Boolean.getBoolean("map.resultSetCloseFailure")) throw new SQLException("Test result-set close failure");
                return null;
            case "getString":
            case "getDouble":
                int column = args[0] instanceof Integer ? ((Integer) args[0]) - 1 : names.indexOf(args[0]);
                Object value = rows.get(index[0]).get(column);
                if (m.getName().equals("getDouble")) return value == null ? 0.0 : ((Number) value).doubleValue();
                return value == null ? null : value.toString();
            default: throw new UnsupportedOperationException("ResultSet." + m.getName());
            }
        });
    }

    private static ResultSet schema(String name) throws SQLException {
        Table table = table(name);
        List<List<Object>> columns = new ArrayList<List<Object>>();
        for (int i = 0; i < table.names.size(); i++)
            columns.add(Arrays.<Object>asList(table.names.get(i), table.types.get(i)));
        return rows(Arrays.asList("COLUMN_NAME", "TYPE_NAME"), columns);
    }

    private static ResultSet query(String sql) throws SQLException {
        int from = sql.indexOf(" FROM ");
        if (from < 0) throw new SQLException("Unexpected fixture query: " + sql);
        String projection = sql.substring("select ".length(), from);
        boolean distinct = projection.startsWith("distinct ");
        if (distinct) projection = projection.substring("distinct ".length());
        String name = sql.substring(from + " FROM ".length()).split(" ")[0];
        Table table = table(name);
        List<String> names = Arrays.asList(projection.split(","));
        List<List<Object>> selected = new ArrayList<List<Object>>();
        for (List<Object> row : table.rows) {
            List<Object> result = new ArrayList<Object>();
            for (String column : names) result.add(row.get(table.names.indexOf(column)));
            if (!distinct || !selected.contains(result)) selected.add(result);
        }
        if (sql.contains(" order by ")) {
            TreeSet<Object> sorted = new TreeSet<Object>();
            for (List<Object> row : selected) sorted.add(row.get(0));
            selected.clear();
            for (Object value : sorted) selected.add(Arrays.asList(value));
        }
        return rows(names, selected);
    }

    public Connection connect(String url, Properties properties) throws SQLException {
        if (!acceptsURL(url)) return null;
        if (Boolean.getBoolean("map.offline")) throw new SQLException("Test database unavailable");
        if (!url.startsWith("jdbc:mysql://localhost:3306/MapDB") ||
                !"MapUser".equals(properties.getProperty("user")) || !"map".equals(properties.getProperty("password")))
            throw new SQLException("MAP6 connection defaults changed");
        final boolean[] closed = {false};
        return proxy(Connection.class, (p, m, args) -> {
            switch (m.getName()) {
            case "getMetaData":
                if (Boolean.getBoolean("map.metadataFailure")) throw new SQLException("Test metadata failure");
                return proxy(DatabaseMetaData.class, (mp, mm, ma) -> {
                    if (mm.getName().equals("getColumns")) return schema((String) ma[2]);
                    throw new UnsupportedOperationException("DatabaseMetaData." + mm.getName());
                });
            case "createStatement":
                if (Boolean.getBoolean("map.statementFailure")) throw new SQLException("Test statement failure");
                return proxy(Statement.class, (sp, sm, sa) -> {
                    if (sm.getName().equals("executeQuery")) {
                        if (Boolean.getBoolean("map.queryFailure")) throw new SQLException("Test query failure");
                        return query((String) sa[0]);
                    }
                    if (sm.getName().equals("close")) {
                        if (Boolean.getBoolean("map.statementCloseFailure")) throw new SQLException("Test statement close failure");
                        return null;
                    }
                    throw new UnsupportedOperationException("Statement." + sm.getName());
                });
            case "close":
                if (Boolean.getBoolean("map.closeFailure")) throw new SQLException("Test close failure");
                closed[0] = true;
                return null;
            case "isClosed": return closed[0];
            default: throw new UnsupportedOperationException("Connection." + m.getName());
            }
        });
    }

    public boolean acceptsURL(String url) { return url.startsWith("jdbc:mysql:"); }
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties p) { return new DriverPropertyInfo[0]; }
    public int getMajorVersion() { return 0; }
    public int getMinorVersion() { return 0; }
    public boolean jdbcCompliant() { return false; }
    public Logger getParentLogger() { return Logger.getGlobal(); }
}
