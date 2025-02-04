package server.dataAccess;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TreeMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import models.AuthToken;
import models.Game;

/**
 * Object that modifies the database directly
 */
public class DBInterface {
    public DBInterface() throws SQLException {
        var connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/chess",
            "root", password);
        var command = connect.prepareStatement("CREATE DATABASE IF NOT EXISTS chess;");
        command.executeUpdate();

        Tokens = new HashMap<String, AuthToken>();
        Games = new TreeMap<Integer, Game>();
    }

    public Connection getNewConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/chess", "root", password);
    }

    public int executeCommand(String statement, Object[] SQLArgs) throws SQLException {
        return executeCommand(statement, SQLArgs, null);
    }

    public int executeCommand(String statement, Object[] SQLArgs, Connection connect) throws SQLException {
        boolean connectionProvided = (connect != null);
        if (!connectionProvided) {
            connect = getNewConnection();
        }
        var command = connect.prepareStatement(statement);

        int rowCount;

        for (int i = 0; SQLArgs != null && i < SQLArgs.length; i++) {
            if (SQLArgs[i] instanceof Integer) {
                command.setInt(i + 1, (Integer)SQLArgs[i]);
            }

            else if (SQLArgs[i] instanceof String) {
                command.setString(i + 1, (String)SQLArgs[i]);
            }
            else if (SQLArgs[i] instanceof NullableString) {
                if (((NullableString)SQLArgs[i]).val == null) {
                    command.setNull(i + 1, java.sql.Types.VARCHAR);
                }
                else {
                    command.setString(i + 1, ((NullableString)SQLArgs[i]).val);
                }
            }

            else if (SQLArgs[i] instanceof Boolean) {
                command.setBoolean(i + 1, (Boolean)SQLArgs[i]);
            }
            else if (SQLArgs[i] instanceof NullableBoolean) {
                if (((NullableBoolean)SQLArgs[i]).val == null) {
                    command.setNull(i + 1, java.sql.Types.BOOLEAN);
                }
                else {
                    command.setBoolean(i + 1, ((NullableBoolean)SQLArgs[i]).val);
                }
            }

        }
        rowCount = command.executeUpdate();
        if (!connectionProvided) {
            connect.close();
        }
        return rowCount;
    }

    public ResultSet executeQuery(String queryStr, Object[] SQLArgs) throws SQLException {
        try (Connection connect = getNewConnection();) {
            return executeQuery(queryStr, SQLArgs, connect);
        }
    }

    public ResultSet executeQuery(String queryStr, Object[] SQLArgs, Connection connection)
            throws SQLException {

        var query = connection.prepareStatement(queryStr);

        for (int i = 0; SQLArgs != null && i < SQLArgs.length; i++) {
            if (SQLArgs[0] instanceof Integer) {
                query.setInt(i + 1, (Integer)SQLArgs[i]);
            }
            if (SQLArgs[0] instanceof String) {
                query.setString(i + 1, (String)SQLArgs[i]);
            }
            if (SQLArgs[0] instanceof Boolean) {
                query.setBoolean(i + 1, (Boolean)SQLArgs[i]);
            }
        }
        var result = query.executeQuery();

        return result;
    }

    public HashMap<String, AuthToken> Tokens;
    public TreeMap<Integer, Game> Games;
    public String password = "placeholder_password";
}
