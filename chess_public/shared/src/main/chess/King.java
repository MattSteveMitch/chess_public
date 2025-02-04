package chess;

import java.util.HashSet;

public class King extends RKP {

    public King(BoardObj board, ChessGame.TeamColor color, Position pos) {
        super(board, color, pos);
        type = PieceType.KING;
    }

    public King(BoardObj board, ChessGame.TeamColor color, Position pos, boolean hasMoved) {
        super(board, color, pos);
        type = PieceType.KING;
        this.hasMoved = hasMoved;
    }

    public King(King other, BoardObj board) {
        super(other, board);
    }

    public HashSet<ChessMove> pieceMoves() {
        return legalMoves;
    }

    public void updatePieceMoves() {
        legalMoves.clear();
        Position endPos;
        int[] directions = {-1, 1};
        for (int r : directions) {
            for (int c : directions) {
                if (Position.isValidAddition(pos, r, c)) {
                    endPos = pos.plus(r, c);
                    board.addVulnerableSpot(color, endPos);
                    if (board.getPiece(endPos) == null ||
                            board.getPiece(endPos).getTeamColor() == GameObj.otherTeam(color)) {
                        legalMoves.add(new MoveObj(pos, endPos));
                    }
                }
            }
            int C = 0;
            for (int k = 0; k < 2; k++) {
                if (Position.isValidAddition(pos, r, C)) {
                    endPos = pos.plus(r, C);
                    board.addVulnerableSpot(color, endPos);
                    if (board.getPiece(endPos) == null ||
                            board.getPiece(endPos).getTeamColor() == GameObj.otherTeam(color)) {
                        legalMoves.add(new MoveObj(pos, endPos));
                    }
                }
                C = r;
                r = 0;
            }
        }
    }

    private void getCastle(boolean isKingside) {
        int direction;
        if (isKingside) {direction = 1;}
        else {direction = -1;}

        Position posInQuestion = null;
        Rook theRook = null;
        ChessPiece cornerPiece = null;
        if (isKingside) {
            cornerPiece = board.getPiece(pos.addToColumn(3));
        }
        else {
            cornerPiece = board.getPiece(pos.addToColumn(-4));
        }

        posInQuestion = pos.addToColumn(direction);
        if (board.getPiece(posInQuestion) != null ||
                board.isVulnerableSpot(posInQuestion, GameObj.otherTeam(color))) {
            return;
        }
        posInQuestion = pos.addToColumn(2 * direction);
        if (board.getPiece(posInQuestion) != null ||
                board.isVulnerableSpot(posInQuestion, GameObj.otherTeam(color))) {
            return;
        }
        if (!isKingside && board.getPiece(pos.addToColumn(-3)) != null) {
            return;
        }

        if (cornerPiece != null && cornerPiece.getPieceType() == PieceType.ROOK) {
            theRook = (Rook) cornerPiece;
            if (!theRook.getMovedStatus()) {
                legalMoves.add(new MoveObj(pos, pos.addToColumn(2 * direction), true));
            }
        }
    }

    public void getCastleMoves() {
        if (hasMoved || board.getGame().isInCheck(color)) {return;}
        getCastle(true);
        getCastle(false);
    }
}
