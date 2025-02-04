package server.Service;

import Requests.JoinGameRequest;
import Responses.JoinGameResponse;
import server.dataAccess.AuthDAO;
import server.dataAccess.GameDAO;
import server.dataAccess.DataAccessException;

public class JoinGame {
    /**
     * Handles requests to join a game
     * @param request The request received
     * @return A Response object of subtype JoinGameResponse
     */
    public static JoinGameResponse joinGame(JoinGameRequest request,
                                            AuthDAO authDao,
                                            GameDAO gameDao) {
        String username;
        boolean hadEffect = false;

        int code;
        try {
            username = authDao.matchToken(request.getToken());
            if (username == null) {
                return new JoinGameResponse(401);
            }
            hadEffect = gameDao.addPlayer(username,
                request.getPlayerColor(), request.getGameID());
        }
        catch (DataAccessException except) {
            if (except.getMessage().startsWith(gameDao.playerTakenMsgStart)) {code = 403;}
            else {code = 400;}
            return new JoinGameResponse(code, "Error: " + except.getMessage());
        }

        return new JoinGameResponse(200, hadEffect);
    }
}
