package chess;

public abstract class RKP extends PieceObj { // Rook, King, and Pawn objects;
    // these need a flag to indicate
    // whether they've moved yet
    public RKP(BoardObj board, ChessGame.TeamColor color, Position pos) {
        super(board, color, pos);
        hasMoved = false;
    }

    public RKP(RKP other, BoardObj board) {
        super(other, board);
        hasMoved = other.hasMoved;
    }

    public boolean getMovedStatus() {
        return hasMoved;
    }

    public void hasNowMoved() {
        hasMoved = true;
    }

    protected boolean hasMoved;
}