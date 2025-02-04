package Requests;
/**
 * A login request
 */
public class LoginRequest extends Request {
    /**
     @param username
     @param password
     */
    public LoginRequest(String username, String password) {
        type = RequestType.LOGIN;
        this.username = username;
        this.password = password;
    }
}
