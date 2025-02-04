package WebSocketMsg;

public class LeaveMsg extends ClientMessage {
    public LeaveMsg(String authToken, int gameID) {
        this.type = ClientMsgType.LEAVE;
        this.authToken = authToken;
        this.gameID = gameID;
    }
}
