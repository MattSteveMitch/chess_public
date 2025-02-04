package server.dataAccess;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import chess.*;
import chess.ChessPiece.PieceType;
import chess.ChessGame.TeamColor;
import models.Game;
import models.GameBasicInfo;
/**
 * Data Access Object for games
 */
public class GameDAO {
    public GameDAO(DBInterface db) throws DataAccessException {
        this.db = db;
        //this.games = new HashMap<Integer, GameObj>();

        try (Connection connect = db.getNewConnection()) {
            boolean existsTable = existsGameTable(connect);
            if (existsTable) {takenIDs = initTakenIDs(connect);}
            else {
                takenIDs = new HashSet<Integer>();
                db.executeCommand("CREATE TABLE games(id INT NOT NULL, whitePlayer VARCHAR(32), " +
                        "blackPlayer VARCHAR(32), gameName VARCHAR(32), whiteActive BOOLEAN, " +
                        "whiteWon BOOLEAN, " + "PRIMARY KEY(id)  );", null, connect);
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    /**
     * @return A list of all games requested by a listGames request
     */
    public ArrayList<GameBasicInfo> getAll() throws DataAccessException {
        ArrayList<GameBasicInfo> returnList = new ArrayList<>();// Game class must be stripped down
        // to a simpler class storing its
        // basic properties to avoid issues
        // with converting to Json string
        ResultSet result;

        String name;
        int ID;
        String whitePlayer;
        String blackPlayer;
        Boolean whiteActive;
        Boolean whiteWon;
        int numOfObservers;

        try (Connection connect = db.getNewConnection()) {
            result = db.executeQuery("SELECT * FROM games;", null, connect);
            while (result.next()) {
                name = result.getString("gameName");
                ID = result.getInt("id");
                whitePlayer = result.getString("whitePlayer");
                blackPlayer = result.getString("blackPlayer");
                whiteActive = result.getBoolean("whiteActive");
                if (result.wasNull()) {
                    whiteActive = null;
                }
                whiteWon = result.getBoolean("whiteWon");
                if (result.wasNull()) {
                    whiteWon = null;
                }
                numOfObservers = getNumOfObservers(ID, connect);

                returnList.add(new GameBasicInfo(ID, blackPlayer, whitePlayer, name, whiteActive,
                        whiteWon, numOfObservers));
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        return returnList;
    }
    /**
     * Create a new game with given gameName
     * @param gameName
     * @return The ID of the new game
     */
    public int insert(String gameName) throws DataAccessException {

        Integer ID = getNewID();
        String boardName = "Board" + ID.toString();
        String observerTableName = "Observers" + ID.toString();
        String moveTableName = "Moves" + ID.toString();
        NullableString nullPlayer = new NullableString(null);

        var theGame = new GameObj();

        try (Connection connect = db.getNewConnection()) {
            db.executeCommand("INSERT INTO games VALUES(?, ?, ?, ?, ?, ?);",
                    new Object[] {ID, nullPlayer, nullPlayer, gameName, true, new NullableBoolean(null)},
                    connect);
            db.executeCommand("CREATE TABLE IF NOT EXISTS " + boardName +
                    "(position CHAR(2) NOT NULL, type VARCHAR(6) NOT NULL, " +
                    "isWhite BOOLEAN NOT NULL, hasMoved BOOLEAN, vulnerableToEnPassant BOOLEAN, " +
                    "PRIMARY KEY(position) );", null, connect);
            db.executeCommand("CREATE TABLE IF NOT EXISTS " + moveTableName +
                    "(number INT AUTO_INCREMENT, move VARCHAR(7) NOT NULL, " +
                    "PRIMARY KEY(number) );", null, connect);
            updateBoard(boardName, (BoardObj)theGame.getBoard(), connect);

            db.executeCommand("CREATE TABLE IF NOT EXISTS " + observerTableName +
                            "(username VARCHAR(32) UNIQUE NOT NULL)",
                    null, connect);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        takenIDs.add(ID);
        return ID;
    }

    public void updateGameRecord(Game theGame) throws DataAccessException {
        updateGameRecord(theGame, true);
    }

    public GameBasicInfo getRow(Integer id) throws DataAccessException {
        String gameName;
        String whitePlayer;
        String blackPlayer;
        int numOfObservers;
        Boolean whiteActive;
        Boolean whiteWon = null;

        try (Connection connect = db.getNewConnection()) {
            var gameInfoResult = db.executeQuery("SELECT * FROM games WHERE id = ?;",
                    new Integer[] {id}, connect);
            if (!gameInfoResult.next()) {
                return null;
            }
            whiteActive = gameInfoResult.getBoolean("whiteActive");
            if (gameInfoResult.wasNull()) {
                whiteActive = null;
            }
            numOfObservers = getNumOfObservers(id, connect);
            blackPlayer = gameInfoResult.getString("blackPlayer");
            whitePlayer = gameInfoResult.getString("whitePlayer");
            gameName = gameInfoResult.getString("gameName");
            if (whiteActive == null) {
                whiteWon = gameInfoResult.getBoolean("whiteWon");
                if (gameInfoResult.wasNull()) {
                    whiteWon = null;
                }
            }
        }
        catch (SQLException x) {
            throw new DataAccessException(x.getMessage());
        }

        return new GameBasicInfo(id, blackPlayer, whitePlayer, gameName, whiteActive,
                whiteWon, numOfObservers);
    }

    public void updateGameRecord(Game theGame, boolean includeBoard) throws DataAccessException {
        Boolean whiteActive;
        Integer ID = theGame.getID();
        NullableString blackPlayer = new NullableString(theGame.getPlayer(TeamColor.BLACK));
        NullableString whitePlayer = new NullableString(theGame.getPlayer(TeamColor.WHITE));
        /*if (blackPlayer == null) {
            blackPlayer = "null";
        }
        if (whitePlayer == null) {
            whitePlayer = "null";
        }*/

        GameObj gameStatus = theGame.getStatus();
        if (gameStatus.getGameOver()) {
            whiteActive = null;
        }
        else {
            whiteActive = (gameStatus.getTeamTurn() == TeamColor.WHITE);
        }
        TeamColor winner = gameStatus.getWinner();
        Boolean whiteWinner;
        if (winner == null) {
            whiteWinner = null;
        }
        else if (winner == TeamColor.WHITE) {
            whiteWinner = true;
        }
        else {
            whiteWinner = false;
        }

        try (Connection connect = db.getNewConnection()) {
            Integer rowsAffected = db.executeCommand("UPDATE games SET whitePlayer = ?, blackPlayer = ?, " +
                    "whiteActive = ?, " + "whiteWon = ? " + "WHERE id = ?;", new Object[] {whitePlayer, blackPlayer,
                    new NullableBoolean(whiteActive), new NullableBoolean(whiteWinner), ID}, connect);
            updateBoard("Board" + ID.toString(), (BoardObj)gameStatus.getBoard(), connect);
            // updateObservers(ID, observers);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /*public DBInterface getDB() {
        return db;
    }*/

    public boolean exists(Integer gameID) throws DataAccessException {
        ResultSet result;
        try (Connection connect = db.getNewConnection()) {
            result = db.executeQuery("SELECT id FROM games WHERE id = ?;",
                    new Integer[] {gameID}, connect);
            return result.next();
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public BoardObj getBoard(Integer gameID, Connection connect) throws DataAccessException {
        boolean noConnectionProvided = (connect == null);
        ResultSet gameBoardResult;
        BoardObj theBoard;

        String boardName = "Board" + gameID.toString();
        try {
            if (noConnectionProvided) {
                connect = db.getNewConnection();
            }

            gameBoardResult = db.executeQuery("SELECT * FROM " + boardName + ";",
                    null, connect);
            theBoard = constructBoard(gameBoardResult);

            if (noConnectionProvided) {
                connect.close();
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        return theBoard;
    }
    /**
     * Find the game with given gameID
     * @param gameID
     * @return The game with the given ID, null if not found
     */
    public Game find(Integer gameID) throws DataAccessException {
        ResultSet gameInfoResult;
        BoardObj theBoard;
        Boolean whiteActive;
        TeamColor activePlayer = null;
        GameObj gameStatus;
        String blackPlayer;
        String whitePlayer;
        String gameName;
        Boolean whiteWon = null;
        TeamColor winner = null;
        Boolean gameOver = false;

        String boardName = "Board" + gameID.toString();
        try (Connection connect = db.getNewConnection()) {
            gameInfoResult = db.executeQuery("SELECT * FROM games WHERE id = ?;",
                    new Integer[] {gameID}, connect);
            if (!gameInfoResult.next()) {
                return null;
            }
            whiteActive = gameInfoResult.getBoolean("whiteActive");
            if (gameInfoResult.wasNull()) {
                whiteActive = null;
            }
            blackPlayer = gameInfoResult.getString("blackPlayer");
            whitePlayer = gameInfoResult.getString("whitePlayer");
            gameName = gameInfoResult.getString("gameName");
            if (whiteActive == null) {
                whiteWon = gameInfoResult.getBoolean("whiteWon");
                if (gameInfoResult.wasNull()) {
                    whiteWon = null;
                }
            }

            theBoard = getBoard(gameID, connect);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        if (whiteWon == null) {
            winner = null;
        }
        else if (whiteWon) {
            winner = TeamColor.WHITE;
        }
        else {
            winner = TeamColor.BLACK;
        }

        if (whiteActive == null) {
            gameOver = true;
        }
        else if (whiteActive) {
            activePlayer = TeamColor.WHITE;
        }
        else {
            activePlayer = TeamColor.BLACK;
        }

        gameStatus = new GameObj(theBoard, activePlayer, gameOver, winner);

        Game returnVal = new Game(gameID, gameName, gameStatus, getObservers(gameID));
        returnVal.setPlayer(blackPlayer, TeamColor.BLACK);
        returnVal.setPlayer(whitePlayer, TeamColor.WHITE);

        return returnVal;
    }

    private int getNumOfObservers(Integer gameID, Connection connect) throws DataAccessException {
        ResultSet result;
        int returnVal;

        try {
            result = db.executeQuery("SELECT COUNT(*) FROM Observers" + gameID.toString() + ";",
                    null, connect);
            if (result.next()) {
                returnVal = result.getInt("COUNT(*)");
            }
            else {
                throw new DataAccessException("Unable to get number of observers; try again");
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        return returnVal;
    }

    public ArrayList<String> getObservers(Integer gameID) throws DataAccessException {
        ResultSet result;
        ArrayList<String> usernames = new ArrayList<>();

        try (Connection connect = db.getNewConnection()) {
            result = db.executeQuery("SELECT username FROM Observers" + gameID.toString() + ";",
                    null, connect);
            while (result.next()) {
                usernames.add(result.getString("username"));
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return usernames;
    }
    /*
        public void updateObservers(Integer ID, ArrayList<String> newObserverList) throws SQLException {
            StringBuilder listString = new StringBuilder();
            int numOfNames = newObserverList.size();

            for (int i = 0; i < numOfNames; i++) {
                listString.append("(?)");
                if (i < numOfNames - 1) {
                    listString.append(", ");
                }
            }

            if (numOfNames > 0) {
                db.executeCommand("INSERT IGNORE INTO Observers" + ID.toString() +
                        " values" + listString + ';', newObserverList.toArray());
            }
        }
    */
    private boolean existsGameTable(Connection connect) throws DataAccessException {
        try {
            return db.executeQuery("SHOW TABLES LIKE 'games';", null, connect).next();
        }
        catch (SQLException x) {
            throw new DataAccessException(x.getMessage());
        }
    }

    private BoardObj constructBoard(ResultSet results) throws DataAccessException {
        BoardObj theBoard = new BoardObj(null, false);

        Position pos;
        PieceType type;
        Boolean isWhite = null;
        Boolean hasMoved = null;
        Boolean vulnerableToEnPassant = null;
        PieceObj piece;
        try {
            while (results.next()) {
                pos = new Position(results.getString("position"));
                type = PieceType.valueOf(results.getString("type"));
                isWhite = results.getBoolean("isWhite");

                if (type == PieceType.PAWN || type == PieceType.KING || type == PieceType.ROOK){
                    hasMoved = results.getBoolean("hasMoved");
                    if (type == PieceType.PAWN) {
                        vulnerableToEnPassant = results.getBoolean("vulnerableToEnPassant");
                    }
                    else {
                        vulnerableToEnPassant = null;
                    }
                }
                piece = constructPiece(pos, type, isWhite, hasMoved,
                        vulnerableToEnPassant, theBoard);
                theBoard.addPiece(pos, piece);

                if (piece.getPieceType() == PieceType.KING) {
                    theBoard.setKing((King)piece);
                }
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return theBoard;
    }

    private PieceObj constructPiece(Position pos, PieceType type, Boolean isWhite,
                                    Boolean hasMoved, Boolean vulnerableToEnPassant, BoardObj theBoard)
            throws DataAccessException {

        TeamColor color;
        if (isWhite) {
            color = TeamColor.WHITE;
        }
        else {
            color = TeamColor.BLACK;
        }

        switch (type) {
            case BISHOP:
                return new Bishop(theBoard, color, pos);
            case KING:
                return new King(theBoard, color, pos, hasMoved);
            case KNIGHT:
                return new Knight(theBoard, color, pos);
            case PAWN:
                return new Pawn(theBoard, color, pos, hasMoved, vulnerableToEnPassant);
            case QUEEN:
                return new Queen(theBoard, color, pos);
            case ROOK:
                return new Rook(theBoard, color, pos, hasMoved);
            default:
                throw new DataAccessException("PieceType is null");
        }
    }

    public String getPlayer(Integer gameID, TeamColor playerColor, Connection connect)
            throws DataAccessException {
        boolean noConnectionProvided = (connect == null);

        String username;
        ResultSet result;
        String column = null;
        if (playerColor == TeamColor.WHITE) {
            column = "whitePlayer";
        }
        else if (playerColor == TeamColor.BLACK) {
            column = "blackPlayer";
        }

        try {
            if (noConnectionProvided) {
                connect = db.getNewConnection();
            }

            result = db.executeQuery("SELECT " + column + " FROM games WHERE id = ?;",
                    new Integer[] {gameID}, connect);
            if (!result.next()) {
                throw new DataAccessException("Empty result; game not found");
            }
            username = result.getString(column);
            if (noConnectionProvided) {
                connect.close();
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        return username;
    }
    /**
     * Add a player as the given playerColor
     * @param username The username of the joining player
     * @param playerColor The color that the joining player wants to play as
     * @param gameID The game the player wants to join
     * @return True if game was found and playerColor was available, false otherwise
     */
    public Boolean addPlayer(String username, TeamColor playerColor, Integer gameID)
            throws DataAccessException {
        String player;
        int rowsAffected = 0;

        String column = null;
        if (playerColor == TeamColor.WHITE) {
            column = "whitePlayer";
        }
        else if (playerColor == TeamColor.BLACK) {
            column = "blackPlayer";
        }

        try (Connection connect = db.getNewConnection()) {
            if (playerColor == null) {
                rowsAffected = db.executeCommand("INSERT INTO Observers" + gameID.toString() + " values(?);",
                        new Object[] {username}, connect);
                return rowsAffected > 0;
            }
            player = getPlayer(gameID, playerColor, connect);

            if (player != null && !player.equals(username)) {
                throw new DataAccessException(playerTakenMsgStart + playerColor.toString() +
                        "; color already taken");
            }

            rowsAffected = db.executeCommand("UPDATE games SET " + column + " = ? WHERE id = ?",
                    new Object[] {username, gameID}, connect);
            db.executeCommand("DELETE FROM Observers" + gameID.toString() + " WHERE username = ?;",
                    new String[] {username});
            return rowsAffected > 0;
        }
        catch (SQLException e) {
            if (!e.getMessage().startsWith("Duplicate")) {
                throw new DataAccessException(e.getMessage());
            }
        }
        return rowsAffected > 0;
    }

    public Boolean removePlayer(String username, TeamColor playerColor, Integer gameID)
            throws DataAccessException {
        String player;
        int rowsAffected = 0;

        String column = null;
        if (playerColor == TeamColor.WHITE) {
            column = "whitePlayer";
        }
        else if (playerColor == TeamColor.BLACK) {
            column = "blackPlayer";
        }

        try (Connection connect = db.getNewConnection()) {
            if (playerColor == null) {
                removeObserver(gameID, username, connect);
                return true;
            }
            player = getPlayer(gameID, playerColor, connect);

            if (player != null && !player.equals(username)) {
                throw new DataAccessException(playerTakenMsgStart + playerColor.toString() +
                        "; color already taken");
            }

            rowsAffected = db.executeCommand("UPDATE games SET " + column + " = NULL WHERE id = ?",
                    new Object[] {gameID}, connect);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return rowsAffected > 0;
    }

    public void removeObserver(Integer gameID, String observerToRemove)
            throws DataAccessException {
        removeObserver(gameID, observerToRemove, null);
    }

    public void removeObserver(Integer gameID, String observerToRemove, Connection connect)
            throws DataAccessException {
        int rowsAffected;
        try {
            rowsAffected = db.executeCommand("DELETE FROM Observers" + gameID.toString() + " WHERE username = ?;",
                    new String[]{observerToRemove}, connect);
        }
        catch (SQLException x) {
            throw new DataAccessException(x.getMessage());
        }

        if (rowsAffected == 0) {
            throw new DataAccessException("Could not find observer to remove");
        }
    }
    /**
     * Delete all current games
     */
    public void clear() throws DataAccessException {
        ArrayList<Integer> gameIDs;
        ResultSet result;
        Integer currID;
        try (Connection connect = db.getNewConnection()) {
            result = db.executeQuery("SELECT id FROM games;", null, connect);
            while (result.next()) {
                currID = result.getInt("id");
                remove(currID); // Can't just clear the table; each record in the "games" table
                // has an "observers" table and "board" table associated with it; gotta
                // delete those or else you'll end up with a memory leak of sorts.
            }                   // The remove function will take care of that.
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        db.Games.clear();
    }
    /**
     * Removes the game with the given gameID from the database
     * @param gameID The ID number of the game to be removed
     */
    public void remove(Integer gameID) throws DataAccessException {
        int rowsAffected;
        var idArray = new Integer[] {gameID};

        try {
            rowsAffected = db.executeCommand("DELETE FROM games WHERE id = ?", idArray);
            db.executeCommand("DROP TABLE Observers" + gameID.toString() + ";", null);
            db.executeCommand("DROP TABLE Board" + gameID.toString() + ";", null);
            db.executeCommand("DROP TABLE Moves" + gameID.toString() + ";", null);
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }

        if (rowsAffected == 0) {
            throw new DataAccessException("Could not remove gameID " + gameID + "; not found");
        }
        db.Games.remove(gameID);
        takenIDs.remove(gameID);
    }

    public ArrayList<String> getMoves(Integer gameID) throws DataAccessException {
        ArrayList<String> moves = new ArrayList<>();
        try (Connection connect = db.getNewConnection()) {
            ResultSet countResult = db.executeQuery("SELECT move FROM Moves" + gameID +
                            " ORDER BY number;",
                    null, connect);
            while (countResult.next()) {
                moves.add(countResult.getString("move"));
            }

        }
        catch (SQLException x) {
            throw new DataAccessException(x.getMessage());
        }

        return moves;
    }

    public boolean addMove(String move, Integer gameID) throws DataAccessException {
        int rowsAffected;
        try (Connection connect = db.getNewConnection()) {
            rowsAffected = db.executeCommand("INSERT INTO Moves" + gameID.toString() + "(move) VALUES(?);",
                    new String[]{move}, connect);
        }
        catch (SQLException x) {
            throw new DataAccessException(x.getMessage());
        }
        return rowsAffected > 0;
    }
    /**
     * @return The first available ID
     */
    private int getNewID() {
        int ID;
        for (ID = 1; takenIDs.contains(ID); ID++) {}
        return ID;
    }

    private HashSet<Integer> initTakenIDs(Connection connect) throws DataAccessException {
        HashSet<Integer> takenIDs = new HashSet<>();
        ResultSet result;
        try {
            if (existsGameTable(connect)) {
                result = db.executeQuery("SELECT id FROM games;", null, connect);
            }
            else {
                result = null;
            }

            while (result != null && result.next()) {
                takenIDs.add(result.getInt("id"));
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return takenIDs;
    }

    private void updateBoard(String boardName, BoardObj board, Connection connect) throws DataAccessException {
        Position pos;
        String type;
        Boolean isWhite;
        var hasMoved = new NullableBoolean(null); // Created a new class so that when passed
        // as null in an object array, it can still be
        // recognized as a boolean
        var vulnerableToEnPassant = new NullableBoolean(null);

        try {
            db.executeCommand("TRUNCATE TABLE " + boardName + ";", null, connect);
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    pos = new Position(i, j);
                    PieceObj piece = (PieceObj) board.getPiece(pos);

                    if (piece != null) {
                        type = piece.getPieceType().toString();

                        isWhite = (piece.getTeamColor() == TeamColor.WHITE);

                        if (piece instanceof RKP) {
                            hasMoved.val = ((RKP)piece).getMovedStatus();
                        }
                        else {hasMoved.val = null;}

                        if (piece instanceof Pawn) {
                            vulnerableToEnPassant.val = ((Pawn)piece).getVulnerability();
                        }
                        else {vulnerableToEnPassant.val = null;}

                        db.executeCommand("INSERT INTO " + boardName + " VALUES(?, ?, ?, ?, ?);",
                                new Object[] {pos.toString(), type, isWhite,
                                        hasMoved, vulnerableToEnPassant}, connect);
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    final public String gameNotFoundMsg = "Empty result; game not found";
    final public String playerTakenMsgStart = "Couldn't add you as ";
    /**
     * @return The set of all ID numbers in use
     */
    private HashSet<Integer> getTakenIDs() {
        return new HashSet<Integer>(db.Games.keySet());
    }
    /**
     * Removes the game with the given gameID from the database
     * @param gameID The ID number of the game to be removed
     */
    private DBInterface db;
    //private HashMap<Integer, GameObj> games;
    private HashSet<Integer> takenIDs;
}