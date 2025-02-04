package Responses;
/**
 * A response to a request to join a game
 */
public class JoinGameResponse extends Response {
    /**
     * Calls constructor in parent class that generates the default message for the given
     * response code.
     * @param responseCode
     */
    public JoinGameResponse(int responseCode) {
        super(responseCode);
    }

    public JoinGameResponse(int responseCode, Boolean hadEffect) {
        super(responseCode);
        this.effect = hadEffect;
    }
    /**
     * Constructor for generating custom messages
     * @param responseCode
     * @param message The custom message
     */
    public JoinGameResponse(int responseCode, String message) {
        super(responseCode, message);
    }
    public Boolean hadEffect() {
        return effect;
    }

    Boolean effect;
}
