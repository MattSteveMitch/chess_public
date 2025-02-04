package chess;

import java.util.HashSet;

public class Bishop extends PieceObj {
    public Bishop(BoardObj board, ChessGame.TeamColor color, Position pos) {
        super(board, color, pos);
        type = PieceType.BISHOP;
    }

    public Bishop(Bishop other, BoardObj board) {
        super(other, board);
    }

    public HashSet<ChessMove> pieceMoves() {
        return legalMoves;
    }

    public void updatePieceMoves() {
        legalMoves.clear();
        getBishopMoves();
    }
}