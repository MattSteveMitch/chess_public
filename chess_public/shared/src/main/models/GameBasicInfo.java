package models;
import chess.ChessGame.TeamColor;

public class GameBasicInfo {
    public GameBasicInfo(Game theGame) {
        gameID = theGame.getID();
        blackUsername = theGame.getPlayer(TeamColor.BLACK);
        whiteUsername = theGame.getPlayer(TeamColor.WHITE);
        gameName = theGame.getName();
        observers = theGame.getObservers().size();
    }

    public GameBasicInfo(int ID, String blackPlayer, String whitePlayer, String name,
                         Boolean whiteActive, Boolean whiteWon, int observers) {
        gameID = ID;
        blackUsername = blackPlayer;
        whiteUsername = whitePlayer;
        gameName = name;
        this.observers = observers;
        this.whiteActive = whiteActive;
        this.whiteWon = whiteWon;
    }

    public String gameName;
    public int gameID;
    public String whiteUsername;
    public String blackUsername;
    public int observers;
    public Boolean whiteActive;
    public Boolean whiteWon;
}