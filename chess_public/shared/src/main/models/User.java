package models;
/**
 * A registered user (whether active or not)
 */
public class User {
    /**
     * @param username The username chosen by the user
     * @param password The password chosen by the user
     * @param email The user's email
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    /**
     * server.Server will be set up so that each username is unique
     * @return The hashCode of the username
     */
    public int hashCode() {
        return username.hashCode();
    }
    /**
     * server.Server will be set up so that each username is unique
     * @return Whether the usernames match
     */
    public boolean equals(Object other) {
        if (!(other instanceof User)) {return false;}
        User otherUser = (User)other;
        return username.equals(otherUser.username);
    }
    /**
     * @return The user's password
     */
    public String getPassword() {
        return password;
    }
    /**
     * @return The user's username
     */
    public String getUsername() {
        return username;
    }
    /**
     * @return The user's email
     */
    public String getEmail() {return email;}

    /*public void changeEmail(String newEmail) {
        email = newEmail;
    }*/
    /**
     * User's password
     */
    private String password;
    /**
     * User's username
     */
    private String username;
    /**
     * User's email
     */
    private String email;
}
