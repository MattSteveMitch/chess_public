package server.dataAccess;
// Created a new class so that when passed
// as null in an object array, it can still be
// recognized as a boolean
public class NullableBoolean {
    public NullableBoolean(Boolean val) {
        this.val = val;
    }
    public Boolean val;
}
