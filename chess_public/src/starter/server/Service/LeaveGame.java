package server.Service;

import Requests.LeaveGameRequest;
import Responses.LeaveGameResponse;
import server.dataAccess.DataAccessException;
import server.dataAccess.GameDAO;
import server.dataAccess.AuthDAO;

public class LeaveGame {
    public static LeaveGameResponse leaveGame(LeaveGameRequest req,
                                                        AuthDAO authDao,
                                                        GameDAO gameDao) {
        LeaveGameResponse returnVal;
        try {
            //String personToRemove = req.getUsername();
            String requestSender = authDao.matchToken(req.getToken());

            if (requestSender == null) {
                returnVal = new LeaveGameResponse(401, "Error: Invalid authorization token");
            }
            else {
                gameDao.removeObserver(req.getGameID(), requestSender);
                returnVal = new LeaveGameResponse(200);
            }
        }
        catch (DataAccessException except) {
            returnVal = new LeaveGameResponse(500, "Error: " + except.getMessage());
        }

        return returnVal;
    }
}
