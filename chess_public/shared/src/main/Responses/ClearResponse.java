package Responses;
/**
 * A response to a request to clear all data
 */
public class ClearResponse extends Response {
    /**
     * Check that the response code is one that is used by this response type
     * @param responseCode
     */
    private void checkValidCode(int responseCode) {
        if (responseCode != 200 && responseCode != 500) {
            System.out.println("Wrong response code; this is not used for ClearResponse");
            System.exit(0);
        }
    }
    /**
     * Calls constructor in parent class that generates the default message for the given
     * response code
     * @param responseCode
     */
    public ClearResponse(int responseCode) {
        super(responseCode);
        checkValidCode(responseCode);
    }
    /**
     * Constructor for generating custom messages
     * @param responseCode
     * @param message The custom message
     */
    public ClearResponse(int responseCode, String message) {
        super(responseCode, message);
        checkValidCode(responseCode);
    }
}
