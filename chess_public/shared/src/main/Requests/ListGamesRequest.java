package Requests;
/**
 * A request to list all active games on server
 */
public class ListGamesRequest extends Request {
    /**
     @param token The authentication token to verify user identity
     */
    public ListGamesRequest(String token) {
        type = RequestType.LIST_GAMES;
        this.header = token;
    }
}
