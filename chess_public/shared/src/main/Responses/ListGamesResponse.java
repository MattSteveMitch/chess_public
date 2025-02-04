package Responses;
import models.GameBasicInfo;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * A response to a request to list all current games
 */
public class ListGamesResponse extends Response {
    /**
     * Check that the response code is one that is used by this response type
     * @param responseCode
     */
    private void checkValidCode(int responseCode) {
        if (responseCode == 400 || responseCode == 403) {
            System.out.println("Wrong response code; this is not used for ListGames response");
            System.exit(0);
        }
    }
    /**
     * If a constructor is only meant to be used for an error code, this makes sure that
     * the response code is indeed an error code
     * @param responseCode
     */
    private void checkNot200(int responseCode) {
        if (responseCode == 200) {
            System.out.println("Error: ListGames response success must specify gameList");
            System.exit(0);
        }
    }
    /**
     * Calls constructor in parent class that generates the default message for the given
     * response code.
     * @param responseCode
     */
    public ListGamesResponse(int responseCode) {
        super(responseCode);
        checkNot200(responseCode);
        checkValidCode(responseCode);
    }/*
    /**
     * Constructor for generating custom error messages (only used with error codes)
     * @param responseCode
     * @param message The custom message
     */
   /* public ListGamesResponse(int responseCode, String message) {
        super(responseCode, message);
        checkNot200(responseCode);
        checkValidCode(responseCode);
    }*/
    /**
     * The constructor called for a response to a successful request. If request was not
     * successful, the server creator is notified and the default error message is generated
     * @param responseCode
     * @param gameList The requested list of games
     */
    public ListGamesResponse(int responseCode, ArrayList<GameBasicInfo> gameList) {
        super(responseCode);
        checkValidCode(responseCode);
        if (responseCode != 200) {
            System.out.println("gameList not necessary for a failure response");
            return;
        }
        this.games = gameList;
    }

    public ArrayList<GameBasicInfo> getList() {
        return games;
    }
    /**
     * The requested list of games with their basic info
     */
    private ArrayList<GameBasicInfo> games;
}
