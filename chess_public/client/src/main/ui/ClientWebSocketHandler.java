package ui;
import javax.websocket.MessageHandler;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ClientWebSocketHandler implements MessageHandler.Whole<String> {
 /*   private ArrayList<String> unreadErrorMsgs;
    private ReentrantLock errorMsgLock = new ReentrantLock();*/
    private ArrayList<String> unreadMoveMsgs;
    private ReentrantLock moveMsgLock = new ReentrantLock();
    private ArrayList<String> unreadBoardMsgs;
    private ReentrantLock boardMsgLock = new ReentrantLock();
    private ArrayList<String> unreadJoinMsgs;
    private ReentrantLock joinMsgLock = new ReentrantLock();
    private ArrayList<String> unreadLeaveMsgs;
    private ReentrantLock leaveMsgLock = new ReentrantLock();

    public ClientWebSocketHandler() {
        unreadJoinMsgs = new ArrayList<>();
        unreadMoveMsgs = new ArrayList<>();
        unreadBoardMsgs = new ArrayList<>();
        unreadLeaveMsgs = new ArrayList<>();
    }

    public void onMessage(String serverOutput) {

        if (serverOutput.startsWith("Board: ")) {
            boardMsgLock.lock();
            unreadBoardMsgs.add(serverOutput.substring(7));
            boardMsgLock.unlock();
        }
        else if (serverOutput.startsWith("Move: ")) {
            moveMsgLock.lock();
            unreadMoveMsgs.add(serverOutput.substring(6));
            moveMsgLock.unlock();
        }
        else if (serverOutput.startsWith("Error: ")) {
            System.out.println(serverOutput.substring(0, 7));
            //System.exit(1);
        }
        else if (serverOutput.startsWith("Joining: ")) {
            joinMsgLock.lock();
            unreadJoinMsgs.add(serverOutput.substring(9));
            joinMsgLock.unlock();
        }
        if (serverOutput.startsWith("Leaving: ")) {
            leaveMsgLock.lock();
            unreadLeaveMsgs.add(serverOutput.substring(9));
            leaveMsgLock.unlock();
        }
    }

  /*  public ArrayList<String> getNewErrMsgs() {
        errorMsgLock.lock();
        ArrayList<String> returnVal = new ArrayList<>(unreadErrorMsgs);
        unreadErrorMsgs.clear();
        errorMsgLock.unlock();

        return returnVal;
    }*/

    public ArrayList<String> getNewMoveMsgs() {
        moveMsgLock.lock();
        ArrayList<String> returnVal = new ArrayList<>(unreadMoveMsgs);
        unreadMoveMsgs.clear();
        moveMsgLock.unlock();

        return returnVal;
    }

    public ArrayList<String> getNewLeaveMsgs() {
        leaveMsgLock.lock();
        ArrayList<String> returnVal = new ArrayList<>(unreadLeaveMsgs);
        unreadLeaveMsgs.clear();
        leaveMsgLock.unlock();

        return returnVal;
    }

    public ArrayList<String> getNewBoardMsgs() {
        boardMsgLock.lock();
        ArrayList<String> returnVal = new ArrayList<>(unreadBoardMsgs);
        unreadBoardMsgs.clear();
        boardMsgLock.unlock();

        return returnVal;
    }

    public ArrayList<String> getNewJoinMsgs() {
        joinMsgLock.lock();
        ArrayList<String> returnVal = new ArrayList<>(unreadJoinMsgs);
        unreadJoinMsgs.clear();
        joinMsgLock.unlock();

        return returnVal;
    }
}
