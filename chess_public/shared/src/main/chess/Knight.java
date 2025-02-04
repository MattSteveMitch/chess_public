package chess;

import java.util.HashSet;

public class Knight extends PieceObj {
    public Knight(BoardObj board, ChessGame.TeamColor color, Position pos) {
        super(board, color, pos);
        type = PieceType.KNIGHT;
    }

    public Knight(Knight other, BoardObj board) {
        super(other, board);
    }

    public HashSet<ChessMove> pieceMoves() {
        return legalMoves;
    }

    public void updatePieceMoves() {
        legalMoves.clear();
        int[][] matrix = {{-2, 2}, {-1, 1}};
        Position posInQuestion;

        for (int r : matrix[0]) {
            for (int c : matrix[1]) {
                for (int k = 0; k < 2; k++) {
                    if (Position.isValidAddition(pos, r, c)) {
                        posInQuestion = pos.plus(r, c);
                        board.addVulnerableSpot(color, posInQuestion);
                        if (board.getPiece(posInQuestion) == null ||
                                board.getPiece(posInQuestion).getTeamColor() == GameObj.otherTeam(color)) {
                            legalMoves.add(new MoveObj(pos, posInQuestion));
                        }
                    }
                    int p = r;
                    r = c;
                    c = p;
                }
            }
        }
    }
}
