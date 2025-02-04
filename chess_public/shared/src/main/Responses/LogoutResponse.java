package Responses;
/**
 * A response to a request to log out
 */
public class LogoutResponse extends Response {
    /**
     * Check that the response code is one that is used by this response type
     * @param responseCode
     */
    private void checkValidCode(int responseCode) {
        if (responseCode == 400 || responseCode == 403) {
            System.out.println("Wrong response code; this is not used for LogoutResponse");
            System.exit(0);
        }
    }
    /**
     * Calls constructor in parent class that generates the default message for the given
     * response code.
     * @param responseCode
     */
    public LogoutResponse(int responseCode) {
        super(responseCode);
        checkValidCode(responseCode);
    }

    /**
     * Constructor for generating custom response messages
     * @param responseCode
     * @param message The custom message
     */
    public LogoutResponse(int responseCode, String message) {
        super(responseCode, message);
        checkValidCode(responseCode);
    }
}
