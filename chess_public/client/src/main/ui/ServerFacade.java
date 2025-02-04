package ui;

import java.net.URI;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.io.IOException;
import Requests.*;
import Responses.*;
import chess.ChessGame.TeamColor;
import chess.*;
import java.util.HashMap;
import com.google.gson.Gson;
import javax.websocket.*;
import javax.websocket.server.*;
import WebSocketMsg.fromClient.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ServerFacade extends Endpoint {
    final private String URLStub = "http://DESKTOP-957S9DI:8080/";
    private ArrayList<String> serverMsgs;
    private WebSocketContainer WSEndPoint = ContainerProvider.getWebSocketContainer();
    private Gson serializer;
    private Session session;
    private ReentrantLock msgLock = new ReentrantLock();
    private ClientWebSocketHandler messageHandler;
   // Thread messageScanner = new Thread();
    public Session getCurrSession() {
        return session;
    }
    public ServerFacade() {
        serializer = new Gson();
    }

    private HttpURLConnection getConnection(String path) throws ServerFacadeException {
        HttpURLConnection connect;
        try {
            var URI = new URI(URLStub + path);
            var URL = URI.toURL();
            connect = (HttpURLConnection)URL.openConnection();
        }
        catch (Exception x) {
            throw ExceptionConverter.convert(x);
        }

        return connect;
    }

    private String sendRequest(Request request) throws ServerFacadeException {
        String path;
        String method;
        String token = null;
        switch (request.getType()) {
            case CLEAR:
                path = "db";
                method = "DELETE";
                break;
            case CREATE_GAME:
                path = "game";
                method = "POST";
                token = request.getToken();
                break;
            case JOIN_GAME:
                path = "game";
                method = "PUT";
                token = request.getToken();
                break;
            case LIST_GAMES:
                path = "game";
                method = "GET";
                token = request.getToken();
                break;
            case LOGIN:
                path = "session";
                method = "POST";
                break;
            case LOGOUT:
                path = "session";
                method = "DELETE";
                token = request.getToken();
                break;
            case REGISTER:
                path = "user";
                method = "POST";
                break;
            case LEAVE_GAME:
                path = "game/observer";
                method = "DELETE";
                token = request.getToken();
                break;
            default:
                throw new ServerFacadeException(
                        "Very weird error. Request lacks a type.");
            }
        HttpURLConnection connection = getConnection(path);
        byte[] rawResponse;
        String responseStr;
        InputStream IS = null;

        try {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod(method);

            connection.setRequestProperty("authorization", token);
            if (!method.equals("GET")) {
                connection.setDoOutput(true);
                OutputStream OS = connection.getOutputStream();
                byte[] serializedReq = serializer.toJson(request).getBytes();

                OS.write(serializedReq);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                IS = connection.getInputStream();
            }
            else {
                IS = connection.getErrorStream();
            }

            int charsAvailable = IS.available();

            rawResponse = new byte[charsAvailable];
            if (IS.read(rawResponse) < charsAvailable) {
                throw new ServerFacadeException("Unable to read all bytes");
            }
            responseStr = new String(rawResponse);
        }
        catch (IOException x) {
            throw ExceptionConverter.convert(x);
        }

        return responseStr;
    }

    public ClearResponse clear() throws ServerFacadeException {
        return serializer.fromJson(sendRequest(new ClearRequest()), ClearResponse.class);
    }

    public CreateGameResponse createGame(String token, String gameName) throws ServerFacadeException {
        return serializer.fromJson(sendRequest(new CreateGameRequest(token, gameName)), CreateGameResponse.class);
    }

    public JoinGameResponse joinGame(String token, int gameID, TeamColor team) throws ServerFacadeException {
        return serializer.fromJson(sendRequest(new JoinGameRequest(token, gameID, team)), JoinGameResponse.class);
    }

    public ListGamesResponse listGames(String token) throws ServerFacadeException {
        return serializer.fromJson(sendRequest(new ListGamesRequest(token)), ListGamesResponse.class);
    }
    public LoginResponse login(String username, String password) throws ServerFacadeException {
        return serializer.fromJson(sendRequest(new LoginRequest(username, password)), LoginResponse.class);
    }

    public LogoutResponse logout(String token) throws ServerFacadeException {
        return serializer.fromJson(sendRequest(new LogoutRequest(token)), LogoutResponse.class);
    }

    public RegisterResponse register(String username, String password, String email) throws ServerFacadeException {
        return serializer.fromJson(sendRequest(new RegisterRequest(username, password, email)), RegisterResponse.class);
    }

    public LeaveGameResponse leaveGame(String token, int gameID) // The http method, not websocket!
            throws ServerFacadeException {
        return serializer.fromJson(sendRequest(new LeaveGameRequest(token, gameID)),
                LeaveGameResponse.class);
    }

    public void webSocket(String authToken, int gameID, TeamColor color) throws ServerFacadeException {
        try {
            session = WSEndPoint.connectToServer(this, URI.create("ws://DESKTOP-957S9DI:8080/connect"));
            messageHandler = new ClientWebSocketHandler();
            session.addMessageHandler(messageHandler);

            sendWSMessage(serializer.toJson(new Greeting(authToken, gameID, color)));
        }
        catch (Exception x) {
            throw ExceptionConverter.convert(x);
        }
    }

    public void refreshWS() throws Exception {
        sendWSMessage("Still here");
    }

    public void endSession() {
        session = null;
        messageHandler = null;
    }

    private void sendWSMessage(String message) throws IOException {
        msgLock.lock();
        session.getBasicRemote().sendText(message);
        msgLock.unlock();
    }

    public void sendMove(String authToken, MoveObj theMove, Integer gameID) throws IOException {
        sendWSMessage(serializer.toJson(new MoveMsg(authToken, theMove, gameID)));
    }

    public void leaveGameWS(String authToken, Integer gameID) throws IOException {
        sendWSMessage(serializer.toJson(new LeaveMsg(authToken, gameID)));
    }

    public void requestBoard(String authToken, int gameID) throws IOException {
        sendWSMessage(serializer.toJson(new GetBoardMsg(authToken, gameID)));
    }

    public void onOpen(Session s, EndpointConfig config) {
    }

 /*   public ClientWebSocketHandler getMsgHandler() throws ServerFacadeException {
        for (var handler : session.getMessageHandlers()) {
            return (ClientWebSocketHandler)handler;          // There should only be one
        }
        throw new ServerFacadeException("Couldn't get handler");
    }*/

    public ArrayList<String> getNewMoveMsgs() throws ServerFacadeException {
        return messageHandler.getNewMoveMsgs();
    }

    public ArrayList<String> getNewJoinMsgs() throws ServerFacadeException {
        return messageHandler.getNewJoinMsgs();
    }

    public ArrayList<String> getNewLeaveMsgs() throws ServerFacadeException {
        return messageHandler.getNewLeaveMsgs();
    }

    private PieceObj constructPawn(HashMap pieceMap, BoardObj theBoard) {
        Integer row = ((Double)pieceMap.get("row")).intValue();
        Integer column = ((Double)pieceMap.get("column")).intValue();

        Position pos = new Position(row, column);
        TeamColor color = TeamColor.valueOf((String)pieceMap.get("color"));
        ChessPiece.PieceType type = ChessPiece.PieceType.valueOf((String)pieceMap.get("type"));
        boolean hasMoved = (Boolean)pieceMap.get("hasMoved");
        boolean vulnerable = (Boolean)pieceMap.get("vulnerableToEP");

        return new Pawn(theBoard, color, pos, hasMoved, vulnerable);
    }

    private PieceObj constructRKP(HashMap pieceMap, BoardObj theBoard) throws ServerFacadeException {
        Integer row = ((Double)pieceMap.get("row")).intValue();
        Integer column = ((Double)pieceMap.get("column")).intValue();

        Position pos = new Position(row, column);
        TeamColor color = TeamColor.valueOf((String)pieceMap.get("color"));
        ChessPiece.PieceType type = ChessPiece.PieceType.valueOf((String)pieceMap.get("type"));
        boolean hasMoved = (Boolean)pieceMap.get("hasMoved");

        if (type == ChessPiece.PieceType.ROOK) {
            return new Rook(theBoard, color, pos, hasMoved);
        }
        else if (type == ChessPiece.PieceType.KING) {
            return new King(theBoard, color, pos, hasMoved);
        }
        else {
            throw new ServerFacadeException("Invalid piece type for RKP");
        }
    }

    private PieceObj constructPiece(HashMap pieceMap, BoardObj theBoard) throws ServerFacadeException {
        Integer row = ((Double)pieceMap.get("row")).intValue();
        Integer column = ((Double)pieceMap.get("column")).intValue();

        Position pos = new Position(row, column);
        TeamColor color = TeamColor.valueOf((String)pieceMap.get("color"));
        ChessPiece.PieceType type = ChessPiece.PieceType.valueOf((String)pieceMap.get("type"));

        if (type == ChessPiece.PieceType.KNIGHT) {
            return new Knight(theBoard, color, pos);
        }
        else if (type == ChessPiece.PieceType.QUEEN) {
            return new Queen(theBoard, color, pos);
        }
        else if (type == ChessPiece.PieceType.BISHOP) {
            return new Bishop(theBoard, color, pos);
        }
        else {
            throw new ServerFacadeException("Invalid piece type for regular piece");
        }
    }

    public ArrayList<BoardObj> getBoardUpdates() throws ServerFacadeException {
        var returnVal = new ArrayList<BoardObj>();
        var pieceList = new ArrayList<String>();
        var pieceMap = new HashMap<String, String>();
        String type;

        var boardUpdates = messageHandler.getNewBoardMsgs();
        for (String thisBoard : boardUpdates) {
            BoardObj newBoard = new BoardObj(null, false);
            pieceList = serializer.fromJson(thisBoard, ArrayList.class);

            for (String thisPiece : pieceList) {
                pieceMap = serializer.fromJson(thisPiece, HashMap.class);
                PieceObj newPiece;

                type = pieceMap.get("type");

                switch (type) {
                    case "PAWN":
                        newPiece = constructPawn(pieceMap, newBoard);
                        break;
                    case "KNIGHT":
                        newPiece = constructPiece(pieceMap, newBoard);
                        break;
                    case "KING":
                        newPiece = constructRKP(pieceMap, newBoard);
                        newBoard.setKing((King)newPiece);
                        break;
                    case "ROOK":
                        newPiece = constructRKP(pieceMap, newBoard);
                        break;
                    case "BISHOP":
                        newPiece = constructPiece(pieceMap, newBoard);
                        break;
                    case "QUEEN":
                        newPiece = constructPiece(pieceMap, newBoard);
                        break;
                    default:
                        throw new ServerFacadeException("Piece type does not match any expected value");
                }
                newBoard.addPiece(newPiece.getPos(), newPiece);
            }
            returnVal.add(newBoard);
        }
        return returnVal;
    }
}
