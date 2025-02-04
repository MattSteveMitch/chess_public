package passoffTests.serverTests;

import chess.ChessGame.*;
import chess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;
import Requests.CreateGameRequest;
import Requests.JoinGameRequest;
import Requests.RegisterRequest;
import Responses.ClearResponse;
import Requests.ClearRequest;
import Responses.CreateGameResponse;
import Responses.JoinGameResponse;
import Responses.RegisterResponse;
import server.Server;
import server.Service.Clear;
import server.Service.CreateGame;
import server.Service.JoinGame;
import server.Service.Register;
import server.dataAccess.DataAccessException;
import server.dataAccess.*;
import java.sql.SQLException;
import java.util.ArrayList;
import models.User;
import models.Game;
import models.GameBasicInfo;

public class DAOTests {
    public DAOTests() throws DataAccessException, SQLException {
        server = new Server();

        gameDao = server.getGameDao();
        userDao = server.getUserDao();
        authDao = server.getAuthDao();
    }

    public void insertUser(String username, String password, String email) throws DataAccessException {
        userDao.insert(username, password, email);
    }

    @BeforeEach
    public void clearUsers() throws DataAccessException {
        userDao.clear();
        authDao.clear();
        gameDao.clear();
    }

    public ClearResponse clear() {
        return Clear.clear(new ClearRequest(), authDao, userDao, gameDao);
    }

    public RegisterResponse register(RegisterRequest request, boolean fromScratch) {
        if (fromScratch) {clear();}

        RegisterResponse returnVal = Register.register(request, authDao, userDao);
        authToken = returnVal.getAuthToken();

        return returnVal;
    }

    private void playGame(GameObj game) throws InvalidMoveException { // Make a ton of moves and make
                              // sure the game board updates correctly and none of the updates get
                              // "lost in translation" from Java to SQL database
        game.makeMove(new MoveObj(new Position(2, 6), new Position(3, 6)));
        game.makeMove(new MoveObj(new Position(7, 8), new Position(5,8)));
        game.makeMove(new MoveObj(new Position(2, 7), new Position(4,7)));
        game.makeMove(new MoveObj(new Position(5, 8), new Position(4,8)));
        game.makeMove(new MoveObj(new Position(4, 7), new Position(5,7)));
        game.makeMove(new MoveObj(new Position(4, 8), new Position(3,8)));
        game.makeMove(new MoveObj(new Position(2, 3), new Position(3,3)));
        game.makeMove(new MoveObj(new Position(7, 3), new Position(5,3)));
        game.makeMove(new MoveObj(new Position(2, 5), new Position(4,5)));
        game.makeMove(new MoveObj(new Position(5, 3), new Position(4,3)));
        game.makeMove(new MoveObj(new Position(2, 2), new Position(4,2)));
        game.makeMove(new MoveObj(new Position(4, 3), new Position(3,2)));
        game.makeMove(new MoveObj(new Position(4, 5), new Position(5,5)));
        game.makeMove(new MoveObj(new Position(7, 6), new Position(5,6)));
        game.makeMove(new MoveObj(new Position(5, 5), new Position(6,6)));
        game.makeMove(new MoveObj(new Position(7, 7), new Position(6,6)));
        game.makeMove(new MoveObj(new Position(1, 6), new Position(3,8)));
        game.makeMove(new MoveObj(new Position(8, 6), new Position(6,8)));
        game.makeMove(new MoveObj(new Position(3, 8), new Position(4,7)));
        game.makeMove(new MoveObj(new Position(6, 6), new Position(5,7)));
        game.makeMove(new MoveObj(new Position(4, 7), new Position(5,8)));
        game.makeMove(new MoveObj(new Position(8, 5), new Position(8,6)));
        game.makeMove(new MoveObj(new Position(2, 1), new Position(4,1)));
        game.makeMove(new MoveObj(new Position(8, 4), new Position(6,2)));
        game.makeMove(new MoveObj(new Position(4, 1), new Position(5,1)));
        game.makeMove(new MoveObj(new Position(6, 2), new Position(4,4)));
        game.makeMove(new MoveObj(new Position(1, 7), new Position(3,8)));
        game.makeMove(new MoveObj(new Position(4, 4), new Position(3,3)));
        game.makeMove(new MoveObj(new Position(1, 5), new Position(1,7), true));
        game.makeMove(new MoveObj(new Position(8, 6), new Position(7,7)));
        game.makeMove(new MoveObj(new Position(2, 4), new Position(3,3)));
        game.makeMove(new MoveObj(new Position(3, 2), new Position(2,2)));
        game.makeMove(new MoveObj(new Position(3, 3), new Position(4,3)));
        game.makeMove(new MoveObj(new Position(2, 2), new Position(1,1), ChessPiece.PieceType.BISHOP));
    }

