package chess;
import chess.ChessPiece.PieceType;
import java.util.HashSet;

public class MoveObj implements ChessMove {
    public MoveObj(Position start, Position end) {
        startPos = start;
        endPos = end;
        promotionPiece = null;
        isCastleMove = false;
        leavesKingInCheck = false;
    }

    public String toString() {
        if (startPos == null || endPos == null) {
            return "R";     // If either position is null, it indicates that this move is a resignation
        }
        String special = "";
        if (isCastleMove) {
            special = " C";
        }
        else if (promotionPiece != null) {
            special = " " + promotionPiece.toString().charAt(0);
        }
        return startPos.toString() + ' ' + endPos.toString() + special;
    }

    public MoveObj(String startStr, String endStr) {
        this.startPos = new Position(startStr);
        this.endPos = new Position(endStr);
    }

    public MoveObj(Position start, Position end, boolean isCastle) {
        startPos = start;
        endPos = end;
        promotionPiece = null;
        isCastleMove = isCastle;
        leavesKingInCheck = false;
    }

 /*   public static boolean containsEndPos(HashSet<MoveObj> moves, Position endPos) {
        for (var thisMove : moves) {
            if (thisMove.getEndPosition().equals(endPos)) {
                return true;
            }
        }
        return false;
    }*/

    public MoveObj(String move) throws InvalidPositionException {
        String moveStr = move.trim().toUpperCase();
        if (moveStr.length() < 1) {
            throw new InvalidPositionException("Empty string");
        }

        if (moveStr.startsWith("R")) {
            this.startPos = null; // If either position is null, it indicates that this move is a resignation
            this.endPos = null;
            return;
        }

        if (moveStr.length() < 5) {
            throw new InvalidPositionException("String is too short");
        }

        String pos1Str = moveStr.substring(0, 2);

        String pos2Str = moveStr.substring(3, 5);

        if (!Position.isValidPos(pos1Str) || !Position.isValidPos(pos2Str)) {
            System.out.println("Error deserializing move");
            System.exit(1);
        }
        Position pos1 = new Position(pos1Str);
        Position pos2 = new Position(pos2Str);

        startPos = pos1;
        endPos = pos2;

        char finalChar;
        if (move.length() > 6) {
            finalChar = move.charAt(6);
            switch (finalChar) {
                case 'C':
                    this.markAsCastleMove();
                    break;
                case 'Q':
                    this.addPromotion(PieceType.QUEEN);
                    break;
                case 'K':
                    this.addPromotion(PieceType.KNIGHT);
                    break;
                case 'R':
                    this.addPromotion(PieceType.ROOK);
                    break;
                case 'B':
                    this.addPromotion(PieceType.BISHOP);
                    break;
                default:
                    System.out.println("Invalid final character");
                    System.exit(1);
            }
        }
    }

    public MoveObj(Position start, Position end, ChessPiece.PieceType promotionPiece) {
        startPos = start;
        endPos = end;
        isCastleMove = false;
        leavesKingInCheck = false;
        this.addPromotion(promotionPiece);
    }

    public boolean equals(Object other) {
        if (!(other instanceof MoveObj)) {return false;}
        MoveObj otherCast = (MoveObj)other;
        return (startPos.equals(otherCast.startPos) && endPos.equals(otherCast.endPos) &&
                promotionPiece == otherCast.promotionPiece &&
                isCastleMove == otherCast.isCastleMove &&
                leavesKingInCheck == otherCast.leavesKingInCheck);
    }

    public boolean isCastleMove() {
        return isCastleMove;
    }
    public void markAsCastleMove() {
        isCastleMove = true;
    }

    public int hashCode() { // I have no idea if the hashcode needs to be this complex, probably not
        int p;
        int dangerFlag;
        int castleFlag;
        if (leavesKingInCheck) {dangerFlag = 1;}
        else {dangerFlag = 0;}
        if (isCastleMove) {castleFlag = 1;}
        else {castleFlag = 0;}
        if (promotionPiece != null) {p = promotionPiece.ordinal();}
        else {p = 11;}
        return (startPos.getColumn() + 3) * (startPos.getRow() + 7) * (castleFlag + 2) *
                (endPos.getRow() + 5) * (endPos.getColumn() + 11) * (dangerFlag + 1) *
                (p + 17) + p + startPos.getColumn() + startPos.getRow() + castleFlag +
                endPos.getColumn() + endPos.getRow() + dangerFlag + 13;
    }

    public Position getStartPosition() {return startPos;}

    public Position getEndPosition() {return endPos;}

    public ChessPiece.PieceType getPromotionPiece() {return promotionPiece;}

    public void setAsDangerous() {
        leavesKingInCheck = true;
    }

    public boolean getKingInCheck() {
        return leavesKingInCheck;
    }

    public void addPromotion(ChessPiece.PieceType type) {
        if (type == PieceType.PAWN || type == PieceType.KING) {
            System.out.println("Error in MoveObj: Invalid promotion piece");
            System.exit(1);
        }
        promotionPiece = type;
    }

    public void removePromotion() {
        promotionPiece = null;
    }

    private boolean leavesKingInCheck;
    private Position startPos;
    private Position endPos;
    public ChessPiece.PieceType promotionPiece;
    public boolean isCastleMove;
}
