package ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.ProtocolException;
import javax.websocket.DeploymentException;
import chess.InvalidMoveException;

public class ExceptionConverter {
    public static ServerFacadeException convert(Exception x) {
        ServerFacadeException returnVal;
        if (x instanceof IOException) {
            if (x instanceof MalformedURLException) {
                returnVal = new ServerFacadeException(x, "MalformedURLException");
            }
            else if (x instanceof ProtocolException) {
                returnVal = new ServerFacadeException(x, "ProtocolException");
            }
            else returnVal = new ServerFacadeException(x, "IOException");
        }

        else if (x instanceof URISyntaxException) {
            returnVal = new ServerFacadeException(x, "URISyntaxException");
        }
        else if (x instanceof DeploymentException) {
            returnVal = new ServerFacadeException(x, "DeploymentException");
        }
        else if (x instanceof InvalidMoveException) {
            returnVal = new ServerFacadeException(x, "InvalidMoveException");
        }
        else {
            returnVal = new ServerFacadeException(x, "Unnamed Exception");
        }

        return returnVal;
    }

   // public static ServerFacadeException convert(URISyntaxException x) {
     //   return new ServerFacadeException(x);
 //   }
}
