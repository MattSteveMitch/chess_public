package chess;

import java.util.HashSet;

public class Pawn extends RKP {
    public Pawn(BoardObj board, ChessGame.TeamColor color, Position pos) {
        super(board, color, pos);
        type = PieceType.PAWN;
        vulnerableToEnPassant = false;
    }

    public Pawn(BoardObj board, ChessGame.TeamColor color, Position pos,
                boolean hasMoved, boolean vulnerableToEnPassant) {
        super(board, color, pos);
        type = PieceType.PAWN;
        this.hasMoved = hasMoved;
        this.vulnerableToEnPassant = vulnerableToEnPassant;
    }

    public Pawn(Pawn other, BoardObj board) {
        super(other, board);
        vulnerableToEnPassant = other.vulnerableToEnPassant;
    }

    public HashSet<ChessMove> pieceMoves() {
        return legalMoves;
    }

    private void getAttackedSpot(Position attackedPos, boolean promotion) {
        board.addVulnerableSpot(color, attackedPos);
        if (board.getPiece(attackedPos) != null &&
                board.getPiece(attackedPos).getTeamColor() == GameObj.otherTeam(color)) {
            addMove(pos, attackedPos, promotion);
        }
    }

    private void getEnPassantSpot(int vertDirection, int horizDirection) {
        ChessPiece horizNeighbor = board.getPiece(pos.addToColumn(horizDirection));
        if (horizNeighbor != null && horizNeighbor.getPieceType() == PieceType.PAWN) {
            Pawn neighborPawn = (Pawn) horizNeighbor;
            if (neighborPawn.color == GameObj.otherTeam(color) &&
                    neighborPawn.vulnerableToEnPassant) {
                legalMoves.add(new MoveObj(pos, pos.plus(vertDirection, horizDirection)));
            }
        }
    }

    public void updatePieceMoves() {
        legalMoves.clear();
        Position attackedPos;
        ChessPiece horizNeighbor;

        boolean promotion = false;
        boolean isFarRight = (pos.getColumn() == 8);
        boolean isFarLeft = (pos.getColumn() == 1);

        int rowAddend;
        if (color == ChessGame.TeamColor.WHITE) {rowAddend = 1;}
        else {rowAddend = -1;}

        if ((color == ChessGame.TeamColor.WHITE && pos.getRow() == 7) ||
                (color == ChessGame.TeamColor.BLACK && pos.getRow() == 2)) {promotion = true;}

        if (board.getPiece(pos.addToRow(rowAddend)) == null) {
            if (!hasMoved && board.getPiece(pos.addToRow(2 * rowAddend)) == null) {
                legalMoves.add(new MoveObj(pos, pos.addToRow(2 * rowAddend)));
            }
            addMove(pos, pos.addToRow(rowAddend), promotion);
        }

        if (!isFarLeft) {
            attackedPos = pos.plus(rowAddend, -1);
            getAttackedSpot(attackedPos, promotion);
        }

        if (!isFarRight) {
            attackedPos = pos.plus(rowAddend, 1);
            getAttackedSpot(attackedPos, promotion);
        }

        if (!isFarRight) {
            getEnPassantSpot(rowAddend, 1);
        }

        if (!isFarLeft) {
            getEnPassantSpot(rowAddend, -1);
        }
    }

    public void addMove(Position startPos, Position endPos, boolean promotion) {
        if (promotion && endPos.getRow() != 8 && endPos.getRow() != 1) {
            System.out.println("Error: Invalid pawn promotion attempted");
            System.exit(1);
        }
        if (!promotion) {
            legalMoves.add(new MoveObj(startPos, endPos));
        }
        else {
            PieceType[] types = {PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN, PieceType.ROOK};
            for (int i = 0; i < 4; i++) {
                legalMoves.add(new MoveObj(startPos, endPos, types[i]));
            }
        }
    }

    public boolean getVulnerability() {
        return vulnerableToEnPassant;
    }

    public void setVulnerability(boolean status) {
        vulnerableToEnPassant = status;
    }

    private boolean vulnerableToEnPassant;
}