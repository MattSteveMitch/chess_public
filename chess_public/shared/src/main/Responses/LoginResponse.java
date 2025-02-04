package Responses;

import models.AuthToken;

/**
 * A response to a request to log in
 */
public class LoginResponse extends Response {
    /**
     * Check that the response code is one that is used by this response type
     * @param responseCode
     */
    private void checkValidCode(int responseCode) {
        if (responseCode == 400 || responseCode == 403) {
            System.out.println("Wrong response code; this is not used for LoginResponse");
            System.exit(0);
        }
    }
    /**
     * Calls constructor in parent class that generates the default message for the given
     * error code. To be used only with error codes.
     * @param responseCode
     */
    public LoginResponse(int responseCode) {
        super(responseCode);
        if (responseCode == 200) {
            System.out.println("Error: Login response success must specify AuthToken");
            System.exit(0);
        }
        checkValidCode(responseCode);
    }
    /**
     * Constructor for generating custom error messages (only used with error codes)
     * @param responseCode
     * @param message The custom message
     */
    public LoginResponse(int responseCode, String message) {
        super(responseCode, message);
        if (responseCode == 200) {
            System.out.println("Error: Login response success must specify AuthToken");
            System.exit(0);
        }
        checkValidCode(responseCode);
    }
    /**
     * The constructor called for a response to a successful request. If request was not
     * successful, the server creator is notified and the default error message is generated
     * @param responseCode
     * @param authToken The authentication token to be used for requests during this session
     */
    public LoginResponse(int responseCode, AuthToken authToken) {
        super(responseCode);
        checkValidCode(responseCode);
        if (responseCode != 200) {
            System.out.println("AuthToken not necessary for a failure response");
            return;
        }
        this.authToken = authToken.toString();
        this.username = authToken.getUsername();
    }

    public String getAuthToken() {
        return authToken;
    }
    public String getUsername() {
        return username;
    }
}
