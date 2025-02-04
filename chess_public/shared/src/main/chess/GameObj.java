package chess;

import java.util.ArrayList;
import java.util.HashSet;

public class GameObj implements ChessGame {
    public GameObj() {
        board = new BoardObj(this, true);
        vulnerableTo = new ArrayList<HashSet<Position>>();
        vulnerableTo.add(new HashSet<Position>());
        vulnerableTo.add(new HashSet<Position>());
        currTurn = TeamColor.WHITE;
        updateLegalMoves(board);
    }

    public GameObj(BoardObj theBoard, TeamColor activePlayer, boolean gameOver, TeamColor winner) {
        board = theBoard;
        this.gameOver = gameOver;
        this.winner = winner;
        theBoard.setGame(this);
        currTurn = activePlayer;
        vulnerableTo = new ArrayList<HashSet<Position>>();
        vulnerableTo.add(new HashSet<Position>());
        vulnerableTo.add(new HashSet<Position>());
        currTurn = activePlayer;
        updateLegalMoves(board);
    }

    public ChessGame.TeamColor getTeamTurn() {
        return currTurn;
    }

    public void setTeamTurn(ChessGame.TeamColor team) {
        currTurn = team;
    }

    public HashSet<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {return new HashSet<ChessMove>();}
        else {return (HashSet<ChessMove>)piece.pieceMoves();}
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (gameOver) {
            throw new InvalidMoveException("Game already ended");
        }
        inCheck = null;
        MoveObj thisMove = (MoveObj) move;
        Position startPos = thisMove.getStartPosition();
        Position endPos = thisMove.getEndPosition();

        if (startPos == null || endPos == null) {
            declareVictory(otherTeam(currTurn));
            return;
        }

