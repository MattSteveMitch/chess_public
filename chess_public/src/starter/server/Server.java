package server;

import com.google.gson.Gson;
import java.util.HashMap;

import server.dataAccess.AuthDAO;
import server.dataAccess.DBInterface;
import server.dataAccess.GameDAO;
import Requests.*;
import Responses.BadRequestResponse;
import server.Service.*;
import server.dataAccess.UserDAO;
import spark.*;
import java.sql.SQLException;
import server.dataAccess.DataAccessException;

public class Server {
    public Server() throws SQLException, DataAccessException {
        database = new DBInterface();
        authDao = new AuthDAO(database);
        userDao = new UserDAO(database);
        gameDao = new GameDAO(database);
        serializer = new Gson();
    }

    public static void main(String[] args) throws SQLException, DataAccessException { // Question 1: where is the loop that keeps the server going?
        var server = new WSServer();
        Spark.port(8080);
        Spark.webSocket("/connect", server);

        Spark.externalStaticFileLocation("web");

        Spark.post("/game", server::createGame);
        Spark.post("/session", server::login);
        Spark.post("/user", server::register);
        Spark.put("/game", server::joinGame);
        Spark.get("/game", server::listGames);
    //    Spark.delete("/db", server::clear);
        Spark.delete("/session", server::logout);
        Spark.delete("/game/observer", server::leaveGame);
    }

    public String createGame(spark.Request request,    // Question 2: when/where are these objects created?
                             spark.Response response) {// Question 2b: What are they? What's their purpose?

        String header = request.headers("authorization");
        HashMap reqBody = serializer.fromJson(request.body(),
                HashMap.class);                        // Question 3: Apparently this still works if you just use Map instead of HashMap. How does it create a nonspecific Map if Map is an interface?

        return handler(header, reqBody, response, Requests.Request.RequestType.CREATE_GAME);
    }

    public String clear(spark.Request request,
                        spark.Response response) {
        return handler(null, new HashMap(), response,
                Requests.Request.RequestType.CLEAR);
    }

    public String joinGame(spark.Request request,
                           spark.Response response) {
        String header = request.headers("authorization");
        HashMap reqBody = serializer.fromJson(request.body(), HashMap.class);

        return handler(header, reqBody, response,
                Requests.Request.RequestType.JOIN_GAME);
    }

    public String listGames(spark.Request request,
                            spark.Response response) {
        String header = request.headers("authorization");

        return handler(header, new HashMap(), response,
                Requests.Request.RequestType.LIST_GAMES);
    }

    public String login(spark.Request request,
                        spark.Response response) {
        HashMap reqBody = serializer.fromJson(request.body(), HashMap.class);

        return handler(null, reqBody, response,
                Requests.Request.RequestType.LOGIN);
    }

    public String logout(spark.Request request,
                         spark.Response response) {
        String header = request.headers("authorization");

        return handler(header, new HashMap(), response,
                Requests.Request.RequestType.LOGOUT);
    }

    public String register(spark.Request request,
                           spark.Response response) {
        HashMap reqBody = serializer.fromJson(request.body(), HashMap.class);

        return handler(null, reqBody, response,
                Requests.Request.RequestType.REGISTER);
    }

    public String leaveGame(spark.Request request,
                             spark.Response response) {

        String header = request.headers("authorization");
        HashMap reqBody = serializer.fromJson(request.body(),
                HashMap.class);

        return handler(header, reqBody, response, Requests.Request.RequestType.LEAVE_GAME);
    }

    // Switch statement to call the right service class
    public Responses.Response getResponse(Requests.Request newReq) {
        Requests.Request.RequestType reqType = newReq.getType();
        switch (reqType) {
         //   case CLEAR:
           //     return Clear.clear(
             //           (ClearRequest)newReq, authDao, userDao, gameDao);
            case CREATE_GAME:
                return CreateGame.createGame(
                        (CreateGameRequest)newReq, authDao, gameDao);
            case JOIN_GAME:
                return JoinGame.joinGame(
                        (JoinGameRequest)newReq, authDao, gameDao);
            case LIST_GAMES:
                return ListGames.listGames(
                        (ListGamesRequest)newReq, authDao, gameDao);
            case LOGOUT:
                return Logout.logout(
                        (LogoutRequest)newReq, authDao);
            case LOGIN:
                return Login.login(
                        (LoginRequest)newReq, authDao, userDao);
            case REGISTER:
                return Register.register(
                        (RegisterRequest)newReq, authDao, userDao);
            case LEAVE_GAME:
                return LeaveGame.leaveGame(
                        (LeaveGameRequest)newReq, authDao, gameDao);
            default:
                return new BadRequestResponse(
                    "Congratulations, you broke the server in a way I didn't think possible");
        }
    }

