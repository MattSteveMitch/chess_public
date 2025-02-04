package Responses;

import models.AuthToken;

/**
 * A response to a request to register new account
 */
public class RegisterResponse extends Response {
    /**
     * Check that the response code is one that is used by this response type
     * @param responseCode
     */
    private void checkValidCode(int responseCode) {
        if (responseCode == 401) {
            System.out.println("Wrong response code; 401 is not used for RegisterResponse");
            System.exit(0);
        }
    }
    /**
     * If a constructor is only meant to be used for an error code, this makes sure that
     * the response code is indeed an error code
     * @param responseCode
     */
    private void checkNot200(int responseCode) {
        if (responseCode == 200) {
            System.out.println("Error: Register response success must specify AuthToken");
            System.exit(0);
        }
    }
    /**
     * Calls constructor in parent class that generates the default message for the given
     * error code. To be used only with error codes.
     * @param responseCode
     */
    public RegisterResponse(int responseCode) {
        super(responseCode);
        checkNot200(responseCode);
        checkValidCode(responseCode);
    }
    /**
     * Constructor for generating custom error messages (only used with error codes)
     * @param responseCode
     * @param message The custom message
     */
    public RegisterResponse(int responseCode, String message) {
        super(responseCode, message);
        checkNot200(responseCode);
        checkValidCode(responseCode);
    }
    /**
     * The constructor called for a response to a successful request. If request was not
     * successful, the server creator is notified and the default error message is generated
     * @param responseCode
     * @param authToken The authentication token to be used for current session
     */
    public RegisterResponse(int responseCode, AuthToken authToken) {
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