    @Test
    public void clearUsersTest() throws DataAccessException {
        userDao.insert("username", "password", "email@email.com");
        userDao.insert("me", "aleimals", "joe@gmail.com");
        userDao.insert("somebody!", "abracadabra", "mike@gmail.com");
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");

        clearUsers();
        User user1 = userDao.find("somebody!");
        Assertions.assertNull(user1);
        Assertions.assertNull(userDao.find("me"));
        Assertions.assertNull(userDao.find("elmo"));
        Assertions.assertNull(userDao.find("username"));
    }

    @Test
    public void findUserSuccess() throws DataAccessException {
        userDao.insert("username", "password", "email@email.com");
        userDao.insert("me", "aleimals", "joe@gmail.com");
        userDao.insert("somebody!", "abracadabra", "mike@gmail.com");
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");

        User user1 = userDao.find("somebody!");
        Assertions.assertEquals("abracadabra", user1.getPassword());
        Assertions.assertEquals("aleimals", userDao.find("me").getPassword());
        Assertions.assertEquals("mike@gmail.com", user1.getEmail());
        Assertions.assertEquals("elmo@sesame.com", userDao.find("elmo").getEmail());
        Assertions.assertNull(userDao.find("aljer"));
        Assertions.assertNull(userDao.find("wejalw"));
    }

    @Test
    public void findUserFail() throws DataAccessException {
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");

        Assertions.assertEquals("elmo@sesame.com", userDao.find("elmo").getEmail());
        userDao.delete("elmo");
        Assertions.assertNull(userDao.find("elmo"));
    }

    @Test
    public void deleteUserSuccess() throws DataAccessException {
        userDao.insert("username", "password", "email@email.com");
        userDao.insert("me", "aleimals", "joe@gmail.com");
        userDao.insert("somebody!", "abracadabra", "mike@gmail.com");
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");

        Assertions.assertEquals("elmo@sesame.com", userDao.find("elmo").getEmail());
        userDao.delete("elmo");
        User user1 = userDao.find("somebody!");
        Assertions.assertEquals("abracadabra", user1.getPassword());
        Assertions.assertEquals("aleimals", userDao.find("me").getPassword());
        Assertions.assertEquals("mike@gmail.com", user1.getEmail());
        Assertions.assertNull(userDao.find("elmo"));
    }

    @Test
    public void deleteUserFail() throws DataAccessException {
        boolean success = true;
        try {
            userDao.delete("elmo");
        }
        catch (DataAccessException e) {
            success = false;
        }
        Assertions.assertFalse(success);
    }

    @Test
    public void addTokenSuccess() throws DataAccessException {
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");

        authToken = authDao.insert("elmo").toString();
        authDao.delete(authToken); // No assertion required; as long as no exception was
                                   // thrown, we're good
    }
/*
    @Test
    public void addTokenFail() throws DataAccessException {
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");

        authToken = authDao.insert("elmo").toString();
        String newToken = authDao.insert("elmo").toString();

        Assertions.assertEquals(authToken, newToken); // There isn't really a fail case for this method,
    }                                                 // so instead I just check that it doesn't create
                                                      // a new token when you call it the second time
                                                      // with the same username
    */
    @Test
    public void addUserSuccess() throws DataAccessException {
        boolean success = true;
        try {                                                   // Already taken username
            userDao.insert("elmo", "bigBird", "grover@sesame.com");
        }
        catch (DataAccessException e) {
            success = false;
        }

        Assertions.assertTrue(success);
    }

