package models;
/**
 * An authentication token
 */
public class AuthToken {
    /**
     * @param token The token itself, in string form
     * @param username The user associated with this token
     */
    public AuthToken(String token, String username) {
        this.token = token;
        this.username = username;
    }
    /**
     * @return The string representation of this token
     */
    public String toString() {
        return token;
    }
    /**
     * @return Username associated with this token
     */
    public String getUsername() {
        return username;
    }
    /**
     * Compares two tokens by comparing the string representation of the token
     * and the username associated with it
     * @param other Other object being compared (should be of type AuthToken)
     * @return Whether the object is equal to the AuthToken calling equals()
     */
    public boolean equals(Object other) {
        if (!(other instanceof AuthToken)) {return false;}
        AuthToken otherToken = (AuthToken) other;
        return (token.equals(otherToken.token) && username.equals(otherToken.username));
    }
    /**
     * @return HashCode for the token
     */
    public int hashCode() {
        return token.hashCode() + 31 * username.hashCode();
    }
    /**
     * The token itself, in string form
     */
    private String token;
    /**
     * The user associated with this token
     */
    private String username;
}
