package Responses;
import java.util.ArrayList;
/**
 * Any response sent back to the user
 */
public abstract class Response {
    /**
     * Default constructor, empty
     */
    public Response() {}
    /**
     * Constructor that generates the default message for the given response code
     * @param responseCode
     */
    public Response(int responseCode) {
        this.responseCode = responseCode;
        switch (responseCode) {
            case 200:
                message = null;
                break;
            case 400:
                message = "Error: Bad request";
                break;
            case 401:
                message = "Error: Invalid identification; must log in";
                break;
            case 403:
                message = "Error: Access denied";
                break;
            case 500:
                message = "server.Server error (unspecified)";
                break;
            default:
                message = "Error in response message: Wrong code";
                System.out.println("Wrong response code; this is not used in the project");
                System.exit(0);
        }
    }
    /**
     * Constructor for generating custom response messages
     */
    public Response(int responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }
    /**
     * @return The status code for this response
     */
    public int getResponseCode() {
        return responseCode;
    }

    public String getMessage() {
        return message;
    }
    /**
     * User's username, for login and register responses
     */
    String username;
    /**
     * Authentication token to be used for current session if logging in or registering
     */
    String authToken;
    /**
     * The message associated with the response, if applicable
     */
    String message;
    /**
     * Indicates success or reason for failure
     */
    int responseCode;
}
