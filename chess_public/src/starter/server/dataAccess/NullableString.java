package server.dataAccess;
// Created a new class so that when passed
// as null in an object array, it can still be
// recognized as a string
public class NullableString {
    public NullableString(String val) {
        this.val = val;
    }
    public String val;
}
