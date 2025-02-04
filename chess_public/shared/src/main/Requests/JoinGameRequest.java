package Requests;
/**
 * A request to clear join a game
 */
public class JoinGameRequest extends Request {
    /**
     @param token The authentication token to verify user identity
     @param gameID The gameID of the game to be joined
     @param playerColor The color the user wants to play as
     */
    public JoinGameRequest(String token, int gameID, chess.ChessGame.TeamColor playerColor) {
        type = RequestType.JOIN_GAME;
        header = token;
        this.gameID = gameID;
        this.playerColor = playerColor;
    }
}
