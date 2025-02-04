package chess;

public class KingInCheckException extends InvalidMoveException {
    public KingInCheckException() {
        super("Move leaves the king in check");
    }
}