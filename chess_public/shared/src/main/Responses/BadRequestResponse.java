package Responses;

public class BadRequestResponse extends Response {
    public BadRequestResponse(String message) {
        responseCode = 400;
        this.message = message;
    }
}
