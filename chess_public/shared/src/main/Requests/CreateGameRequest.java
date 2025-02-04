package Requests;
/**
 * A request to create a new game
 */
public class CreateGameRequest extends Request {
    /**
     @param token The authentication token to verify user identity
     @param gameName The name to be assigned to the new game
     */
    public CreateGameRequest(String token, String gameName) {
        type = RequestType.CREATE_GAME;
        header = token;
        this.gameName = gameName;
    }

}
