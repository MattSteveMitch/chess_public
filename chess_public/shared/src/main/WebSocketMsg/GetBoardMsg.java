package WebSocketMsg;

public class GetBoardMsg extends ClientMessage {
    public GetBoardMsg(String authToken, int gameID) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.type = ClientMsgType.GET_BOARD;
    }

}
