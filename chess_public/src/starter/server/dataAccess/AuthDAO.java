package server.dataAccess;
import java.security.SecureRandom;
import java.sql.*;
import models.AuthToken;

/**
 * Data Access Object for authentication token objects
 */
public class AuthDAO {
    /**
     * @param db The database being accessed
     */
    public AuthDAO(DBInterface db) throws DataAccessException {
        this.db = db;
        this.RNG = new SecureRandom();
        try {
            db.executeCommand("CREATE TABLE IF NOT EXISTS authTokens(" +
                    "token CHAR(16) NOT NULL, username VARCHAR(32) NOT NULL," +
                    " PRIMARY KEY(token)  );", null);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    /**
     * Create a new AuthToken for user's current session
     * @param username The user associated with the token
     * @return A new complete AuthToken object
     */
    public AuthToken insert(String username) throws DataAccessException {
        AuthToken newToken;
        try (Connection connect = db.getNewConnection()) {
            AuthToken existingToken = find(username, connect);
            if (existingToken != null) {
                return existingToken;
            }
            var tokenStr = getNewTokenStr();
            newToken = new AuthToken(tokenStr, username);
            db.executeCommand("INSERT INTO authTokens values(?, ?);",
                    new String[] {tokenStr, username}, connect);
        } catch (SQLException SQLE) {
            throw new DataAccessException(SQLE.getMessage());
        }

        return newToken;
    }
    /**
     * Compare given AuthToken with currently stored AuthTokens
     * @param token The token to be matched against existing tokens
     * @return The matching token that was found, null if not found
     */
    public String matchToken(String token) throws DataAccessException {
        ResultSet result;
        try (Connection connect = db.getNewConnection()) {
            result = db.executeQuery("SELECT username FROM authTokens WHERE token = ?;",
                    new String[] {token}, connect);
            return ResultToUsernameStr(result);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private String ResultToUsernameStr(ResultSet result) throws DataAccessException {
        try {
            if (result.next()) {
                return result.getString("username");
            }
            else {return null;}
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private AuthToken ResultToTokenObj(ResultSet result) throws DataAccessException {
        String username;
        String tokenStr;

        try {
            if (result.next()) {
                tokenStr = result.getString("token");
                username = result.getString("username");
            }
            else {return null;}
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        return new AuthToken(tokenStr, username);
    }

    /**
     * Find AuthToken that matches the given string
     * @param username
     * @return The matching token that was found, null if not found
     */
    private AuthToken find(String username, Connection connect) throws DataAccessException {
        try {
            var result = db.executeQuery("SELECT * FROM authTokens WHERE username = ?",
                    new Object[] {username}, connect);
            return ResultToTokenObj(result);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    /**
     * Delete all tokens in database
     */
    public void clear() throws DataAccessException {
        try {
            db.executeCommand("DELETE FROM authTokens;", null);
        }
        catch (SQLException SQLE) {
            throw new DataAccessException(SQLE.getMessage());
        }
    }
    /**
     * Delete the given token
     * @param username The username whose associated token(s) should be deleted
     */
    public void delete(String username) throws DataAccessException {
        try {
            int rowsAffected = db.executeCommand("DELETE FROM authTokens WHERE username = ?;",
                    new String[] {username});
            if (rowsAffected == 0) {
                throw new DataAccessException("Error: Could not delete AuthToken; not found");
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    /**
     * @return A new string to serve as an authentication token
     */
    private String getNewTokenStr() {
        StringBuilder token = new StringBuilder("----------------");
        for (int i = 0; i < token.length(); i++) {
            do {
                token.setCharAt(i, (char) (RNG.nextInt(94) + 33));
            } while (token.charAt(i) == '\\' || token.charAt(i) == '"');
        }
        return token.toString();
    }
    /**
     * The database being accessed
     */
    private DBInterface db;
    private SecureRandom RNG;
}
