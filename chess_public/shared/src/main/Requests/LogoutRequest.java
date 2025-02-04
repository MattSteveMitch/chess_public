package Requests;
/**
 * A logout request
 */
public class LogoutRequest extends Request {
    /**
     @param token The authentication token to verify user identity
     */
    public LogoutRequest(String token) {
        type = RequestType.LOGOUT;
        this.header = token;
    }
}
