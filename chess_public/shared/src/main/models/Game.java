package models;
import java.util.ArrayList;
import chess.GameObj;
/**
 * An active game, containing the usernames of the respective players, a game
 * ID, a name, and an object representation of the game status
 */
public class Game {
    /**
     * @param ID The ID to be assigned to the game
     * @param name The name chosen by the creator for the game
     */
    public Game(int ID, String name, chess.GameObj gameStatus, ArrayList<String> observers) {
        playerNames = new ArrayList<String>(2);
        playerNames.add(null);
        playerNames.add(null);
        gameID = ID;
        gameName = name;
        this.gameStatus = gameStatus;
        observerNames = observers;
    }

    public Game(int ID, String name, chess.GameObj gameStatus, ArrayList<String> observers,
                String whitePlayer, String blackPlayer) {
        playerNames = new ArrayList<String>(2);
        playerNames.add(whitePlayer);
        playerNames.add(blackPlayer);
        gameID = ID;
        gameName = name;
        this.gameStatus = gameStatus;
        observerNames = observers;
    }
    /**
     * Each game has one unique gameID, so it can be used as the hashcode
     * @return The games ID number
     */
    public int hashCode() {
        return gameID;
    }
    /**
     * Equality operator simply compares gameIDs; each game has one unique gameID
     * @return Whether the ID numbers match (assuming both objects are of Game type);
     * thus whether they are the same game
     */
    public boolean equals(Object other) {
        if (!(other instanceof Game)) {return false;}
        Game otherGame = (Game) other;
        return (gameID == otherGame.gameID);
    }
    /**
     * @return The game's ID number
     */
    public int getID() {
        return gameID;
    }
    /**
     * @return The game's name
     */
    public String getName() {
        return gameName;
    }
    /**
     *
     */
    public void setObserver(String username) {
        observerNames.add(username);
    }

    public ArrayList<String> getObservers() {
        return observerNames;
    }

    public GameObj getStatus() {
        return gameStatus;
    }
    /**
     * Joins a player to the game as the given color
     * @param username The username of the person joining the game
     * @param color The color the player will be playing as
     */
    public void setPlayer(String username, chess.ChessGame.TeamColor color) {
        playerNames.set(color.ordinal(), username);
    }
/*    /**
     * Do not use this function! Except in testing!
     */
  //  public void setID(int ID) {
    //    gameID = ID;
    //}
    /**
     * @param color
     * @return The username of the player playing as the given color
     */
    public String getPlayer(chess.ChessGame.TeamColor color) {
        return playerNames.get(color.ordinal());
    }
    /**
     * The ID number assigned to this game
     */
    final private int gameID;
    /**
     * The usernames of the players (white first, then black)
     */
    private ArrayList<String> playerNames;
    private ArrayList<String> observerNames;
    /**
     * The name chosen by the creator
     */
    private String gameName;
    /**
     * Object representing lower-level workings of the game
     */
    private chess.GameObj gameStatus;


    //private chess.ChessGame.TeamColor currTurn;
}
