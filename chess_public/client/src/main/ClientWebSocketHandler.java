import javax.websocket.MessageHandler;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import com.google.gson.Gson;

public class ClientWebSocketHandler implements MessageHandler.Whole<String> {
 /*   private ArrayList<String> unreadErrorMsgs;
    private ReentrantLock errorMsgLock = new ReentrantLock();*/
    private String myUsername;
    private ArrayList<String> unreadMoveMsgs;
    private ReentrantLock moveMsgLock = new ReentrantLock();
    private ArrayList<String> unreadPrevMoveMsgs;
    private ReentrantLock prevMovesMsgLock = new ReentrantLock();
    private ArrayList<String> unreadJoinMsgs;
    private ReentrantLock joinMsgLock = new ReentrantLock();
    private ArrayList<String> unreadLeaveMsgs;
    private ReentrantLock leaveMsgLock = new ReentrantLock();
    private Gson serializer;
    private boolean newPrevMoveMsg;

    public ClientWebSocketHandler(String username) {
        unreadJoinMsgs = new ArrayList<>();
        unreadMoveMsgs = new ArrayList<>();
        unreadPrevMoveMsgs = new ArrayList<>();
        unreadLeaveMsgs = new ArrayList<>();
        serializer = new Gson();
        myUsername = username;
        newPrevMoveMsg = false;
    }

    public void onMessage(String serverOutput) {

        if (serverOutput.startsWith("PrevMoves: ")) {
            moveMsgLock.lock();
            unreadMoveMsgs.clear();
            moveMsgLock.unlock();
            ArrayList<String> newMoves = serializer.fromJson(serverOutput.substring(11),
                    ArrayList.class);
            prevMovesMsgLock.lock();
            newPrevMoveMsg = true;
            if (unreadPrevMoveMsgs.isEmpty()) {unreadPrevMoveMsgs = newMoves;}
            else {unreadPrevMoveMsgs.addAll(newMoves);}
            prevMovesMsgLock.unlock();
        }
        else if (serverOutput.startsWith("Move: ")) {
            moveMsgLock.lock();
            unreadMoveMsgs.add(serverOutput.substring(6));
            moveMsgLock.unlock();
        }
        else if (serverOutput.startsWith("Error: ")) {
            System.out.println(serverOutput.substring(7));
            System.out.print(myUsername + " >> ");
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

    public ArrayList<String> getOldMoveMsgs() {
        prevMovesMsgLock.lock();
        ArrayList<String> returnVal = new ArrayList<>(unreadPrevMoveMsgs);
        unreadPrevMoveMsgs.clear();
        newPrevMoveMsg = false;
        prevMovesMsgLock.unlock();

        return returnVal;
    }

    public boolean prevMoveMsgAvailable() {
        return newPrevMoveMsg;
    }

    public ArrayList<String> getNewJoinMsgs() {
        joinMsgLock.lock();
        ArrayList<String> returnVal = new ArrayList<>(unreadJoinMsgs);
        unreadJoinMsgs.clear();
        joinMsgLock.unlock();

        return returnVal;
    }
}
