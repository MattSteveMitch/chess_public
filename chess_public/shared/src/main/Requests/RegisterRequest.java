package Requests;
/**
 * A request to create a new account
 */
public class RegisterRequest extends Request {
    /**
     @param username The desired username for new account
     @param password The desired password for new account
     */
    public RegisterRequest(String username, String password, String email) {
        type = RequestType.REGISTER;
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
