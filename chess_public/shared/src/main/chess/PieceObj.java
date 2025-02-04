package chess;

import java.util.HashSet;

public abstract class PieceObj implements ChessPiece {
    public PieceObj(BoardObj board, ChessGame.TeamColor color, Position pos) {
        this.board = board;
        this.color = color;
        this.pos = pos;
        this.legalMoves = new HashSet<ChessMove>();
    }

    public PieceObj(PieceObj other, BoardObj board) {
        this.board = board;
        this.color = other.color;
        this.pos = new Position(other.pos);
        this.legalMoves = new HashSet<ChessMove>();
        this.type = other.type;
    }

    public boolean equals(PieceObj otherP) {
        if (otherP == null) {
            return false;
        }
        //PieceObj otherP = (PieceObj)otherP;
        boolean returnVal;
        returnVal = this.type == otherP.type && this.color == otherP.color;
        if (returnVal == true && this instanceof RKP &&
                ((RKP)this).hasMoved != ((RKP)otherP).hasMoved) {
            returnVal = false;
        }
        if (returnVal == true && this instanceof Pawn && ((Pawn)this).getVulnerability() !=
                ((Pawn)otherP).getVulnerability()) {
            returnVal = false;
        }

        return returnVal;
    }

    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    public PieceType getPieceType() {
        return type;
    }

    public void relocate(Position newPos) {
        pos = newPos;
    }

    public Position getPos() {
        return pos;
    }

    abstract public void updatePieceMoves();

    // I want the queen to be able to access getBishopMoves and getRookMoves
    protected void getBishopMoves() {
        Position extremePos;

        int[] directions = {-1, 1};

        for (int i : directions) {
            for (int j : directions) {
                extremePos = pos;
                while (Position.isValidAddition(extremePos, i, j)) {
                    extremePos = extremePos.plus(i, j);
                }
                getMoveLegalitiesForBishop(extremePos, legalMoves);
            }
        }
    }

    protected void getRookMoves() {
        Position extremePos;

        int[] directions = {-1, 1};

        for (int i : directions) {
            extremePos = pos;
            while ((i == 1 && extremePos.getRow() < 8) ||
                    (i == -1 && extremePos.getRow() > 1)) {
                extremePos = extremePos.addToRow(i);
            }
            getMoveLegalitiesForRook(extremePos, legalMoves);
        }

        for (int i : directions) {
            extremePos = pos;
            while ((i == 1 && extremePos.getColumn() < 8) ||
                    (i == -1 && extremePos.getColumn() > 1)) {
                extremePos = extremePos.addToColumn(i);
            }
            getMoveLegalitiesForRook(extremePos, legalMoves);
        }
    }

    protected int getMoveLegalitiesForRook(Position posInQuestion, HashSet<ChessMove> moves) {
        /* Get and record the legality of moves to all
        squares from current pos to posInQuestion (recursive)*/
        Position prevSquare = null;
        int prevSquareLegality = 3;

        if (posInQuestion.getColumn() > pos.getColumn()) {
            prevSquare = posInQuestion.addToColumn(-1);
        }
        else if (posInQuestion.getColumn() < pos.getColumn()) {
            prevSquare = posInQuestion.addToColumn(1);
        }
        else if (posInQuestion.getRow() > pos.getRow()) {
            prevSquare = posInQuestion.addToRow(-1);
        }
        else if (posInQuestion.getRow() < pos.getRow()) {
            prevSquare = posInQuestion.addToRow(1);
        }

        if (prevSquare != null) {
            prevSquareLegality = getMoveLegalitiesForRook(prevSquare, moves);
        }

        if (board.getPiece(posInQuestion) != null &&
                board.getPiece(posInQuestion).getTeamColor() == this.color) {
            return 0;
        }

        if (prevSquareLegality == 3) {System.out.println("Error in Rook.isLegalMove()");}

        if (prevSquare.equals(pos) || prevSquareLegality == 2) {
            moves.add(new MoveObj(pos, posInQuestion));
            board.addVulnerableSpot(color, posInQuestion);

            if (board.getPiece(posInQuestion) == null) {
                return 2;
            }
            else {
                return 1;
            }
        }

        return 0;
    }

    protected int getMoveLegalitiesForBishop(Position posInQuestion, HashSet<ChessMove> moves) {
        /* Get and record the legality of moves to all
        squares from current pos to posInQuestion (recursive)*/
        Position prevSquare = null;
        int prevSquareLegality = 3;

        if (posInQuestion.getColumn() > pos.getColumn() &&
                posInQuestion.getRow() > pos.getRow()) {
            prevSquare = posInQuestion.plus(-1, -1);
        }
        else if (posInQuestion.getColumn() > pos.getColumn() &&
                posInQuestion.getRow() < pos.getRow()) {
            prevSquare = posInQuestion.plus(1, -1);
        }
        else if (posInQuestion.getColumn() < pos.getColumn() &&
                posInQuestion.getRow() < pos.getRow()) {
            prevSquare = posInQuestion.plus(1, 1);
        }
        else if (posInQuestion.getColumn() < pos.getColumn() &&
                posInQuestion.getRow() > pos.getRow()) {
            prevSquare = posInQuestion.plus(-1, 1);
        }

        if (prevSquare != null) {
            prevSquareLegality = getMoveLegalitiesForBishop(prevSquare, moves);
        }

        if (board.getPiece(posInQuestion) != null &&
                board.getPiece(posInQuestion).getTeamColor() == this.color) {
            return 0;
        }

        if (prevSquareLegality == 3) {System.out.println("Error in Bishop.isLegalMove()");}

        if (prevSquare.equals(pos) || prevSquareLegality == 2) {
            moves.add(new MoveObj(pos, posInQuestion));
            board.addVulnerableSpot(color, posInQuestion);

            if (board.getPiece(posInQuestion) == null) {
                return 2;
            }
            else {
                return 1;
            }
        }

        return 0;
    }
    protected BoardObj board;
    protected ChessGame.TeamColor color;
    protected PieceType type;
    protected Position pos;
    protected HashSet<ChessMove> legalMoves;
}
