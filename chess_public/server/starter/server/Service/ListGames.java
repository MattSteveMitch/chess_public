package server.Service;

import Responses.CreateGameResponse;
import server.dataAccess.AuthDAO;
import server.dataAccess.DataAccessException;
import server.dataAccess.GameDAO;
import models.Game;
import models.GameBasicInfo;
import Requests.ListGamesRequest;
import Responses.ListGamesResponse;

import java.util.HashMap;
import java.util.ArrayList;

public class ListGames {
    /**
     * Handles requests to list all active games
     * @param request The request received
     * @return A Response object of subtype ListGamesResponse
     */
    public static ListGamesResponse listGames(ListGamesRequest request,
                                              AuthDAO authDao,
                                              GameDAO gameDao) {
        ArrayList<GameBasicInfo> gameList;
        ListGamesResponse returnVal;
        try {
            if (authDao.matchToken(request.getToken()) == null) {
                returnVal = new ListGamesResponse(401);
            }
            else {
                gameList = gameDao.getAll();
                returnVal = new ListGamesResponse(200, gameList);
            }
        } catch (DataAccessException DAE) {
            returnVal = new ListGamesResponse(500);
        }

        return returnVal;
    }
}
