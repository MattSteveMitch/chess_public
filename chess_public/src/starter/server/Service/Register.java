package server.Service;

import Requests.RegisterRequest;
import Responses.RegisterResponse;
import server.dataAccess.UserDAO;
import server.dataAccess.AuthDAO;
import models.AuthToken;
import server.dataAccess.DataAccessException;

public class Register {
    /**
     * Handles requests to register new account
     * @param request The request received
     * @return A Response object of subtype RegisterResponse
     */
    public static RegisterResponse register(RegisterRequest request,
                                            AuthDAO authDao,
                                            UserDAO userDao) {
        RegisterResponse returnVal = null;
        AuthToken returnToken = null;
        try {
            String username = request.getUsername();
            if (username.equals("None") || username.equals("null")) {
                return new RegisterResponse(403, "Error: Username not allowed");
            }
            userDao.insert(username, request.getPassword(), request.getEmail());
            returnToken = authDao.insert(username);
        }
        catch (DataAccessException DAE) {
            String message = DAE.getMessage();
            if (message.startsWith("Duplicate")) {
                returnVal = new RegisterResponse(403, "Error: " + message);
            }
            else {returnVal = new RegisterResponse(500, "Error: " + message);}
        }

        if (returnVal == null) {
            returnVal = new RegisterResponse(200, returnToken);
        }

        return returnVal;
    }
}
