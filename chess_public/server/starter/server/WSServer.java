package server;

import chess.*;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import models.GameBasicInfo;
import java.util.concurrent.locks.ReentrantLock;

import chess.ChessGame.TeamColor;
import server.dataAccess.*;

import java.io.IOException;

@WebSocket
public class WSServer extends Server {
    public WSServer() throws SQLException, DataAccessException {
//        super();
        sessions = new HashMap<>();
        moveLock = new ReentrantLock();
    }

    @OnWebSocketMessage
    public void receiveMessage(Session session, String input) {
        try {
            handleMessage(session, input);
        }
        catch (Exception x) {
            try {
                sendMessageWOutUsername(session, "Error: " + x.getMessage());
                System.out.println("Error: " + x.getMessage());
            }
            catch (IOException iox) {
                System.out.println(iox.getMessage());
            }
        }
    }

    public void handleMessage(Session session, String message) throws Exception {
        if (message.equals("Still here")) {
            return;
        }
        HashMap<String, Object> messageMap = serializer.fromJson(message, HashMap.class);
        String authToken = (String)messageMap.get("authToken");
        String username = checkAuthToken(authToken);
        Integer gameID = ((Double) messageMap.get("gameID")).intValue();
        try {
            String type = (String) messageMap.get("type");
            switch (type) {
                case "GET_BOARD":
                    sendBoard(username, gameID);
                    break;
                case "GREETING":
                    String color = (String) messageMap.get("color");

                    sessions.put(username, session);
                    broadcastJoin(username, gameID, color);
                    break;
                case "LEAVE":
                    removePlayer(username, gameID);
                    break;
                case "MAKE_MOVE":
                    String moveStr = (String) messageMap.get("move");
                    MoveObj theMove = deserializeMove(moveStr);
                    gameID = ((Double) messageMap.get("gameID")).intValue();

                    makeMove(theMove, gameID, username);
                    break;
                default:
                    throw new BadRequestException("Client websocket request type not found");
            }
        }
        catch (Exception x) {
            sendMessage(username, "Error: " + x.getMessage());
        }
    }

    private void removePlayer(String username, int gameID) throws Exception {
        GameBasicInfo game = gameDao.getRow(gameID);
        String whitePlayer = game.whiteUsername;
        String blackPlayer = game.blackUsername;
        TeamColor playerColor = null;

        if (username.equals(blackPlayer)) {
            playerColor = TeamColor.BLACK;
        }
        else if (username.equals(whitePlayer)) {
            playerColor = TeamColor.WHITE;
        }

        if (!gameDao.removePlayer(username, playerColor, gameID)) {
            throw new Exception("Couldn't find player to remove");
        }

        var recipients = gameDao.getObservers(gameID);
        recipients.add(whitePlayer);
        recipients.add(blackPlayer);

        broadcastLeave(username, recipients);

        boolean playersGone = (username.equals(whitePlayer) && blackPlayer == null) ||
                (username.equals(blackPlayer) && whitePlayer == null) ||
                (blackPlayer == null && whitePlayer == null);

        boolean observersGone = (game.observers == 0) ||
                (game.observers == 1 && blackPlayer == null && whitePlayer == null);

        if (game.whiteActive == null && playersGone && observersGone) {
            gameDao.remove(gameID);
        }
    }

    private MoveObj deserializeMove(String moveStr) throws Exception {
        MoveObj returnVal;
        if (moveStr.length() == 0) {
            throw new Exception("String is empty");
        }
        if (moveStr.startsWith("R")) {
            return new MoveObj(null, new Position(1, 1));
        }

        if (moveStr.length() < 5) {
            throw new Exception("String is too short");
        }

        String pos1Str = moveStr.substring(0, 2);
        String pos2Str = moveStr.substring(3, 5);

        if (!Position.isValidPos(pos1Str) || !Position.isValidPos(pos2Str)) {
            throw new Exception("Error deserializing move");
        }
        Position pos1 = new Position(pos1Str);
        Position pos2 = new Position(pos2Str);

        returnVal = new MoveObj(pos1, pos2);

        char finalChar;
        if (moveStr.length() > 6) {
            finalChar = moveStr.charAt(6);
            switch (finalChar) {
                case 'C':
                    returnVal.markAsCastleMove();
                    break;
                case 'Q':
                    returnVal.addPromotion(ChessPiece.PieceType.QUEEN);
                    break;
                case 'K':
                    returnVal.addPromotion(ChessPiece.PieceType.KNIGHT);
                    break;
                case 'R':
                    returnVal.addPromotion(ChessPiece.PieceType.ROOK);
                    break;
                case 'B':
                    returnVal.addPromotion(ChessPiece.PieceType.BISHOP);
                    break;
                default:
                    throw new Exception("Error deserializing move: Invalid final character");
            }
        }

        return returnVal;
    }

