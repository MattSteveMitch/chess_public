package server.Service;

import models.User;
import Requests.LoginRequest;
import Responses.LoginResponse;
import server.dataAccess.AuthDAO;
import server.dataAccess.UserDAO;
import models.AuthToken;
import server.dataAccess.DataAccessException;

public class Login {
    /**
     * Handles login requests
     * @param request The request received
     * @return A Response object of subtype LoginResponse
     */
    public static LoginResponse login(LoginRequest request,
                                      AuthDAO authDao,
                                      UserDAO userDao) {
        LoginResponse returnVal;
        AuthToken returnToken;
        try {
            User user = userDao.find(request.getUsername());
            if (user == null) {
                returnVal = new LoginResponse(401, "Error: Invalid username");
            }
            else if (user.getPassword().equals(request.getPassword())) {
                returnToken = authDao.insert(user.getUsername());
                returnVal = new LoginResponse(200, returnToken);
            }
            else {
                returnVal = new LoginResponse(401, "Error: Invalid password");
            }
        } catch (DataAccessException except) {
            returnVal = new LoginResponse(500);
        }

        return returnVal;
    }
}