    // Finishes building the Requests.Request object, then sends it off to the service class and
    // returns the response as a Json string
    public String handler(String reqHeader, HashMap reqBody, spark.Response response,
                          Requests.Request.RequestType reqType) {
        response.type("application/json");

        Requests.Request newReq = null;
        Responses.Response returnVal = null;

        try {
            newReq = requestBuilder(reqBody, reqHeader, reqType); // Build a Requests.Request object to
        }                                                         // send to the Service class
        catch (BadRequestException BRE) {
            returnVal = new BadRequestResponse(BRE.getMessage()); // If bad data was sent to the request builder,
        }                                                         // send back a Bad Request error

        if (newReq != null) {
            returnVal = getResponse(newReq);
        }

        response.body(returnVal.getMessage());
        response.status(returnVal.getResponseCode());   // set status code for response
        return serializer.toJson(returnVal);
    }

    // Takes Map representation of the request information and builds it into a Requests.Request
    Requests.Request requestBuilder(HashMap stub, String token,
                                           Requests.Request.RequestType reqType) throws BadRequestException {
        Requests.Request returnVal;

        switch (reqType) {
            case CREATE_GAME:
                returnVal = getCreateGameReq(stub, token);
                break;
            case CLEAR:
                returnVal = new ClearRequest();
                break;
            case JOIN_GAME:
                returnVal = getJoinGameReq(stub, token);
                break;
            case LIST_GAMES:
                if (token != null) {
                    returnVal = new ListGamesRequest(token);
                }
                else {
                    throw new BadRequestException("Error: No authorization token provided for list games");
                }
                break;

            case LOGIN:
                returnVal = getLoginReq(stub, token);
                break;

            case LOGOUT:
                if (token != null) {
                    returnVal = new LogoutRequest(token);
                }
                else {
                    throw new BadRequestException("Error: No authorization token provided for logout");
                }
                break;

            case REGISTER:
                returnVal = getRegisterReq(stub, token);
                break;

            case LEAVE_GAME:
                Object gameID = stub.get("gameID");
                if (gameID instanceof Double) {
                    returnVal = new LeaveGameRequest(token, ((Double)gameID).intValue());
                }
                else {
                    throw new BadRequestException("Error: Must specify game ID and username of observer to remove");
                }
                break;

            default:
                throw new BadRequestException("Error: Invalid request type");
        }

        return returnVal;
    }

    Requests.CreateGameRequest getCreateGameReq(HashMap stub, String token)
            throws BadRequestException {
        Object gameName = stub.get("gameName");
        if (gameName instanceof String && token != null) {
            return new CreateGameRequest(token, (String) gameName);
        }
        throw new BadRequestException("Error: Game name and authorization token required");
    }

    Requests.JoinGameRequest getJoinGameReq(HashMap stub, String token)
            throws BadRequestException {
        Requests.JoinGameRequest returnVal;

        Object gameID = stub.get("gameID");
        Object playerColor = stub.get("playerColor");
        if (gameID instanceof Double && playerColor instanceof String &&
                token != null) {
            returnVal = new JoinGameRequest(token, ((Double)gameID).intValue(),
                    chess.ChessGame.TeamColor.valueOf((String)playerColor));
        }
        else if (gameID instanceof Double && playerColor == null && token != null) {
            returnVal = new JoinGameRequest(token, ((Double)gameID).intValue(), null);
        }
        else {
            throw new BadRequestException(
                    "Error: Authorization token, game ID, and player color required");
        }

        return returnVal;
    }

    Requests.LoginRequest getLoginReq(HashMap stub, String token) throws BadRequestException {
        Object username = stub.get("username");
        Object password = stub.get("password");
        if (username instanceof String && password instanceof String) {
            return new LoginRequest((String)username, (String)password);
        }
        throw new BadRequestException("Error: Username and password required");
    }

    Requests.RegisterRequest getRegisterReq(HashMap stub, String token)
            throws BadRequestException {
        Object UserName = stub.get("username");
        Object PassWord = stub.get("password");
        Object email = stub.get("email");

        if (UserName instanceof String && PassWord instanceof String &&
                email instanceof String) {
            return new RegisterRequest((String)UserName, (String)PassWord, (String)email);
        }
        throw new BadRequestException("Error: Username, password, and email required");
    }

    public AuthDAO getAuthDao() {
        return authDao;
    }

    public GameDAO getGameDao() {
        return gameDao;
    }

    public UserDAO getUserDao() {
        return userDao;
    }

    private DBInterface database;
    AuthDAO authDao;
    UserDAO userDao;
    GameDAO gameDao;
    Gson serializer;
}

/*
{
  "username": "NinjaNerd",
  "password": "ArguileMcCollough",
  "email": "mattstevemitch@gmail.com"
}
*/