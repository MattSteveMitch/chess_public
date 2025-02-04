package Requests;

public class LeaveGameRequest extends Request {
    public LeaveGameRequest(String token, int gameID) {
        this.type = Request.RequestType.LEAVE_GAME;
        this.header = token;
        this.gameID = gameID;
    }
}
