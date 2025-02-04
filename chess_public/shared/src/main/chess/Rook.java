package chess;

import java.util.HashSet;

public class Rook extends RKP {
    public Rook(BoardObj board, ChessGame.TeamColor color, Position pos) {
        super(board, color, pos);
        type = PieceType.ROOK;
    }

    public Rook(BoardObj board, ChessGame.TeamColor color, Position pos, boolean hasMoved) {
        super(board, color, pos);
        type = PieceType.ROOK;
        this.hasMoved = hasMoved;
    }

    public Rook(Rook other, BoardObj board) {
        super(other, board);
    }

    public HashSet<ChessMove> pieceMoves() {
        return legalMoves;
    }

    public void updatePieceMoves() {
        legalMoves.clear();
        getRookMoves();
    }
}
