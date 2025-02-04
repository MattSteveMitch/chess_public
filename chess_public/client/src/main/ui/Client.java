package ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import Responses.RegisterResponse;
import Responses.LogoutResponse;
import Responses.ListGamesResponse;
import Responses.CreateGameResponse;
import Responses.JoinGameResponse;
import chess.*;
import chess.ChessPiece.PieceType;
import java.util.HashSet;
import java.util.ArrayList;
import models.GameBasicInfo;
import chess.ChessGame.TeamColor;
import chess.BoardObj;
import java.time.LocalTime;

public class Client {
    final static int BUFFER_SIZE = 128;
    final static int MAX_USERNAME_SIZE = 32;
    final static int MAX_PASSWORD_SIZE = 32;
    final static int MAX_EMAIL_SIZE = 64;
    static private ServerFacade serverF;
    static private ArrayList<GameBasicInfo> gameList;
    static private models.Game currGame;
    static private LocalTime LastWSRefresh = null;
    static private Integer currGameID = null;
    static private TeamColor myColor = null;
    static private String authToken = null;
    static private String myUsername = null;
    final static String loggedOutMenu = "Type one of the following commands (not case sensitive):\n" +
            "\n\033[32;1mhelp\033[39;0m" + ": Display your current options\n" +
            "\033[32;1mquit\033[39;0m" + ": Exit the program\n" +
            "\033[32;1mlogin\033[39;0m" + ": Log in to view and join games\n" +
            "\033[32;1mregister\033[39;0m" + ": Create an account\n\n";
    final static String helpMsg =
            "Note: You can type \033[32;1mhelp\033[39;0m at \033[4many time\033[0m while the application is running";

    final static String duringExecutionMenu = "\nYou may type any of the following commands (not case sensitive):\n" +
            "\n\033[32;1mhelp\033[39;0m" + ": Display your current options\n" +
            "\033[32;1mcancel\033[39;0m" + ": Abort current action\n" +
            "\033[32;1mquit\033[39;0m" + ": Exit the program\n\n";
    final static String[] loggedInCmds = {"help", "quit", "games", "join", "create", "observe", "logout"};
    final static String loggedInMenu = "\nType one of the following commands (not case sensitive):\n" +
            "\n\033[32;1mhelp\033[39;0m" + ": Display your current options\n" +
            "\033[32;1mgames\033[39;0m" + ": Show all current games\n" +
            "\033[32;1mjoin\033[39;0m" + ": Join a game\n" +
            "\033[32;1mcreate\033[39;0m" + ": Start a game of your own\n" +
            "\033[32;1mobserve\033[39;0m" + ": Join a game as a spectator\n" +
            "\033[32;1mlogout\033[39;0m" + ": Log out\n\n";

    final static String gameMenu = "\nType one of the following commands (not case sensitive):\n" +
            "\n\033[32;1mhelp\033[39;0m" + ": Display your current options\n" +
            "\033[32;1mmove\033[39;0m" + ": Make a move\n" +
            "\033[32;1moptions\033[39;0m" + ": Show all legal moves for a given piece\n" +
            "\033[32;1mboard\033[39;0m" + ": Show board\n" +
            "\033[32;1mresign\033[39;0m" + ": Surrender to your opponent\n" +
            "\033[32;1mleave\033[39;0m" + ": Leave the game\n\n";
    final static String observerMenu = "\nType one of the following commands (not case sensitive):\n" +
            "\n\033[32;1mhelp\033[39;0m" + ": Display your current options\n" +
            "\033[32;1moptions\033[39;0m" + ": Show all legal moves for a given piece\n" +
            "\033[32;1mboard\033[39;0m" + ": Show board\n" +
            "\033[32;1mleave\033[39;0m" + ": Leave the game\n\n";
    static PrintStream console = System.out;
    static InputStream reader = System.in;
    //static boolean exiting = false;
    static byte[] buffer = new byte[BUFFER_SIZE];
    static byte[] garbage = new byte[1024];
   // static String userInput;
    static boolean skipValidityCheck;
    static boolean loggedIn;

    public Client() {
        serverF = new ServerFacade();
    }

