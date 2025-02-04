import java.net.URISyntaxException;

public class ServerFacadeException extends Exception {

    public ServerFacadeException(Exception x, String type) {
        super(type + ": " + x.getMessage());
    }

    public ServerFacadeException(String message) {
        super(message);
    }
}