    private void broadcastLeave(String username, ArrayList<String> recipients) throws Exception {
        String message = "Leaving: " + username;

        for (String recip : recipients) {
            if (recip != null && !recip.equals(username)) {
                sendMessage(recip, message.toString());
            }
        }
    }

    private void broadcastJoin(String username, int gameID, String color) throws Exception {
        var recipients = new ArrayList<String>();
        recipients.add(gameDao.getPlayer(gameID, TeamColor.WHITE, null));
        recipients.add(gameDao.getPlayer(gameID, TeamColor.BLACK, null));
        recipients.addAll(gameDao.getObservers(gameID));

        StringBuilder message = new StringBuilder("Joining: ");
        if (color != null) {
            message.append(color);
        }
        else {
            message.append("null ");
        }
        message.append(' ');
        message.append(username);

        for (String recip : recipients) {
            if (recip != null && !recip.equals(username)) {
                sendMessage(recip, message.toString());
            }
        }
    }

    private boolean verifyMove(models.Game theGame, String username) {
        GameObj gameStatus = theGame.getStatus();

        String activePlayer = theGame.getPlayer(gameStatus.getTeamTurn());

        return username.equals(activePlayer);
    }

    private void makeMove(MoveObj theMove, Integer gameID, String username) throws Exception {

        models.Game theGame = gameDao.find(gameID);
        if (!verifyMove(theGame, username)) {
            throw new Exception("Out of turn");
        }

        TeamColor activePlayer = theGame.getStatus().getTeamTurn();

        theGame.getStatus().makeMove(theMove);

        moveLock.lock();
        if (!gameDao.addMove(theMove.toString(), gameID)) {
            throw new Exception("Unable to update move record");
        }

        gameDao.updateGameRecord(theGame);

        String recipient = theGame.getPlayer(TeamColor.WHITE);
        if (recipient != null && (activePlayer == TeamColor.BLACK)) {
            sendMessage(recipient, "Move: " + theMove.toString());
        }
        recipient = theGame.getPlayer(TeamColor.BLACK);
        if (recipient != null && (activePlayer == TeamColor.WHITE)) {
            sendMessage(recipient, "Move: " + theMove.toString());
        }

        for (String thisRecipient : theGame.getObservers()) {
            sendMessage(thisRecipient, "Move: " + theMove.toString());
        }
        moveLock.unlock();
    }

    private String checkAuthToken(String token) throws Exception {
        if (token == null) {
            throw new BadRequestException("No authorization token received");
        }
        String username = authDao.matchToken(token);

        if (username == null) {
            throw new BadRequestException("Invalid authorization token");
        }

        return username;
    }

    private void sendBoard(String username, int gameID) throws Exception {
        moveLock.lock();
        ArrayList<String> moves = gameDao.getMoves(gameID);
        ArrayList<String> pieceList = new ArrayList<>();

        sendMessage(username, "PrevMoves: " + serializer.toJson(moves));
        moveLock.unlock();
    }

    private void sendMessageWOutUsername(Session session, String response) throws IOException {
        if (!session.isOpen()) {
            System.out.println("session closed! Removing");
            sessions.values().remove(session);
        }
        session.getRemote().sendString(response);
    }

    private boolean sendMessage(String username, String response) throws Exception {
        Session session = sessions.get(username);
        if (session == null) {
            return false;
        }
        if (!session.isOpen()) {
            System.out.println("session closed!");
            //sessions.get(username).close();
            sessions.remove(username);
            return false;
        }
        session.getRemote().sendString(response);
        return true;
    }

    private HashMap<String, Session> sessions;
    private ReentrantLock moveLock;
}