import org.junit.jupiter.api.*;
import Responses.*;
import Requests.*;
import ui.Client;
import ui.ServerFacadeException;
import ui.ServerFacade;
import chess.ChessGame.TeamColor;
import java.sql.SQLException;

public class ServerFacadeTests {
    ServerFacade serverF;
    String defaultGameName;
    ClearRequest goodClearReq;
    RegisterRequest goodRegisterReq1;

    RegisterRequest goodRegisterReq2;
    LoginRequest goodLoginReq1;
    LoginRequest goodLoginReq2;

    RegisterRequest redundantReq;
    String authToken;
    String invalidAuthToken;
    int currGameID;
    public ServerFacadeTests() throws ServerFacadeException {
        defaultGameName = "Easy Pushover";
        serverF = new ServerFacade();
        goodClearReq = new ClearRequest();
        goodRegisterReq1 = new RegisterRequest("Ninja Nerd",
                "ArguileMcCollough", "mattstevemitch@gmail.com");

        goodRegisterReq2 = new RegisterRequest("Suave Samurai",
                "ArguileMcCollough", "Sam@thebomb.com");
        goodLoginReq1 = new LoginRequest(goodRegisterReq1.getUsername(), goodRegisterReq1.getPassword());
        goodLoginReq2 = new LoginRequest(goodRegisterReq2.getUsername(), goodRegisterReq2.getPassword());

        redundantReq = new RegisterRequest("Ninja Nerd",
                "password", "Joe@thebomb.com");
        invalidAuthToken = "3ji#(jD(l3jll_';";
    }

    public void setAuthToken(String token) {
        authToken = token;
    }

    public ClearResponse clear() throws ServerFacadeException {
        return serverF.clear();
    }

    public RegisterResponse register(RegisterRequest request, boolean fromScratch)
            throws ServerFacadeException {
        if (fromScratch) {clear();}

        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();

        RegisterResponse returnVal = serverF.register(username, password, email);
        setAuthToken(returnVal.getAuthToken());

        return returnVal;
    }

    public CreateGameResponse createGame(String name, boolean succeed, boolean fromScratch)
            throws ServerFacadeException {
        if (fromScratch) {
            register(goodRegisterReq1, true);
        }

        String token;
        if (succeed) {
            token = authToken;
        }
        else {
            token = invalidAuthToken;
        }
        return serverF.createGame(token, name);
    }

    public JoinGameResponse joinGame(TeamColor playerColor, int gameID)
            throws ServerFacadeException { // Pass 0 as gameID to create a new game from scratch
        if (gameID == 0) {
            currGameID = createGame(defaultGameName, true, true).getID();
        }
        else {
            currGameID = gameID;
        }

        return serverF.joinGame(authToken, currGameID, playerColor);
    }

    public LogoutResponse logout(boolean succeed, boolean testFromScratch)
            throws ServerFacadeException {
        String token;

        if (testFromScratch) {register(goodRegisterReq1, true);}

        if (succeed) {token = authToken;}
        else {token = invalidAuthToken;}

        return serverF.logout(token);
    }

    public LoginResponse login(LoginRequest request, boolean testFromScratch) throws ServerFacadeException {
        if (testFromScratch) {
            logout(true, true);
        }

        if (request == null) {
            request = new LoginRequest(goodRegisterReq1.getUsername(),
                    goodRegisterReq1.getPassword());
        }

        String username = request.getUsername();
        String password = request.getPassword();

        var response = serverF.login(username, password);
        setAuthToken(response.getAuthToken());
        return response;
    }

    @Test
    public void clearSuccessTest() throws ServerFacadeException {
        var response = clear();
        Assertions.assertEquals(200, response.getResponseCode(),
                "Clear test was not successful");
    }

    @Test
    public void RegisterSuccess() throws ServerFacadeException {
        var response = register(goodRegisterReq1, true);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Register test 1 was not successful");
        response = register(goodRegisterReq2, false);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Register test 2 was not successful");
    }
    @Test
    public void RegisterFailure() throws ServerFacadeException {
        RegisterResponse response = null;
        register(goodRegisterReq1, true);

        response = register(redundantReq, false);

        Assertions.assertEquals(403, response.getResponseCode(),
                "Register failure test returned wrong response code");
    }


