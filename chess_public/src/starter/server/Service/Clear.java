package server.Service;

import Requests.ClearRequest;
import Responses.ClearResponse;
import server.dataAccess.AuthDAO;
import server.dataAccess.UserDAO;
import server.dataAccess.GameDAO;
import server.dataAccess.DataAccessException;

public class Clear {
    /**
     * Handles requests to clear all data
     * @param request The request received
     * @param authDao Authentication token data access object
     * @param gameDao Game data access object
     * @param userDao User data access object
     * @return A Response object of subtype ClearResponse
     */
    public static ClearResponse clear(ClearRequest request,
                                      AuthDAO authDao,
                                      UserDAO userDao,
                                      GameDAO gameDao) {
        int responseCode = 200;
        String message = null;

        try {
            authDao.clear();
            gameDao.clear();
            userDao.clear();
        } catch (DataAccessException DAE) {
            responseCode = 500;
            message = DAE.getMessage();
        }

        return new ClearResponse(responseCode, message);
    }
}
