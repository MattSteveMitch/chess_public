package Responses;

public class LeaveGameResponse extends Response {
    public LeaveGameResponse(int code) {
        if (code != 200) {
            System.out.println("Non-success code should specify error message");
            System.exit(0);
        }
        responseCode = 200;
    }

    public LeaveGameResponse(int code, String message) {
        super(code, message);
        if (code == 200) {
            System.out.println("Success code does not require message");
        }
    }
}