    public static void main(String[] args) throws Exception {
        serverF = new ServerFacade();
        String userInput;

/*
        authToken = "l,=U_kgQ6hr/7G*+";
        session();
//*/
        console.print("\033[1m\nWelcome to chess!\033[39;0m\n");
        console.print(loggedOutMenu);
        console.println(helpMsg);
        int pendingBytes = 1;
        int readBytes;

        while (true) {
            if (pendingBytes > 0) { // If the previous value of pendingBytes was > 0; i.e. if something was input
                console.print(">> ");
            }
            buffer = new byte[BUFFER_SIZE];
            pendingBytes = reader.available();

            if (pendingBytes > 0) {
                if (pendingBytes > BUFFER_SIZE) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }
                readBytes = reader.read(buffer);

                if (readBytes < pendingBytes) {
                    console.println("Error: Unable to read full command; please try again");
                    reader.read(garbage);
                    continue;
                }

                userInput = new String(buffer).trim().toLowerCase();

                switch (userInput) {
                    case "help":
                        console.print(loggedOutMenu);
                        break;
                    case "quit":
                        System.exit(0);
                    case "login":
                        attemptLogin();
                        break;
                    case "logout":
                        console.println("Already logged out");
                        break;
                    case "register":
                        attemptRegistration();
                        break;
                    default:
                        if (loggedInCmdsContains(userInput)) {
                            console.println("Must log in first");
                        }
                        else {
                            console.println("Invalid command");
                        }
                }
            }
        }
    }

    private static boolean loggedInCmdsContains(String cmd) {
        for (String option : loggedInCmds) {
            if (option.equals(cmd)) {
                return true;
            }
        }
        return false;
    }

    private static void attemptLogin() throws Exception {
        int pendingBytes = 1;

        String password;
        boolean validPassword = false;
        skipValidityCheck = false;
        console.println("Enter your username and password. Enter \"cancel\" at any time to return");
        console.println("to main menu.");

        String username = getUsername(true);
        if (username == null) {
            return;
        }

        while (!validPassword) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print("Password: ");
            }
            pendingBytes = reader.available();
            buffer = new byte[MAX_PASSWORD_SIZE];

            if (pendingBytes > 0) {
                if (reader.read(buffer) < pendingBytes) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                password = new String(buffer).trim();

                if (checkForCancel(password, false)) {
                    return;
                }
                else if (password.length() == 0) {
                    console.println("Invalid password");
                    continue;
                }

                if (!skipValidityCheck) {validPassword = sendLoginRequest(username, password);}
            }
        }
        session();
    }

    private static String getUsername(boolean forLogin) throws IOException, ServerFacadeException {
        String username = null;
        boolean validUsername = false;
        int pendingBytes = 1;

        while (!validUsername) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print("Username: ");
            }
            pendingBytes = reader.available();
            buffer = new byte[MAX_USERNAME_SIZE];

            if (pendingBytes > 0) {
                if (reader.read(buffer) < pendingBytes) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                username = new String(buffer).trim();

                if (username.equals("None") || username.equals("null")) {
                    console.println("Username not allowed");
                    continue;
                }
                else if (checkForCancel(username, false)) {
                    username = null;
                    break;
                }
                else if (username.length() == 0) {
                    continue;
                }

                if (!skipValidityCheck) {
                    validUsername = (registeredUsername(username) == forLogin);

                    if (!validUsername) {
                        if (forLogin) {
                            console.println("Username not found");
                        } else {
                            console.println("Username already taken");
                        }
                    }
                }
            }
        }

        return username;
    }

    static private boolean registeredUsername(String username) throws ServerFacadeException {
        boolean returnVal;
        var result = serverF.login(username, "");

        if (result.getMessage().equals("Error: Invalid password")) {
            returnVal = true;
        }
        else {
            returnVal = false;
        }

        return returnVal;
    }

    static private boolean sendLoginRequest(String username, String password) throws ServerFacadeException {
        boolean returnVal;
        var result = serverF.login(username, password);

        if (result.getResponseCode() == 200) {
            returnVal = true;
            myUsername = result.getUsername();
            authToken = result.getAuthToken();
        }
        else {
            returnVal = false;
            console.println(result.getMessage().substring(7));
        }

        return returnVal;
    }

    private static void session() throws Exception {
        console.print(loggedInMenu);
        loggedIn = true;
        int readBytes;
        int pendingBytes = 1;
        String userInput;

        while (loggedIn) {
            if (pendingBytes > 0) { // If the previous value of pendingBytes was > 0; i.e. if something was input
                console.print(myUsername + " >> ");
            }
            buffer = new byte[BUFFER_SIZE];
            pendingBytes = reader.available();

            if (pendingBytes > 0) {
                if (pendingBytes > BUFFER_SIZE) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }
                readBytes = reader.read(buffer);

                if (readBytes < pendingBytes) {
                    console.println("Error: Unable to read full command; please try again");
                    reader.read(garbage);
                    continue;
                }

                userInput = new String(buffer).trim().toLowerCase();

                switch (userInput) {
                    case "help":
                        console.print(loggedInMenu);
                        break;
                    case "logout":
                        if (sendLogoutRequest()) {
                            myUsername = null;
                        }
                        break;
                    case "login":
                        console.println("Already logged in");
                        break;
                    case "register":
                        console.println("Must log out first");
                        break;
                    case "games":
                        listGames();
                        break;
                    case "create":
                        attemptCreateGame();
                        break;
                    case "join":
                        attemptJoin(-1);
                        break;
                    case "observe":
                        attemptObserve();
                        break;
                    default:
                        console.println("Invalid command");
                }
            }
        }
    }

    private static void printGame(GameBasicInfo game, Integer index) throws ServerFacadeException {
        String blackPlayer;
        String whitePlayer;
        boolean singleObserver;

        String activePlayer;
        if (game.whiteActive) {
            activePlayer = "WHITE";
        }
        else {
            activePlayer = "BLACK";
        }

        singleObserver = game.observers == 1;
        if (game.blackUsername == null) {
            blackPlayer = "None";
        }
        else {
            blackPlayer = game.blackUsername;
        }

        if (game.whiteUsername == null) {
            whitePlayer = "None";
        }
        else {
            whitePlayer = game.whiteUsername;
        }
        console.println("\033[36;1;4m" + game.gameName + "\033[39;0m:");
        console.println("Game number: " + (index).toString());
        console.println("Playing as white: " + whitePlayer);
        console.println("Playing as black: " + blackPlayer);
        console.println("Currently " + activePlayer + "'s turn");

        if (!singleObserver) {
            console.println(Integer.valueOf(game.observers).toString() + " spectators\n");
        }
        else {
            console.println("1 spectator\n");
        }
    }

    private static boolean updateGameList() throws ServerFacadeException {
        boolean failure = false;

        ListGamesResponse result = serverF.listGames(authToken);
        if (result.getResponseCode() != 200) {
            console.println(result.getMessage());
            failure = true;
        }
        else {
            gameList = result.getList();
        }
        return failure;
    }

    private static void listGames() throws ServerFacadeException {
        if (updateGameList()) {
            return;
        }

        if (gameList.size() == 0) {
            console.println("No games");
        }
        for (Integer i = 0; i < gameList.size(); i++) {
            GameBasicInfo game = gameList.get(i);
            printGame(game, i + 1);
        }
    }

    private static void attemptCreateGame() throws Exception {
        String gameName = getNonEmptyInput("Enter a name for your game: ", true);
        int newGameID;
        int gameIndex = -1;

        if (gameName == null) {
            return;
        }
        CreateGameResponse result = serverF.createGame(authToken, gameName);

        if (result.getResponseCode() != 200) {
            console.println(result.getMessage() + "\nPlease try again");
            return;
        }
        else {
            newGameID = result.getID();
            console.println("New game created\n");
            console.println("Games:\n");
        }

        if (updateGameList()) {
            return;
        }
        for (int i = 0; i < gameList.size(); i++) {
            var game = gameList.get(i);
            if (game.gameID == newGameID) {
                gameIndex = i;
            }
            printGame(game, i + 1);
        }
        console.println("Game list updated");

        Boolean joinOwnGame = askQuestion("Would you like to join your own game? ");

        if (joinOwnGame == null) {}
        else if (joinOwnGame) {
            attemptJoin(gameIndex + 1);
        }
    }

    private static void attemptJoin(int gameNumber) throws Exception {
        Boolean againstSelf = false;
        boolean blackTaken;
        boolean whiteTaken;

        if (gameNumber == -1) {
            console.println("Games:\n");
            listGames();
            if (gameList.size() == 0) {
                return;
            }
            gameNumber = getGameNum("Enter the number of the game you would like to join: ");
            if (gameNumber == -1) {
                return;
            }
        }
        GameBasicInfo gameToJoin = gameList.get(gameNumber - 1);
        int gameID = gameToJoin.gameID;

 /*       TeamColor currColor = null;
        if (myUsername.equals(gameToJoin.blackUsername)) {
            currColor = TeamColor.BLACK;
        }
        else if (myUsername.equals(gameToJoin.whiteUsername)) {
            currColor = TeamColor.WHITE;
        }
        boolean alreadyInGame = currColor != null;*/
        blackTaken = gameToJoin.blackUsername != null && !myUsername.equals(gameToJoin.blackUsername);
        whiteTaken = gameToJoin.whiteUsername != null && !myUsername.equals(gameToJoin.whiteUsername);

        if (blackTaken ^ whiteTaken) {
       /*     if (alreadyInGame) {
                againstSelf = askQuestion(
                        "You are already playing in this game. Play against yourself? "
                );
                if (againstSelf == null || !againstSelf) {return;}
            }*/

            if (!blackTaken) {
                assignTeam(gameID, TeamColor.BLACK, againstSelf);
            }
            else {
                assignTeam(gameID, TeamColor.WHITE, againstSelf);
            }
        }
        else if (!blackTaken) {
            userChoosesTeam(gameID);
        }
        else {
            queryJoinAsObserver(gameID);
        }
    }

    static private Boolean askQuestion(String query) throws IOException {
        Boolean returnVal = null;
        int pendingBytes = 1;
        int readBytes;
        String input;

        while (returnVal == null) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print(query);
            }

            buffer = new byte[MAX_PASSWORD_SIZE];
            pendingBytes = reader.available();

            if (pendingBytes > 0) {
                readBytes = reader.read(buffer);

                if (pendingBytes > MAX_PASSWORD_SIZE) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                if (readBytes < pendingBytes) {
                    console.println("Error: Unable to read full command; please try again");
                    reader.read(garbage);
                    continue;
                }

                input = new String(buffer).trim().toLowerCase();

                if (checkForCancel(input, true)) {
                    returnVal = null;
                    break;
                }
                else if (input.length() == 0) {
                    continue;
                }

                if (skipValidityCheck) {continue;}

                switch (input) {
                    case "y":
                    case "yes":
                        returnVal = true;
                        break;
                    case "n":
                    case "no":
                        returnVal = false;
                }
            }
        }
        return returnVal;
    }

    static private void queryJoinAsObserver(Integer gameID) throws Exception {
        Boolean joinAsObserver = askQuestion("Both player spots are already filled. Join as observer? ");
        if (joinAsObserver == null) {
            return;
        }

        if (joinAsObserver) {
            joinGame(gameID, null);
        }
    }

    static private void assignTeam(int gameID, TeamColor team, boolean againstSelf)
            throws Exception {
        if (!againstSelf) {
            Boolean answer = askQuestion(chess.GameObj.otherTeam(team).toString() +
                    " is already taken; would you like to play as " + team.toString() + "? ");
            if (answer == null || !answer) {
                return;
            }
        }
        joinGame(gameID, team);
    }

    static private void userChoosesTeam(Integer gameID) throws Exception {
        boolean validInput = false;
        int pendingBytes = 1;
        int readBytes;
        String input;
        TeamColor color;

        while (!validInput) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print("What color would you like to play as (black or white, B or W)? ");
            }

            buffer = new byte[MAX_PASSWORD_SIZE];
            pendingBytes = reader.available();

            if (pendingBytes > 0) {
                readBytes = reader.read(buffer);

                if (pendingBytes > MAX_PASSWORD_SIZE) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                if (readBytes < pendingBytes) {
                    console.println("Error: Unable to read full command; please try again");
                    reader.read(garbage);
                    continue;
                }

                input = new String(buffer).trim().toUpperCase();

                if (checkForCancel(input, true)) {
                    return;
                }
                else if (input.length() == 0) {
                    continue;
                }

                if (skipValidityCheck) {continue;}

                switch (input) {
                    case "W":
                    case "WHITE":
                        color = TeamColor.WHITE;
                        validInput = joinGame(gameID, color);
                        break;
                    case "B":
                    case "BLACK":
                        color = TeamColor.BLACK;
                        validInput = joinGame(gameID, color);
                        break;
                }
            }
        }
    }

    private static int getGameNum(String prompt) throws IOException {
        boolean validInput = false;
        int pendingBytes = 1;
        int readBytes;
        String input;
        int gameID = -1;

        while (!validInput) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print(prompt);
            }

            buffer = new byte[MAX_PASSWORD_SIZE];
            pendingBytes = reader.available();

            if (pendingBytes > 0) {
                readBytes = reader.read(buffer);

                if (pendingBytes > MAX_PASSWORD_SIZE) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                if (readBytes < pendingBytes) {
                    console.println("Error: Unable to read full command; please try again");
                    reader.read(garbage);
                    continue;
                }

                input = new String(buffer).trim().toLowerCase();

                if (checkForCancel(input, true)) {
                    gameID = -1;
                    break;
                }
                else if (input.length() == 0) {
                    continue;
                }

                if (skipValidityCheck) {continue;}

                try {
                    gameID = Integer.parseInt(input);
                } catch (NumberFormatException x) {
                    console.println("Invalid number");
                    continue;
                }
                if (gameID < 1) {
                    console.println("Invalid number");
                    continue;
                }

                validInput = gameID > 0 && gameID <= gameList.size();
                if (!validInput) {
                    console.println("Invalid game number");
                }
            }
        }
        return gameID;
    }

    private static void attemptObserve() throws Exception {
        console.println("Games:\n");
        listGames();
        int gameNumber = getGameNum("Enter the number of the game you would like to observe: ");
        if (gameNumber == -1) {
            return;
        }

        GameBasicInfo gameToJoin = gameList.get(gameNumber - 1);
        int gameID = gameToJoin.gameID;

        boolean alreadyInGame = myUsername.equals(gameToJoin.whiteUsername) ||
                myUsername.equals(gameToJoin.blackUsername);

        if (alreadyInGame) {
            console.println("You are already in this game");
        }
        else {
            joinGame(gameID, null);
        }
    }

    private static boolean checkForCancel(String input, boolean loggedIn) {
        boolean returnVal = false;
        String menu;
        if (loggedIn) {
            menu = duringExecutionMenu.substring(0, 159) + "\n";
        }
        else {
            menu = duringExecutionMenu;
        }

        if (input.equalsIgnoreCase("cancel")) {
            returnVal = true;
        }
        else if (!loggedIn && input.equalsIgnoreCase("quit")) {
            System.exit(0);
        }
        else if (input.equalsIgnoreCase("help")) {
            skipValidityCheck = true;
            console.print(menu);
        }

        return returnVal;
    }

    private static void makeMove(MoveObj theMove) throws InvalidMoveException, IOException {
        currGame.getStatus().makeMove(theMove);
        GameObj status = currGame.getStatus();
        sendMoveToServer(theMove);
        printBoard();
        checkGameConditions();
    }

    private static String colorToPlayer(TeamColor color) {
        String playerName = currGame.getPlayer(color);
        if (playerName == null) {
            return color.toString();
        }
        else {
            return playerName.toString() + " (" + color.toString() + ')';
        }
    }

    private static void checkGameConditions() {
        GameObj status = currGame.getStatus();
        boolean finished = currGame.getStatus().getGameOver();
        TeamColor winner = currGame.getStatus().getWinner();
        TeamColor inCheck = currGame.getStatus().getCheckedTeam();
        TeamColor checkmated = currGame.getStatus().getCheckmatedTeam();

        if (checkmated != null) {
            console.print('\n' + colorToPlayer(checkmated) + " is in checkmate.");
        }
        else if (inCheck != null) {
            console.println('\n' + colorToPlayer(inCheck) + " is in check.");
        }
        if (finished && winner == null) {
            console.print('\n' + colorToPlayer(status.getTeamTurn()) + " has no legal moves.");
        }
        if (finished) {
            if (winner != null) {
                console.println(' ' + colorToPlayer(winner) + " wins!");
            }
            else {
                console.println("The game is a stalemate.");
            }
        }
    }

    private static void gameMove() throws IOException {
        String moveStr = null;
        MoveObj moveAttempt;
        String pos1;
        String pos2;
        boolean validMove = false;
        boolean isResignation = false;
        int pendingBytes = 1;
        console.println("Move format example: \"E4 G6\" (to move from E4 to G6)");

        while (!validMove) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print("Enter desired move: ");
            }
            pendingBytes = reader.available();
            buffer = new byte[MAX_USERNAME_SIZE];

            if (pendingBytes > 0) {
                if (reader.read(buffer) < pendingBytes) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                moveStr = new String(buffer).trim();

                if (checkForCancel(moveStr, true)) {
                    break;
                }
                else if (moveStr.length() < 5) {
                    continue;
                }

                if (!skipValidityCheck) {
                    try {
                        validMove = parseMove(moveStr);
                    }
                    catch (InvalidMoveException x) {
                        console.println(x.getMessage());
                    }
                }
            }
        }
    }

    private static void sendMoveToServer(MoveObj theMove) throws IOException {
        serverF.sendMove(authToken, theMove, currGameID);
    }

    private static boolean getPromotion(MoveObj theMove) throws IOException {
        String promoStr = null;
        MoveObj moveAttempt;
        String pos1;
        String pos2;
        boolean validPromo = false;
        int pendingBytes = 1;
        console.println("Promote your pawn! Q for Queen, K or N for Knight,");
        console.println("B for Bishop, R for Rook");

        while (!validPromo) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print("Enter desired promotion: ");
            }
            pendingBytes = reader.available();
            buffer = new byte[8];

            if (pendingBytes > 0) {
                if (reader.read(buffer) < pendingBytes) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                promoStr = new String(buffer).trim().toLowerCase();

                if (checkForCancel(promoStr, true)) {
                    return false;
                }
                else if (promoStr.length() == 0) {
                    continue;
                }

                validPromo = true;
                if (!skipValidityCheck) {
                    switch (promoStr) {
                        case "q":
                        case "queen":
                            theMove.addPromotion(PieceType.QUEEN);
                            break;
                        case "b":
                        case "bishop":
                            theMove.addPromotion(PieceType.BISHOP);
                            break;
                        case "k":
                        case "n":
                        case "knight":
                            theMove.addPromotion(PieceType.KNIGHT);
                            break;
                        case "r":
                        case "rook":
                            theMove.addPromotion(PieceType.ROOK);
                            break;
                        default:
                            validPromo = false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean checkForNotifications() throws Exception {
        boolean outputReceived = false;
        if (checkForNewJoins()) {
            outputReceived = true;
        }
        if (checkForNewLeave()) {
            outputReceived = true;
        }
        if (checkForNewMoves()) {
            checkGameConditions();
            outputReceived = true;
        }

        return outputReceived;
    }

    private static boolean checkForNewMoves() throws Exception {
        var newMoves = serverF.getNewMoveMsgs();
        TeamColor activePlayer;
        String activePlayerName;
        StringBuilder moveDescrip;
        MoveObj theMove;
        boolean outputReceived = false;
        boolean isResignation;

        try {
            for (String moveStr : newMoves) {
                isResignation = false;
                outputReceived = true;
                theMove = new MoveObj(moveStr);
                activePlayer = currGame.getStatus().getTeamTurn();

                if (!moveStr.equals("R")) {
                    console.println('\n' + colorToPlayer(activePlayer) + " made a move:");
                    moveDescrip = new StringBuilder(moveStr.substring(0, 2) + " to " + moveStr.substring(3, 5));

                    if (theMove.isCastleMove()) {
                        if (theMove.getEndPosition().getColumn() < theMove.getStartPosition().getColumn()) {
                            moveDescrip.append(" (Queenside castle)");
                        } else {
                            moveDescrip.append(" (Kingside castle)");
                        }
                    }

                    if (theMove.getPromotionPiece() != null) {
                        moveDescrip.append("; Pawn promoted to ");
                        moveDescrip.append(theMove.getPromotionPiece().toString());
                    }
                } else {
                    isResignation = true;
                    moveDescrip = new StringBuilder('\n' + colorToPlayer(activePlayer) + " resigned.");
                }
                console.println(moveDescrip);

                try {
                    currGame.getStatus().makeMove(theMove);
                } catch (InvalidMoveException x) {
                    throw ExceptionConverter.convert(x);
                }

                if (!isResignation) {
                    printBoard();
                }
            }
        }
        catch (NullPointerException x) {
            console.println(newMoves == null);
            console.println(newMoves.size());
            console.println(x.getMessage());
            throw x;
        }

        return outputReceived;
    }

    private static boolean checkForNewLeave() throws ServerFacadeException {
        var newLeaves = serverF.getNewLeaveMsgs();
        StringBuilder leaveMsg;
        boolean outputReceived = false;

        try {
            for (String leavingPerson : newLeaves) {
                outputReceived = true;

                leaveMsg = new StringBuilder('\n' + leavingPerson + " just left");

                console.println(leaveMsg);
            }
        }
        catch (NullPointerException x) {
            console.println(newLeaves == null);
            console.println(newLeaves.size());
            console.println(x.getMessage());
            throw x;
        }
        return outputReceived;
    }

    private static boolean checkForNewJoins() throws ServerFacadeException {
        var newJoins = serverF.getNewJoinMsgs();
        String color;
        String username;
        StringBuilder joinMsg;
        boolean outputReceived = false;
        try {
            for (String newComer : newJoins) {
                outputReceived = true;

                username = newComer.substring(6);
                joinMsg = new StringBuilder('\n' + username + " just joined");

                color = newComer.substring(0, 5);
                if (color.equals("null ")) {
                    joinMsg.append(" as an observer");
                } else {
                    joinMsg.append(" as " + color);
                    currGame.setPlayer(username, TeamColor.valueOf(color));
                }

                console.println(joinMsg);
            }
        }
        catch (NullPointerException x) {
            console.println(newJoins == null);
            console.println(newJoins.size());
            console.println(x.getMessage());
            throw x;
        }
        return outputReceived;
    }

    private static boolean parseMove(String moveStr) throws IOException, InvalidMoveException {
        String pos1 = moveStr.substring(0, 2);
        String pos2 = moveStr.substring(3).trim();
        MoveObj moveAttempt;

        boolean validMove = Position.isValidPos(pos1) && Position.isValidPos(pos2);
        if (!validMove) {
            console.println("Improper move format");
            return false;
        }

        moveAttempt = new MoveObj(pos1, pos2);
        Position startPos = moveAttempt.getStartPosition();
        Position endPos = moveAttempt.getEndPosition();

        if (currGame.getStatus().getTeamTurn() != myColor) {
            console.println("It's not your turn");
            return false;
        }
        if (currGame.getStatus().getGameOver()) {
            console.println("Game ended already");
            return false;
        }

        if (currGame.getStatus().getBoard().getPiece(startPos) instanceof King &&
                Math.abs(startPos.getColumn() - endPos.getColumn()) > 1) {
            moveAttempt.markAsCastleMove();
        }

        try {
            makeMove(moveAttempt);
        }
        catch (InvalidMoveException x) {
            validMove = false;
            String msg = x.getMessage();
            if (msg.equals("Must promote pawn")) {
                if (!getPromotion(moveAttempt)) {
                    return false;
                }
                try {
                    makeMove(moveAttempt);
                    validMove = true;
                }
                catch (InvalidMoveException y) {
                    console.println("Sorry, bug in the program. Please contact mattstevemitch@gmail.com");
                    System.exit(1);
                }
            }
            else {
                throw x;
            }
        }
        return validMove;
    }

    private static void playGame(boolean asObserver) throws Exception {
        String menu;
        if (asObserver) {
            menu = observerMenu;
        }
        else {
            menu = gameMenu;
        }

        console.print(menu);
        int readBytes;
        int pendingBytes = 1;
        String userInput;
        boolean outputReceived;
  //      boolean validMove;

        while (currGameID != null) {
            skipValidityCheck = false;

            outputReceived = checkForNotifications();

            if (pendingBytes > 0 || outputReceived) { // If the previous value of pendingBytes was > 0; i.e. if something was input
                console.print(myUsername + " >> ");
            }

            pendingBytes = reader.available();
            buffer = new byte[MAX_USERNAME_SIZE];

            if (pendingBytes > 0) {
                if (pendingBytes > BUFFER_SIZE) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }
                readBytes = reader.read(buffer);

                if (readBytes < pendingBytes) {
                    console.println("Error: Unable to read full command; please try again");
                    reader.read(garbage);
                    continue;
                }

                userInput = new String(buffer).trim().toLowerCase();

                if (checkForCancel(userInput, true)) {
                    break;
                }

                switch (userInput) {
                    case "help":
                        console.print(menu);
                        break;
                    case "move":
                        if (myColor == null) {
                            console.println("Invalid command");
                            break;
                        }
                        else if (currGame.getStatus().getGameOver()) {
                            console.println("Game ended already");
                            break;
                        }
                        if (currGame.getStatus().getTeamTurn() == myColor) {
                            gameMove();
                        }
                        else {
                            console.println("It's not your turn");
                        }
                        break;
                    case "board":
                        printBoard();
                        break;
                    case "resign":
                        if (myColor == null) {
                            console.println("Invalid command");
                            break;
                        }
                        handleResignation();
                        break;
                    case "leave":
                        handleLeaving();
                        break;
                    case "options":
                        handleMoveHighlight();
                        break;
                    default:
                        if (myColor == null) {
                            console.println("Invalid command");
                            break;
                        }

                        if (userInput.length() < 5) {
                            console.println("Invalid command");
                            continue;
                        }

                        if (!skipValidityCheck) {
                            try {
                                parseMove(userInput);
                            }
                            catch (InvalidMoveException x) {
                                console.println(x.getMessage());
                            }
                        }
                }
            }
        }
    }

    private static void refreshWS() {
        try {
            while (LastWSRefresh != null) {
                LocalTime now = LocalTime.now();
                int currMinute = now.getMinute();
                int lastRefreshMinute = LastWSRefresh.getMinute();
                int minuteDiff = currMinute - lastRefreshMinute;
                if (minuteDiff < 0) {
                    minuteDiff += 60;
                }
                int secondDiff = now.getSecond() - LastWSRefresh.getSecond();
                if (minuteDiff > 2 ||
                        (minuteDiff == 2 && secondDiff > 0)) {  // All that to calculate if two minutes have passed
                    serverF.refreshWS();
                    LastWSRefresh = LocalTime.now();
                    //console.println("refreshed " + minuteDiff + " minutes " + secondDiff + " seconds");
                }
            }
        }
        catch (Exception x) {
            String message = x.getMessage();
            if (message != null && !message.startsWith("Cannot invoke \"java.time.LocalTime.")) {
                console.println("Websocket session refresher has crashed. Restart is recommended.");
                console.println(message);
                console.print(myUsername + " >> ");
            }
        }
    }

    private static void handleMoveHighlight() throws Exception {
        String positionStr = null;
        Position posAttempt;
        PieceObj chosenPiece = null;
        boolean validPos = false;
        int pendingBytes = 0;
        console.println("What piece do you want to view legal moves for?");
        console.print("Enter its position (example: E2): ");

        while (!validPos) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print("Enter a position: ");
            }
            pendingBytes = reader.available();
            buffer = new byte[MAX_USERNAME_SIZE];

            if (pendingBytes > 0) {
                if (reader.read(buffer) < pendingBytes) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                positionStr = new String(buffer).trim();

                if (checkForCancel(positionStr, true)) {
                    break;
                }
                else if (positionStr.length() < 2) {
                    continue;
                }

                if (!skipValidityCheck) {

                    validPos = Position.isValidPos(positionStr);
                    if (!validPos) {
                        console.println("Invalid position");
                        continue;
                    }

                    posAttempt = new Position(positionStr);
                    chosenPiece = (PieceObj)currGame.getStatus().getBoard().getPiece(posAttempt);

                    validPos = (chosenPiece != null);

                    if (!validPos) {
                        console.println("No piece in that spot");
                    }
                }
            }
        }

        if (chosenPiece != null) {printHighlightedBoard(chosenPiece);}
    }

    private static void printHighlightedBoard(PieceObj chosenPiece) {
        GameObj theGame = currGame.getStatus();
        BoardObj theBoard = (BoardObj)theGame.getBoard();
        HashSet<ChessMove> legalMoves = (HashSet<ChessMove>)chosenPiece.pieceMoves();

        HashSet<Position> legalSpots = new HashSet<>();
        HashSet<Position> illegalSpots = new HashSet<>();

        for (var move : legalMoves) {
            MoveObj thisMove = (MoveObj)move;
            try {
                theGame.testMoveForCheck((MoveObj)thisMove);
            }
            catch (KingInCheckException x) {
                illegalSpots.add( (Position)thisMove.getEndPosition());
                continue;
            }
            legalSpots.add( (Position)thisMove.getEndPosition());
        }

        theBoard.printBoard(myColor, chosenPiece.getPos(), legalSpots, illegalSpots);
    }

    private static void handleLeaving() throws IOException {
        Boolean confirm = askQuestion("Are you sure you want to exit this game? ");
        if (confirm == null || !confirm) {
            return;
        }
        else {
            try {
                serverF.leaveGameWS(authToken, currGameID);
            }
            catch (IOException x) {
                console.println(x.getMessage());
                System.exit(1);
            }
            currGameID = null;
            currGame = null;
            myColor = null;
        }
    }

    private static void handleResignation() throws IOException {
        Boolean confirm = askQuestion("Are you sure you want to surrender? ");
        if (confirm == null || !confirm) {
            return;
        }
        else {
            try {
                MoveObj resignation = new MoveObj(null, new Position(1, 1));
                currGame.getStatus().makeMove(resignation);
                sendMoveToServer(resignation);
            }
            catch (InvalidMoveException x) {
                console.println(x.getMessage());
                System.exit(1);
            }
        }
    }

    private static void attemptRegistration() throws Exception {
        int pendingBytes = 1;
        String email;
        boolean validEmail = false;
        console.println("Enter a username, password, and email. Enter \"cancel\" at any time to");
        console.println("return to main menu.");

        String username = getUsername(false);
        if (username == null) {
            return;
        }

        console.println("This program does not check to see if your password is secure.");
        console.println("It's your responsibility to secure your account.");

        String password = getNonEmptyInput("Password: ", false);
        if (password == null) {
            return;
        }

        while (!validEmail) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print("Email: ");
            }
            pendingBytes = reader.available();
            buffer = new byte[MAX_EMAIL_SIZE];

            if (pendingBytes > 0) {
                if (reader.read(buffer) < pendingBytes) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                email = new String(buffer).trim();

                if (checkForCancel(email, false)) {
                    return;
                }
                else if (email.length() == 0) {
                    continue;
                }

                if (!skipValidityCheck) {validEmail = sendRegisterRequest(username, password, email);}
            }
        }
        session();
    }

    static private String getNonEmptyInput(String prompt, boolean loggedIn) throws IOException {
        String input = "";
        int pendingBytes = 1;

        while (input.length() == 0) {
            skipValidityCheck = false;
            if (pendingBytes > 0) {
                console.print(prompt);
            }
            pendingBytes = reader.available();
            buffer = new byte[MAX_PASSWORD_SIZE];

            if (pendingBytes > 0) {
                if (reader.read(buffer) < pendingBytes) {
                    console.println("Error: too many characters; please try again");
                    reader.read(garbage);
                    continue;
                }

                input = new String(buffer).trim();

                if (checkForCancel(input, loggedIn)) {
                    input = null;
                    break;
                }
                if (skipValidityCheck) {input = "";}
            }
        }

        return input;
    }

    private static models.Game constructGame(int gameID) throws IOException, ServerFacadeException {
        serverF.requestBoard(authToken, gameID);
        BoardObj latestBoard = null;
        GameBasicInfo thisGameInfo = null;
        TeamColor activePlayer;
        var newBoardUpdates = new ArrayList<BoardObj>();

        while (newBoardUpdates.isEmpty()) {
            newBoardUpdates = serverF.getBoardUpdates();
        }

        while (!newBoardUpdates.isEmpty()) {
            latestBoard = newBoardUpdates.get(newBoardUpdates.size() - 1);

            updateGameList();

            for (var game : gameList) {
                if (game.gameID == gameID) {
                    thisGameInfo = game;
                }
            }
            newBoardUpdates = serverF.getBoardUpdates();
        }

        if (thisGameInfo.whiteActive) {
            activePlayer = TeamColor.WHITE;
        }
        else {
            activePlayer = TeamColor.BLACK;
        }

        GameObj status = new GameObj(latestBoard, activePlayer);

        return new models.Game(gameID, thisGameInfo.gameName, status, null,
                thisGameInfo.whiteUsername, thisGameInfo.blackUsername);
    }

    static private void printBoard() {
        ((BoardObj)currGame.getStatus().getBoard()).printBoard(myColor);
    }

    static private boolean joinGame(int gameID, TeamColor color)
            throws Exception {
        JoinGameResponse result = serverF.joinGame(authToken, gameID, color);
        boolean success = true;

        if (result.getResponseCode() == 200) {
            try {
                serverF.webSocket(authToken, gameID, color);
            }
            catch (ServerFacadeException x) {
                console.println(x.getMessage());
                success = false;
            }

            if (success) {
                if (color != null) {
                    console.println("Successfully joined game");
                } else {
                    console.println("Successfully joined as observer");
                }

                myColor = color;

                currGame = constructGame(gameID);
                currGameID = gameID;
                LastWSRefresh = LocalTime.now();
                Thread WSConnMaint = new Thread(Client::refreshWS);
                WSConnMaint.start();
                printBoard();

                playGame(color == null);

                currGameID = null;
                myColor = null;
                currGame = null;
                LastWSRefresh = null;
            }
        }
        else {
            console.println(result.getMessage());
            success = false;
        }

        return success;
    }

    static private boolean sendRegisterRequest(String username, String password, String email) {
        RegisterResponse result;
        try {
            result = serverF.register(username, password, email);
        }
        catch (ServerFacadeException e) {
            console.println(e.getMessage());
            return false;
        }

        if (result.getResponseCode() == 200) {
            console.println("Account successfully created");
            authToken = result.getAuthToken();
            myUsername = result.getUsername();
            return true;
        }
        else {
            console.println(result.getMessage());
            return false;
        }
    }

    public static boolean sendLogoutRequest() {
        LogoutResponse result;
        try {
            result = serverF.logout(authToken);
        }
        catch (ServerFacadeException e) {
            console.println(e.getMessage());
            return false;
        }

        if (result.getResponseCode() == 200) {
            loggedIn = false;
            myUsername = null;
            console.println("Logged out successfully\n");
            console.print(loggedOutMenu);
            return true;
        }
        else {
            console.println(result.getMessage());
            return false;
        }
    }
}
//FIXME: simplify board notifications?