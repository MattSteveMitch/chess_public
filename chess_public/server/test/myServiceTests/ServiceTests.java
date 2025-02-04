package myServiceTests;
import org.junit.jupiter.api.*;
import server.Service.*;
import Responses.*;
import Requests.*;
import server.dataAccess.*;
import server.*;
import chess.ChessGame.TeamColor;
import java.sql.SQLException;
import models.Game;

public class ServiceTests {
    String defaultGameName;
    Server server;
    AuthDAO authDao;
    GameDAO gameDao;
    UserDAO userDao;
    ClearRequest goodClearReq;
    RegisterRequest goodRegisterReq1;

    RegisterRequest goodRegisterReq2;

    RegisterRequest redundantReq;
    String authToken;
    String invalidAuthToken;
    int currGameID;
    public ServiceTests() throws SQLException, DataAccessException  {
        defaultGameName = "Easy Pushover";
        server = new Server();
        authDao = server.getAuthDao();
        gameDao = server.getGameDao();
        userDao = server.getUserDao();
        goodClearReq = new ClearRequest();
        goodRegisterReq1 = new RegisterRequest("Ninja Nerd",
                "ArguileMcCollough", "mattstevemitch@gmail.com");

        goodRegisterReq2 = new RegisterRequest("Suave Samurai",
                "ArguileMcCollough", "Sam@thebomb.com");

        redundantReq = new RegisterRequest("Ninja Nerd",
                "password", "Joe@thebomb.com");
        invalidAuthToken = "3ji#(jD(l3jll_';";
    }

    public void setAuthToken(String token) {
        authToken = token;
    }

    public ClearResponse clear() {
        return Clear.clear(goodClearReq, authDao, userDao, gameDao);
    }

    public RegisterResponse register(RegisterRequest request, boolean fromScratch) {
        if (fromScratch) {clear();}

        RegisterResponse returnVal = Register.register(request, authDao, userDao);
        setAuthToken(returnVal.getAuthToken());

        return returnVal;
    }

    public CreateGameResponse createGame(String name, boolean succeed, boolean fromScratch) {
        if (fromScratch) {
            register(goodRegisterReq1, true);
        }

        CreateGameRequest request;
        if (succeed) {
            request = new CreateGameRequest(authToken, name);
        }
        else {
            request = new CreateGameRequest(invalidAuthToken, name);
        }
        return CreateGame.createGame(request, authDao, gameDao);
    }

    public JoinGameResponse joinGame(TeamColor playerColor, int gameID) { // Pass 0 as gameID to create a new game from scratch
        if (gameID == 0) {
            currGameID = createGame(defaultGameName, true, true).getID();
        }
        else {
            currGameID = gameID;
        }
        var request = new JoinGameRequest(authToken, currGameID, playerColor);
        return JoinGame.joinGame(request, authDao, gameDao);
    }

    public LogoutResponse logout(boolean succeed, boolean testFromScratch) {
        LogoutRequest request;

        if (testFromScratch) {register(goodRegisterReq1, true);}

        if (succeed) {request = new LogoutRequest(authToken);}
        else {request = new LogoutRequest(invalidAuthToken);}

        return Logout.logout(request, authDao);
    }

    public LoginResponse login(LoginRequest request, boolean testFromScratch) {
        if (testFromScratch) {
            logout(true, true);
        }
        if (request == null) {
            request = new LoginRequest(goodRegisterReq1.getUsername(),
                    goodRegisterReq1.getPassword());
        }

        var response = Login.login(request, authDao, userDao);
        setAuthToken(response.getAuthToken());
        return response;
    }

    @Test
    public void clearSuccessTest() {
        var response = clear();
        Assertions.assertEquals(200, response.getResponseCode(),
                "Clear test was not successful");
    }


    @Test
    public void RegisterSuccess() {
        var response = register(goodRegisterReq1, true);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Register test 1 was not successful");
        response = register(goodRegisterReq2, false);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Register test 2 was not successful");
    }
    @Test
    public void RegisterFailure() {
        register(goodRegisterReq1, true);
        var response = register(redundantReq, false);
        Assertions.assertEquals(403, response.getResponseCode(),
                "Register failure test returned wrong response code");
    }


    @Test
    public void createGameSuccess() {
        var response = createGame("Epic Showdown", true, true);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Create game test was not successful");
    }
    @Test
    public void createGameFailure() {
        var response = createGame("Epic Showdown", false, true);
        Assertions.assertEquals(401, response.getResponseCode(),
                "Create game failure test returned wrong response code");
    }