        PieceObj piece = (PieceObj)board.getPiece(startPos);

        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }

        TeamColor team = board.getPiece(startPos).getTeamColor();
        if (team != currTurn) {
            throw new InvalidMoveException("Piece is not your color");
        }

        HashSet<ChessMove> legalMoves = allValidMoves(team);
        String errorMsg;
        if (thisMove.isCastleMove()) {
            errorMsg = "Cannot castle at this time";
        }
        else {
            errorMsg = "Well yes, outstanding move, but it's illegal";
        }
        if (!legalMoves.contains(thisMove)) {
            thisMove.addPromotion(ChessPiece.PieceType.QUEEN);
            if (legalMoves.contains(thisMove)) {
                throw new InvalidMoveException("Must promote pawn");
            }
            thisMove.removePromotion();
            thisMove.setAsDangerous();        // Might not have been found because it's not yet marked
            if (legalMoves.contains(thisMove)) {// as leaving the king in check. If marking it as
                throw new KingInCheckException(); // dangerous causes a match to be found, then it's
                // obviously been marked as such in the set of legal moves, meaning it leaves king in check.
            } else {
                throw new InvalidMoveException(errorMsg);
            }
        }

        boolean isEnPassant = false;
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && // If we're moving a pawn,
                endPos.getColumn() != startPos.getColumn() && //and it's moving diagonally
                //(i.e. it's attacking),
                board.getPiece(endPos) == null)               //and its destination is vacant,
        {
            isEnPassant = true;                               //then this is an en passant
        }                                                     //move!

 /*       hypotheticalBoard = new BoardObj(board);
        forceMove(hypotheticalBoard, thisMove, isEnPassant, team);

        var savedVulnerabilityList = new ArrayList<HashSet<Position>>(vulnerableTo);

        updateLegalMoves(hypotheticalBoard);
        if (isInCheck(team, hypotheticalBoard)) {
            throw new KingInCheckException();
        }

        vulnerableTo = new ArrayList<HashSet<Position>>(savedVulnerabilityList);*/

        // If no exceptions have been thrown so far, then the move is fully legal
        forceMove(board, thisMove, isEnPassant, team);

        /*int enPassantVulnerabilityRow;
        if (team == TeamColor.BLACK) {enPassantVulnerabilityRow = 5;}
        else {enPassantVulnerabilityRow = 4;}

        Position pawnSquare;
        Pawn vulnerablePawn;
        ChessPiece pieceInRow;
        for (int col = 1; col <= 8; col++) {
            pawnSquare = new Position(enPassantVulnerabilityRow, col);
            pieceInRow = board.getPiece(pawnSquare);
            if (pieceInRow instanceof Pawn) {
                vulnerablePawn = (Pawn)pieceInRow;
                vulnerablePawn.setVulnerability(false);
            }
        }

        piece.relocate(endPos);
        board.setPiece(startPos, null);
        board.setPiece(endPos, piece);

        if (piece instanceof Pawn) {
            Pawn thisPawn = (Pawn)piece;
            if (!thisPawn.getMovedStatus()) {              // If this is its first move
                int distance = endPos.getRow() - startPos.getRow();
                if (distance == 2 || distance == -2) {   // and it's moving two spaces,
                    thisPawn.setVulnerability(true);   // it's vulnerable to en passant.
                }
            }
        }

        if (piece instanceof RKP) {
            RKP rkpPiece = (RKP)piece;
            rkpPiece.hasNowMoved();
        }

        if (thisMove.isCastleMove()) {
            Position rookPos = findRook(startPos, endPos);
            moveRookForCastle(rookPos);
        }

        if (thisMove.getPromotionPiece() != null) {
            promotePawn(endPos, move.getPromotionPiece());
        }

        if (isEnPassant) {
            if (team == TeamColor.BLACK) {
                board.setPiece(endPos.addToRow(1), null);
            }
            else {
                board.setPiece(endPos.addToRow(-1), null);
            }
        }*/
        //board.printBoard();
        switchTurn();
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor, BoardObj theBoard) {
        Position location = theBoard.getKing(teamColor).getPos();
        if (vulnerableTo.get(otherTeam(teamColor).ordinal()).contains(location)) {
            return true;
        }
        else {return false;}
    }

    public boolean isInCheckmate(ChessGame.TeamColor teamColor) {
        // Call this immediately after switching turns
        if (!isInCheck(teamColor)) {
            return false;
        }
        return isInCheckmateOrStalemate(teamColor);
    }


    public void handleCheckmate(TeamColor losingTeam) {
        //System.out.print(losingTeam.toString() + " is in checkmate. ");
        checkmated = losingTeam;
        declareVictory(otherTeam(losingTeam));
    }

    public void handleCheck(TeamColor threatenedTeam) {
        //System.out.println(threatenedTeam.toString() + " is in check.\n");
        inCheck = threatenedTeam;
    }

    private void declareVictory(TeamColor winner) {
        gameOver = true;
        this.winner = winner;
       /* if (winner != null) {
            System.out.println(winner.toString() + " wins!");
        }
        else {
            System.out.println("The game is a stalemate.");
        }*/
    }

    public void handleStalemate(TeamColor trappedTeam) {
        // System.out.print(trappedTeam.toString() + " has no legal moves. ");
        declareVictory(null);
    }

    public boolean isInStalemate(ChessGame.TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return isInCheckmateOrStalemate(teamColor);
    }

    public boolean isInCheckmateOrStalemate(ChessGame.TeamColor teamColor) {
        boolean returnVal = true;
        HashSet<ChessMove> possibleMoves = allValidMoves(teamColor);
        for (ChessMove thisMove : possibleMoves) {
            MoveObj thisMoveObj = (MoveObj)thisMove;
            try {testMoveForCheck(thisMoveObj);}
            catch (KingInCheckException kingExcept) {
                thisMoveObj.setAsDangerous();
            }
            if (!thisMoveObj.getKingInCheck()) {
                returnVal = false;
            }
        }
        return returnVal;
    }

    public void setBoard(ChessBoard board) {
        this.board = new BoardObj(board);
        this.board.setGame(this);
        //updateLegalMoves(this.board);  // May need to add this!!!!!
    }

    public ChessBoard getBoard() {
        return board;
    }

    public boolean addVulnerableSpot (TeamColor toWhichColor, Position pos) {
        return vulnerableTo.get(toWhichColor.ordinal()).add(pos);
    }

    public boolean isVulnerableSpot(Position thisPos, ChessGame.TeamColor toWhichTeam) {
        return vulnerableTo.get(toWhichTeam.ordinal()).contains(thisPos);
    }

    public void switchTurn() {
        updateLegalMoves(board);
    /*    vulnerableTo.get(0).clear();
        vulnerableTo.get(1).clear();
        Position pos;
        PieceObj piece;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                pos = new Position(i, j);
                piece = (PieceObj)board.getPiece(pos);
                if (piece != null) {piece.updatePieceMoves();}
            }
        } */

        currTurn = otherTeam(currTurn);
        if (isInCheckmate(currTurn)) {
            handleCheckmate(currTurn);
        }
        if (isInCheck(currTurn)) {
            handleCheck(currTurn);
        }
        if (isInStalemate(currTurn)) {
            handleStalemate(currTurn);
        }
    }

    public static TeamColor otherTeam(TeamColor thisCol) {
        if (thisCol == TeamColor.BLACK) {return TeamColor.WHITE;}
        else {return TeamColor.BLACK;}
    }

    public void testMoveForCheck(MoveObj thisMove)
            throws KingInCheckException {
        Position startPos = thisMove.getStartPosition();
        PieceObj piece = (PieceObj)board.getPiece(startPos);
        Position endPos = thisMove.getEndPosition();
        TeamColor team = board.getPiece(startPos).getTeamColor();

        boolean isEnPassant = false;
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && // If we're moving a pawn,
                endPos.getColumn() != startPos.getColumn() && //and it's moving diagonally
                //(i.e. it's attacking),
                board.getPiece(endPos) == null)               //and its destination is vacant,
        {
            isEnPassant = true;                               //then this is an en passant
        }                                                     //move!

        hypotheticalBoard = new BoardObj(board);
        forceMove(hypotheticalBoard, thisMove, isEnPassant, team);

        var savedVulnerabilityList = new ArrayList<HashSet<Position>>(vulnerableTo);

        updateLegalMoves(hypotheticalBoard);
        if (isInCheck(team, hypotheticalBoard)) {
            throw new KingInCheckException();
        }

        vulnerableTo = savedVulnerabilityList;
    }

    private void updateLegalMoves(BoardObj theBoard) {
        vulnerableTo.clear();
        vulnerableTo.add(new HashSet<Position>());
        vulnerableTo.add(new HashSet<Position>());
        Position pos;
        PieceObj piece;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                pos = new Position(i, j);
                piece = (PieceObj) theBoard.getPiece(pos);
                if (piece != null) {
                    piece.updatePieceMoves();
                }
            }
        }
        theBoard.getKing(TeamColor.WHITE).getCastleMoves();
        theBoard.getKing(TeamColor.BLACK).getCastleMoves();
    }

    private void forceMove(BoardObj theBoard, MoveObj thisMove, boolean isEnPassant,
                           TeamColor team) {
        Position startPos = thisMove.getStartPosition();
        Position endPos = thisMove.getEndPosition();
        PieceObj piece = (PieceObj)theBoard.getPiece(startPos);

        int enPassantVulnerabilityRow;
        if (team == TeamColor.BLACK) {enPassantVulnerabilityRow = 5;}
        else {enPassantVulnerabilityRow = 4;}

        Position pawnSquare;
        Pawn vulnerablePawn;
        ChessPiece pieceInRow;
        for (int col = 1; col <= 8; col++) {
            pawnSquare = new Position(enPassantVulnerabilityRow, col);
            pieceInRow = theBoard.getPiece(pawnSquare);
            if (pieceInRow instanceof Pawn) { // Different in subsequent version; change?
                vulnerablePawn = (Pawn)pieceInRow;
                vulnerablePawn.setVulnerability(false);
            }
        }

        piece.relocate(endPos);
        theBoard.setPiece(startPos, null);
        theBoard.setPiece(endPos, piece);

        if (piece instanceof Pawn) { // Different in subsequent version; change?
            Pawn thisPawn = (Pawn)piece;
            if (!thisPawn.getMovedStatus()) {              // If this is its first move
                int distance = endPos.getRow() - startPos.getRow();
                if (distance == 2 || distance == -2) {   // and it's moving two spaces,
                    thisPawn.setVulnerability(true);   // it's vulnerable to en passant.
                }
            }
        }

        if (piece instanceof RKP) {
            RKP rkpPiece = (RKP)piece;
            rkpPiece.hasNowMoved();
        }

        if (thisMove.isCastleMove()) {
            Position rookPos = findRook(theBoard, startPos, endPos);
            moveRookForCastle(theBoard, rookPos);
        }

        if (thisMove.getPromotionPiece() != null) {
            promotePawn(theBoard, endPos, thisMove.getPromotionPiece());
        }

        if (isEnPassant) {
            if (team == TeamColor.BLACK) {
                theBoard.setPiece(endPos.addToRow(1), null);
            }
            else {
                theBoard.setPiece(endPos.addToRow(-1), null);
            }
        }
    }

    private void promotePawn(BoardObj theBoard, Position pawnSpot, ChessPiece.PieceType type) {
        TeamColor team = theBoard.getPiece(pawnSpot).getTeamColor();
        switch (type) {
            case KNIGHT:
                theBoard.setPiece(pawnSpot, new Knight(theBoard, team, pawnSpot));
                break;
            case BISHOP:
                theBoard.setPiece(pawnSpot, new Bishop(theBoard, team, pawnSpot));
                break;
            case ROOK:
                theBoard.setPiece(pawnSpot, new Rook(theBoard, team, pawnSpot));
                break;
            case QUEEN:
                theBoard.setPiece(pawnSpot, new Queen(theBoard, team, pawnSpot));
                break;
            default:
                System.out.println("Invalid pawn promotion: " + type.toString());
                System.exit(1);
        }
    }

    private Position findRook(BoardObj theBoard, Position kingStartPos,
                              Position kingEndPos) {
        TeamColor team = theBoard.getPiece(kingEndPos).getTeamColor();
        if (kingStartPos.getColumn() < kingEndPos.getColumn()) {
            if (team == TeamColor.WHITE) {
                return new Position(1, 8);
            }
            else {return new Position(8, 8);}
        }
        else {
            if (team == TeamColor.WHITE) {
                return new Position(1, 1);
            }
            else {return new Position(8, 1);}
        }
    }

    private void moveRookForCastle(BoardObj theBoard, Position rookPos) {
        Position endPos;
        if (rookPos.getColumn() == 1) {endPos = rookPos.addToColumn(3);}
        else {endPos = rookPos.addToColumn(-2);}
        Rook theRook = (Rook)theBoard.getPiece(rookPos);
        theRook.relocate(endPos);
        theBoard.setPiece(rookPos, null);
        theBoard.setPiece(endPos, theRook);
    }

    private HashSet<ChessMove> allValidMoves(TeamColor team) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();
        Position pos;
        PieceObj piece;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                pos = new Position(i, j);
                piece = (PieceObj)board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == team) {
                    moves.addAll(piece.pieceMoves());
                }
            }
        }
        return moves;
    }

    public TeamColor getWinner() {
        return winner;
    }

    public TeamColor getCheckedTeam() {
        return inCheck;
    }

    public TeamColor getCheckmatedTeam() {
        return checkmated;
    }

    public boolean getGameOver() {
        return gameOver;
    }

    private boolean gameOver = false;
    private TeamColor winner = null;
    private TeamColor inCheck = null;
    private TeamColor checkmated = null;
    private BoardObj board;
    private BoardObj hypotheticalBoard;
    private TeamColor currTurn;
    private ArrayList<HashSet<Position>> vulnerableTo;
}