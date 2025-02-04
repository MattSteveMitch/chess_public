package server.Service;

import Requests.CreateGameRequest;
import Responses.CreateGameResponse;
import server.dataAccess.AuthDAO;
import server.dataAccess.GameDAO;
import server.dataAccess.DataAccessException;

public class CreateGame {
    /**
     * Handles requests to create a game
     * @param request The request received
     * @return A Response object of subtype CreateGameResponse
     */
    public static CreateGameResponse createGame(CreateGameRequest request,
                                                AuthDAO authDao,
                                                GameDAO gameDao) {
        CreateGameResponse response = null;
        int gameID;
        try {
            if (authDao.matchToken(request.getToken()) != null) {

                gameID = gameDao.insert(request.getGameName());
                response = new CreateGameResponse(200, gameID);

            } else {
                response = new CreateGameResponse(401);
            }
        } catch (DataAccessException DAE) {
            response = new CreateGameResponse(500);
        }

        return response;
    }
}