    @Test
    public void addUserFail() throws DataAccessException {
        boolean success = true;
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");
        try {                                                   // Already taken username
            userDao.insert("elmo", "bigBird", "grover@sesame.com");
        }
        catch (DataAccessException e) {
            success = false;
        }

        Assertions.assertFalse(success);
        success = true;

        try {                                                       // Already taken email
            userDao.insert("bigBird", "bigBird", "elmo@sesame.com");
        }
        catch (DataAccessException e) {
            success = false;
        }
        Assertions.assertFalse(success);
    }

    @Test
    public void matchTokenSuccess() throws DataAccessException {
        userDao.insert("Sharky", "chomp", "greatwhite@ocean.com");

        authToken = authDao.insert("Sharky").toString();
        String username = authDao.matchToken(authToken);
        Assertions.assertEquals("Sharky", username);
    }

    @Test
    public void matchTokenFailure() throws DataAccessException {
        boolean success = true;
        userDao.insert("Sharky", "chomp", "greatwhite@ocean.com");

        authToken = authDao.insert("Sharky").toString();

        String username = authDao.matchToken("je93mwldk389)0J*");

        Assertions.assertNull(username);
    }

    @Test
    public void deleteTokenSuccess() throws DataAccessException {
        var success = true;
        userDao.insert("Sharky", "chomp", "greatwhite@ocean.com");

        authToken = authDao.insert("Sharky").toString();
        String username = authDao.matchToken(authToken);
        authDao.delete(authToken);

        username = authDao.matchToken(authToken);

        Assertions.assertNull(username);
    }

    @Test
    public void deleteTokenFailure() throws DataAccessException {
        var success = true;
        userDao.insert("Sharky", "chomp", "greatwhite@ocean.com");

        authToken = authDao.insert("Sharky").toString();
        String username = authDao.matchToken(authToken);
        try {
            authDao.delete("je93mwldk389)0J*");
        }
        catch (DataAccessException e) {
            success = false;
        }

        Assertions.assertFalse(success);
    }

    @Test
    public void clearTokensSuccess() throws DataAccessException {
        userDao.insert("username", "password", "email@email.com");
        String token1 = authDao.insert("username").toString();
        userDao.insert("me", "aleimals", "joe@gmail.com");
        String token2 = authDao.insert("me").toString();
        userDao.insert("somebody!", "abracadabra", "mike@gmail.com");
        String token3 = authDao.insert("somebody!").toString();

        authDao.clear();
        Assertions.assertNull(authDao.matchToken(token1));
        Assertions.assertNull(authDao.matchToken(token2));
        Assertions.assertNull(authDao.matchToken(token3));
    }

    @Test
    public void insertGameSuccess() throws DataAccessException {

        int gameID = gameDao.insert("Epic Showdown");
        int gameID2 = gameDao.insert("World Championship");
        Assertions.assertTrue(gameDao.exists(gameID));
        Assertions.assertTrue(gameDao.exists(gameID2));
        gameID = gameDao.insert("another one");
        Assertions.assertTrue(gameDao.exists(gameID));
    }

    @Test
    public void insertGameFailure() throws DataAccessException {
        boolean success = true;
        try {
            gameDao.insert(null);
        }
        catch (DataAccessException e) {
            success = false;
        }

        Assertions.assertFalse(success);
        Assertions.assertFalse(gameDao.exists(3892));
    }

    @Test
    public void findGameSuccess() throws DataAccessException {
        int gameID1 = gameDao.insert("Epic Showdown");
        int gameID2 = gameDao.insert("World Championship");

        var game1 = gameDao.find(gameID1);
        var game2 = gameDao.find(gameID2);

        Assertions.assertNotNull(game1);
        Assertions.assertNotNull(game2);
        Assertions.assertEquals("Epic Showdown", game1.getName());
        Assertions.assertEquals("World Championship", game2.getName());
    }

    @Test
    public void findGameFailure() throws DataAccessException {
        int gameID1 = gameDao.insert("Epic Showdown");
        int gameID2 = gameDao.insert("World Championship");

        Assertions.assertNull(gameDao.find(876));
    }

