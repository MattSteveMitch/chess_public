package server.Service;

import Requests.LogoutRequest;
import Responses.LogoutResponse;
import server.dataAccess.AuthDAO;
import server.dataAccess.DataAccessException;

public class Logout {
    /**
     * Handles logout requests
     * @param request The request received
     * @return A Response object of subtype LogoutResponse
     */
    public static LogoutResponse logout(LogoutRequest request,
                                        AuthDAO authDao) {
        LogoutResponse returnVal = null;
        String username;
        try {
            username = authDao.matchToken(request.getToken());
            if (username == null) {
                returnVal = new LogoutResponse(401,
                        "Error: Could not delete AuthToken; not found");
            }
            else {
                authDao.delete(username);
            }
        }
        catch (DataAccessException DAE) {
            if (DAE.getMessage().startsWith("Error: Could not delete")) {
                returnVal = new LogoutResponse(401, DAE.getMessage());
            }
            else {returnVal = new LogoutResponse(500);}
        }

        if (returnVal == null) {
            returnVal = new LogoutResponse(200);
        }

        return returnVal;
    }
}