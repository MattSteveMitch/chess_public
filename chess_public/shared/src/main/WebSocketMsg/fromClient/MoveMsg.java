package WebSocketMsg.fromClient;
import chess.MoveObj;

public class MoveMsg extends ClientMessage {
    public MoveMsg(String authToken, MoveObj move, Integer gameID) {
        this.authToken = authToken;
        this.type = ClientMsgType.MAKE_MOVE;
        this.move = move.toString();
        this.gameID = gameID;
    }

    private String move;
}
