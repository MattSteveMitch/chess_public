package Requests;
/**
 * A request to clear all data on server
 */
public class ClearRequest extends Request {
    public ClearRequest() {
        type = RequestType.CLEAR;
    }
}