    @Test
    public void createGameSuccess() throws ServerFacadeException {
        var response = createGame("Epic Showdown", true, true);
        Assertions.assertEquals(200, response.getResponseCode(),
                "Create game test was not successful");
    }
    @Test
    public void createGameFailure() throws ServerFacadeException {
        var response = createGame("Epic Showdown", false, true);

        Assertions.assertEquals(401, response.getResponseCode(),
                "Create game failure test returned wrong response code");
    }


    @Test
    public void joinGameSuccess() throws ServerFacadeException {
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
    public void joinGameFailure() throws ServerFacadeException {
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
    public void ListGamesSuccess() throws ServerFacadeException {
        joinGame(TeamColor.BLACK, 0);
        int ID2 = createGame("Friendly Spar", true, false).getID();

        var response = serverF.listGames(authToken);

        Assertions.assertEquals(200, response.getResponseCode(), "Failed to list games");

        var game1 = response.getList().get(currGameID);
        var game2 = response.getList().get(ID2);

        Assertions.assertNotEquals(game1.gameID, game2.gameID, "Games have the same ID");
        //Assertions.assertEquals(ID2, game2.gameID, "ID for game 2 is not correct");

        Assertions.assertEquals(defaultGameName, game1.gameName, "Game 1 name is not correct");
        Assertions.assertEquals("Friendly Spar", game2.gameName, "Game 2 name is not correct");
        Assertions.assertNull(game2.whiteUsername, "Game 2 player usernames should be null");
        Assertions.assertNull(game2.blackUsername, "Game 2 player usernames should be null");
        Assertions.assertNull(game1.whiteUsername, "Game 1 white username should be null");
        Assertions.assertEquals(goodRegisterReq1.getUsername(), game1.blackUsername,
                "Game 1 black username is not correct");
    }

    @Test
    public void ListGamesFailure() throws ServerFacadeException {
        createGame("Friendly Spar", true, true).getID();

        var response = serverF.listGames(invalidAuthToken);

        Assertions.assertEquals(401, response.getResponseCode(),
                "List games failure test returned wrong error code");
    }


    @Test
    public void LogoutSuccess() throws ServerFacadeException {
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
    public void LogoutFailure() throws ServerFacadeException {
        var response = logout(false, true);

        Assertions.assertEquals(401, response.getResponseCode(),
                "Logout failure test did not return correct error code");
    }


    @Test
    public void LoginSuccess() throws ServerFacadeException {
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
    public void LoginFailure() throws ServerFacadeException {
        int errorCode = -1;
        var request = new LoginRequest(goodRegisterReq1.getUsername(), "password");

        var response = login(request, true);

        Assertions.assertEquals(401, response.getResponseCode(),
                "Successful login with invalid credentials");
        Assertions.assertEquals("Error: Invalid password", response.getMessage(),
                "Error message is not correct");
    }

    @Test
    public void leaveGameSuccess() throws ServerFacadeException {
        joinGame(null, 0);
        int registeredSecond = register(goodRegisterReq2, false).getResponseCode();
        Assertions.assertEquals(200, registeredSecond);

        logout(true, false);
        login(goodLoginReq2, false);
        joinGame(null, currGameID);

        var games = serverF.listGames(authToken).getList();
        var observers = games.get(currGameID).observers;
        Assertions.assertEquals(2, observers);
        logout(true, false);
        login(goodLoginReq1, false);

        serverF.leaveGame(authToken, currGameID);
        games = serverF.listGames(authToken).getList();
        observers = games.get(currGameID).observers;
        Assertions.assertEquals(1, observers);

        logout(true, false);
        login(goodLoginReq2, false);

        serverF.leaveGame(authToken, currGameID);
        games = serverF.listGames(authToken).getList();
        observers = games.get(currGameID).observers;
        Assertions.assertEquals(0, observers);
    }

    @Test
    public void leaveGameFailure() throws ServerFacadeException {
        joinGame(null, 0);

        var result = serverF.leaveGame(authToken, 32);

        Assertions.assertTrue(result.getMessage().startsWith("Table 'chess.observers32'"));

        result = serverF.leaveGame(invalidAuthToken, currGameID);

        Assertions.assertEquals("Invalid authorization token", result.getMessage());
     //   Assertions.assertFalse(success);

        serverF.leaveGame(authToken, currGameID);
        result = serverF.leaveGame(authToken, currGameID);

        Assertions.assertTrue(result.getMessage().startsWith("Could not find"));
    }

}

