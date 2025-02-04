package chess;

import java.util.HashSet;

public class Queen extends PieceObj {
    public Queen(BoardObj board, ChessGame.TeamColor color, Position pos) {
        super(board, color, pos);
        type = PieceType.QUEEN;
    }

    public Queen(Queen other, BoardObj board) {
        super(other, board);
    }

    public void updatePieceMoves() {
        legalMoves.clear();
        getBishopMoves();
        getRookMoves();
    }

    public HashSet<ChessMove> pieceMoves() {
        return legalMoves;
    }
}