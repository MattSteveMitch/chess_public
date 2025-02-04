package WebSocketMsg;

public abstract class ClientMessage {
    enum ClientMsgType {
        GET_BOARD, MAKE_MOVE, LEAVE, GREETING
    }

    protected ClientMsgType type;
    protected String authToken;
    protected int gameID;
}