    @Test
    public void removeGameSuccess() throws DataAccessException {
        int gameID1 = gameDao.insert("Epic Showdown");
        int gameID2 = gameDao.insert("World Championship");

        gameDao.remove(gameID2);
        Assertions.assertNull(gameDao.find(gameID2));
    }

    @Test
    public void removeGameFailure() throws DataAccessException {
        int gameID1 = gameDao.insert("Epic Showdown");
        int gameID2 = gameDao.insert("World Championship");
        boolean success = true;

        try {
            gameDao.remove(765);
        }
        catch (DataAccessException x) {
            success = false;
        }
        Assertions.assertFalse(success);
    }


    @Test
    public void addObserverSuccess() throws DataAccessException {
        int gameID = gameDao.insert("Epic Showdown");

        var username = "watcher1";
        var username2 = "spectator";

        gameDao.addPlayer(username, null, gameID);
        gameDao.addPlayer(username2, null, gameID);
        var game = gameDao.find(gameID);
        var observers = game.getObservers();

        Assertions.assertTrue(observers.contains(username));
        Assertions.assertTrue(observers.contains(username2));
    }

    @Test
    public void addObserverFailure() throws DataAccessException {
        int gameID1 = gameDao.insert("Epic Showdown");
        int gameID2 = gameDao.insert("World Chanpionship");

        var username = "watcher1";
        var username2 = "spectator";

        var game = gameDao.find(gameID1);
        var observers = game.getObservers();

        Assertions.assertTrue(observers.isEmpty());

        boolean success = true;

        try {
            gameDao.addPlayer("John", null, 43362);
        }
        catch (DataAccessException x) {
            success = false;
        }
        Assertions.assertFalse(success);
    }

    @Test
    public void leaveGameSuccess() throws DataAccessException {
        userDao.insert("username", "password", "email@email.com");
        userDao.insert("me", "aleimals", "joe@gmail.com");
        userDao.insert("somebody!", "abracadabra", "mike@gmail.com");
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");

        int gameID = gameDao.insert("Observer removal test");
        int gameID2 = gameDao.insert("test2");

        gameDao.addPlayer("username", null, gameID);
        gameDao.addPlayer("elmo", null, gameID);
        gameDao.addPlayer("somebody!", null, gameID);

        gameDao.addPlayer("somebody!", null, gameID2);
        gameDao.addPlayer("elmo", null, gameID2);
        gameDao.addPlayer("somebody!", null, gameID2);
        gameDao.addPlayer("me", null, gameID2);
        gameDao.addPlayer("username", null, gameID2);

        gameDao.removeObserver(gameID, "elmo");
        gameDao.removeObserver(gameID2, "somebody!");
        gameDao.removeObserver(gameID2, "me");
        gameDao.removeObserver(gameID2, "elmo");

        var observerlist1 = gameDao.find(gameID).getObservers();
        var observerlist2 = gameDao.find(gameID2).getObservers();

        Assertions.assertTrue(observerlist1.contains("username"));
        Assertions.assertTrue(observerlist1.contains("somebody!"));
        Assertions.assertEquals(2, observerlist1.size());

        Assertions.assertTrue(observerlist2.contains("username"));
        Assertions.assertEquals(1, observerlist2.size());
    }

    @Test
    public void leaveGameFailure() throws DataAccessException {
        boolean success = true;
        userDao.insert("username", "password", "email@email.com");
        userDao.insert("elmo", "myWorld", "elmo@sesame.com");
        userDao.insert("me", "aleimals", "joe@gmail.com");

        int gameID = gameDao.insert("Observer removal test");

        gameDao.addPlayer("username", null, gameID);
        gameDao.addPlayer("elmo", null, gameID);
        try {
            gameDao.removeObserver(32, "elmo");
        }
        catch (DataAccessException e) {
            success = false;
        }
        Assertions.assertFalse(success);

        success = true;

        try {
            gameDao.removeObserver(gameID, "me");
        }
        catch (DataAccessException e) {
            success = false;
        }
        Assertions.assertFalse(success);
    }

