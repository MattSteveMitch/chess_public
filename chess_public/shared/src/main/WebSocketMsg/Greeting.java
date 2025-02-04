package WebSocketMsg;
import chess.ChessGame.TeamColor;

public class Greeting extends ClientMessage {
    public Greeting(String authToken, int gameID, TeamColor color) {
        type = ClientMsgType.GREETING;
        this.authToken = authToken;
        this.gameID = gameID;
        this.color = color;
    }

    private TeamColor color;
}
