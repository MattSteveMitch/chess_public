package Requests;
/**
 * Any type of request received by server
 */
public abstract class Request {
    public enum RequestType {
        CLEAR, REGISTER, LOGIN, LOGOUT, LIST_GAMES, CREATE_GAME, JOIN_GAME, WEBSOCKET, LEAVE_GAME
    }

    public RequestType getType() {
        return type;
    }

    public String getToken() {
        return header;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getGameName() {
        return gameName;
    }

    public chess.ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }

    public int getGameID() {
        return gameID;
    }

    protected RequestType type;
    /**
     * Token to authenticate user's identity when necessary
     */
    protected String header;
    /**
     * Desired username for a register request, or the user's assigned username if logging in
     */
    protected String username;
    /**
     * Desired password for a register request, or the user's assigned password if logging in
     */
    protected String password;
    /**
     * User's email for a register request
     */
    protected String email;
    /**
     * Desired game name for createGame request
     */
    protected String gameName;
    /**
     * For joinGame requests, the color the player wants to play as
     */
    protected chess.ChessGame.TeamColor playerColor;
    /**
     * ID of game that the user wants to join
     */
    protected int gameID;
}