    @Test
    public void joinGameSuccess() {
        var response = joinGame(TeamColor.WHITE, 0);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Join game test 1 failed");

        response = joinGame(TeamColor.WHITE, currGameID);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Request not idempotent");

        logout(true, false);
        register(goodRegisterReq2, false);

        response = joinGame(TeamColor.BLACK, currGameID);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Join game test 2 failed");
    }
    @Test
    public void joinGameFailure() {
        var response = joinGame(TeamColor.WHITE, 0);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Couldn't join game");

        logout(true, false);
        register(goodRegisterReq2, false);

        response = joinGame(TeamColor.WHITE, currGameID);
        Assertions.assertEquals(403, response.getResponseCode(),
                "Wrong error code for WHITE already taken");
    }

    @Test
    public void leaveGameSuccess() throws DataAccessException {
        String myName = goodRegisterReq1.getUsername();
        int gameID = createGame("Game", true, true).getID();
        joinGame(null, gameID);

        Game foundGame = gameDao.find(gameID);
        Assertions.assertTrue(foundGame.getObservers().contains(myName));

        gameDao.removeObserver(gameID, myName);
        foundGame = gameDao.find(gameID);
        Assertions.assertFalse(foundGame.getObservers().contains(myName));

    }


    @Test
    public void ListGamesSuccess() {
        joinGame(TeamColor.BLACK, 0);
        int ID2 = createGame("Friendly Spar", true, false).getID();

        var request = new ListGamesRequest(authToken);
        var response = ListGames.listGames(request, authDao, gameDao);

        var game1 = response.getList().get(currGameID);
        var game2 = response.getList().get(ID2);

        Assertions.assertEquals(200, response.getResponseCode(), "Failed to list games");
        Assertions.assertNotEquals(game1.gameID, game2.gameID, "Games have the same ID");
        Assertions.assertEquals(ID2, game2.gameID, "ID for game 2 is not correct");

        Assertions.assertEquals(defaultGameName, game1.gameName, "Game 1 name is not correct");
        Assertions.assertEquals("Friendly Spar", game2.gameName, "Game 2 name is not correct");
        Assertions.assertNull(game2.whiteUsername, "Game 2 player usernames should be null");
        Assertions.assertNull(game2.blackUsername, "Game 2 player usernames should be null");
        Assertions.assertNull(game1.whiteUsername, "Game 1 white username should be null");
        Assertions.assertEquals(goodRegisterReq1.getUsername(), game1.blackUsername,
                "Game 1 black username is not correct");
    }

    @Test
    public void ListGamesFailure() {
        createGame("Friendly Spar", true, true).getID();

        var request = new ListGamesRequest(invalidAuthToken);
        var response = ListGames.listGames(request, authDao, gameDao);

        Assertions.assertEquals(401, response.getResponseCode(),
                "List games failure test returned wrong error code");
    }


    @Test
    public void LogoutSuccess() {
        var response = logout(true, true);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Logout test was not successful");

        var createGameResponse = createGame("Impossible deathtrap", true, false);
        Assertions.assertEquals(401, createGameResponse.getResponseCode(),
                "Create game should return 401 after logging out");
        response = logout(true, false);
        Assertions.assertEquals(401, response.getResponseCode(),
                "Should not be able to log out without logging in");

    }
    @Test
    public void LogoutFailure() {
        var response = logout(false, true);

        Assertions.assertEquals(401, response.getResponseCode(),
                "Logout failure test did not return correct error code");
    }


    @Test
    public void LoginSuccess() {
        var response = login(null, true);

        Assertions.assertEquals(200, response.getResponseCode(),
                "Failed to log in");

        CreateGameResponse createResponse = createGame("Friendly spar", true, false);
        Assertions.assertNotEquals(401, createResponse.getResponseCode(),
                "Unable to create game after login");

        LogoutResponse logoutResp = logout(true, false);
        Assertions.assertNotEquals(401, logoutResp.getResponseCode(),
                "Unable to logout after logging in");
    }
    @Test
    public void LoginFailure() {
        var request = new LoginRequest(goodRegisterReq1.getUsername(), "password");
        var response = login(request, true);

        Assertions.assertEquals(401, response.getResponseCode(),
                "Successful login with invalid credentials");
        Assertions.assertEquals("Error: Invalid password", response.getMessage(),
                "Error message is not correct");
    }
}