    @Test
    public void addPlayerSuccess() throws DataAccessException {
        int gameID = gameDao.insert("Epic Showdown");

        var username = "watcher1";
        var username2 = "spectator";

        gameDao.addPlayer(username, TeamColor.BLACK, gameID);
        gameDao.addPlayer(username2, TeamColor.WHITE, gameID);
        var game = gameDao.find(gameID);
        var white = game.getPlayer(TeamColor.WHITE);
        var black = game.getPlayer(TeamColor.BLACK);

        Assertions.assertEquals(username, black);
        Assertions.assertEquals(username2, white);
    }

    @Test
    public void addPlayerFailure() throws DataAccessException {
        int gameID = gameDao.insert("Epic Showdown");

        var username = "watcher1";
        var username2 = "spectator";

        var game = gameDao.find(gameID);

        Assertions.assertNull(game.getPlayer(TeamColor.WHITE));
        Assertions.assertNull(game.getPlayer(TeamColor.BLACK));

        boolean success = true;
        try {
            gameDao.addPlayer(username, TeamColor.BLACK, 97);
        }
        catch (DataAccessException x) {
            success = false;
        }
        Assertions.assertFalse(success);

        gameDao.addPlayer(username, TeamColor.BLACK, gameID);
        success = true;
        try {
            gameDao.addPlayer(username2, TeamColor.BLACK, gameID);
        }
        catch (DataAccessException x) {
            System.out.println(x.getMessage());
            success = false;
        }
        Assertions.assertFalse(success);

        gameDao.addPlayer(username, TeamColor.WHITE, gameID);
        success = true;
        try {
            gameDao.addPlayer(username2, TeamColor.WHITE, gameID);
        }
        catch (DataAccessException x) {
            System.out.println(x.getMessage());
            success = false;
        }
        Assertions.assertFalse(success);
    }

    @Test
    public void updateGameSuccess() throws DataAccessException, InvalidMoveException, SQLException {
        int gameID = gameDao.insert("Epic Showdown");
        var gameRecord = gameDao.find(gameID);

        var username = "GM";
        var username2 = "ChessNutBoastingInAnOpenFoyer";
        var username3 = "spectator";
        var username4 = "heckler";
        var username5 = "toxic fan";
        var username6 = "fan";

        gameRecord.setPlayer(username, TeamColor.BLACK);
        gameRecord.setPlayer(username2, TeamColor.WHITE);

        gameDao.addPlayer(username3, null, gameID);
        gameDao.addPlayer(username4, null, gameID);
        gameDao.addPlayer(username5, null, gameID);
        gameDao.addPlayer(username6, null, gameID);

        var gameStatus = gameRecord.getStatus();
        playGame(gameStatus);
        gameDao.removeObserver(gameID, username5);

        gameDao.updateGameRecord(gameRecord);
       // gameDao.updateObservers(gameID, gameRecord.getObservers());

        var newGameRecord = gameDao.find(gameID);
        PieceObj shouldBePawn = (PieceObj)newGameRecord.getStatus().getBoard().getPiece(
                new Position(3, 3));
        PieceObj shouldBeKnight = (PieceObj)newGameRecord.getStatus().getBoard().getPiece(
                new Position(6, 1));

        var observers = newGameRecord.getObservers();

        Assertions.assertTrue(newGameRecord.getStatus().getBoard().equals(gameStatus.getBoard()));
        Assertions.assertEquals(username, newGameRecord.getPlayer(TeamColor.BLACK));
        Assertions.assertEquals(username2, newGameRecord.getPlayer(TeamColor.WHITE));
        Assertions.assertTrue(observers.contains(username6));
        Assertions.assertFalse(observers.contains(username5));
        Assertions.assertTrue(observers.contains(username3));
        Assertions.assertTrue(observers.contains(username4));
        Assertions.assertEquals(3, observers.size());
        Assertions.assertEquals(TeamColor.WHITE, newGameRecord.getStatus().getTeamTurn());
    }

