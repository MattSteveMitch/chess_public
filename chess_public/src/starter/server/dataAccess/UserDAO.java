package server.dataAccess;
import java.util.HashSet;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import models.User;

/**
 * Data Access Object for User objects
 */
public class UserDAO {

    public UserDAO(DBInterface db) throws DataAccessException {
        this.db = db;
        try {
            db.executeCommand("CREATE TABLE IF NOT EXISTS users(" +
                    "username VARCHAR(32) NOT NULL, password VARCHAR(32) NOT NULL, " +
                    "email VARCHAR(64) NOT NULL, PRIMARY KEY(username), UNIQUE (email)  );", null);

        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private User ResultToUser(ResultSet result) throws SQLException {
        String username;
        String password;
        String email;

        if (result.next()) {
            username = result.getString("username");
            password = result.getString("password");
            email = result.getString("email");
        }
        else {
            return null;
        }

        return new User(username, password, email);
    }
    /**
     * Register a new user with given username, password, and email
     * @param username
     * @param password
     * @param email
     */
    public void insert(String username, String password, String email) throws DataAccessException {

        try {
            db.executeCommand("INSERT INTO users values(?, ?, ?);",
                    new String[] {username, password, email});
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

    }
    /**
     * Delete all users
     */
    public void clear() throws DataAccessException {
        try {
            db.executeCommand("DELETE FROM users;", null);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    /**
     * @param username
     * @return The User with the given username
     */
    public User find(String username) throws DataAccessException {
        ResultSet result = null;
        User newUser;
        try (Connection connect = db.getNewConnection()) {
            result = db.executeQuery(
                    "SELECT * FROM users WHERE username = ?;",
                    new String[]{username}, connect);
            newUser = ResultToUser(result);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        return newUser;
    }
    /**
     * Delete the user with the given username
     * @param username
     */
    public void delete(String username) throws DataAccessException {
        try (Connection connect = db.getNewConnection()) {
            ResultSet result = db.executeQuery("SELECT * FROM users WHERE username = ?;",
                    new String[]{username}, connect);
            if (!result.next()) {
                throw new DataAccessException("Error in UserDAO.delete(): User not found");
            }

            db.executeCommand("DELETE FROM users WHERE username = ?;",
                    new String[] {username}, connect);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    /**
     * The database being accessed
     */
    private DBInterface db;
}