    @Test
    public void updateGameFailure() throws DataAccessException {
        int gameID = gameDao.insert("Epic Showdown");
        boolean success = true;

        Game newGame = new Game(362, "invalidGame", new GameObj(), new ArrayList<String>());
        try {
            gameDao.updateGameRecord(newGame);
        }
        catch (DataAccessException e) {
            success = false;
        }
        Assertions.assertFalse(success);
    }

    @Test
    public void getListSuccess() throws DataAccessException {
        int gameID1 = gameDao.insert("Epic Showdown");
        int gameID2 = gameDao.insert("World Chanpionship");

        gameDao.addPlayer("Greg", TeamColor.WHITE, gameID1);
        gameDao.addPlayer("floob", TeamColor.BLACK, gameID2);

        var gameList = gameDao.getAll();
        GameBasicInfo game1 = null;
        GameBasicInfo game2 = null;

        for (var thisGame : gameList) {
            if (thisGame.gameID == 1) {
                game1 = thisGame;
            }
            if (thisGame.gameID == 2) {
                game2 = thisGame;
            }
        }
        Assertions.assertNotNull(game1);
        Assertions.assertNotNull(game2);

        Assertions.assertEquals("floob", game2.blackUsername);
        Assertions.assertNull(game2.whiteUsername);
        Assertions.assertEquals("Greg", game1.whiteUsername);
        Assertions.assertNull(game1.blackUsername);
        Assertions.assertEquals("Epic Showdown", game1.gameName);
        Assertions.assertEquals("World Chanpionship", game2.gameName);
    }

    @Test
    public void existencePositive() throws DataAccessException {
        gameDao.insert("New Game");
        int ID = gameDao.insert("Epic Showdown");
        Assertions.assertTrue(gameDao.exists(ID));
    }

    @Test
    public void existenceNegative() throws DataAccessException {
        gameDao.insert("New Game");
        int ID = gameDao.insert("Epic Showdown");
        gameDao.remove(ID);
        Assertions.assertFalse(gameDao.exists(ID));
        Assertions.assertFalse(gameDao.exists(235));
    }

    @Test
    public void clearGamesSuccess() throws DataAccessException {
        gameDao.insert("New Game");
        gameDao.insert("Epic Showdown");
        gameDao.insert("World Championship");
        gameDao.insert("Friendly Spar");
        Assertions.assertFalse(gameDao.getAll().isEmpty());

        gameDao.clear();
        Assertions.assertTrue(gameDao.getAll().isEmpty());
    }

    /*
    @Test
    public void updateSuccess() throws DataAccessException {
        int gameID = gameDao.insert("Epic Showdown");

        var username = "watcher1";
        var username2 = "spectator";

        try {
            gameDao.addPlayer(username, TeamColor.BLACK, 97);
        }
        catch (DataAccessException x) {
            success = false;
        }
        Assertions.assertFalse(success);

        gameDao.addPlayer(username, TeamColor.BLACK, gameID);
        success = true;
        try {
            gameDao.addPlayer(username2, TeamColor.BLACK, gameID);
        }
        catch (DataAccessException x) {
            System.out.println(x.getMessage());
            success = false;
        }
        Assertions.assertFalse(success);

        gameDao.addPlayer(username, TeamColor.WHITE, gameID);
        success = true;
        try {
            gameDao.addPlayer(username2, TeamColor.WHITE, gameID);
        }
        catch (DataAccessException x) {
            System.out.println(x.getMessage());
            success = false;
        }
        Assertions.assertFalse(success);
    }
//*/
    RegisterRequest goodRegisterReq1 = new RegisterRequest("Ninja Nerd",
            "ArguileMcCollough", "mattstevemitch@gmail.com");

    RegisterRequest goodRegisterReq2 = new RegisterRequest("Suave Samurai",
            "ArguileMcCollough", "Sam@thebomb.com");

    RegisterRequest redundantReq = new RegisterRequest("Ninja Nerd",
            "password", "Joe@thebomb.com");
    String authToken;
    String invalidAuthToken = "3ji#(jD(l3jll_';";
    AuthDAO authDao;
    UserDAO userDao;
    GameDAO gameDao;
    Server server;
}