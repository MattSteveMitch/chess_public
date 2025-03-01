package chess;

import java.util.ArrayList;
import java.util.HashSet;
import chess.ChessGame.TeamColor;

public class BoardObj implements ChessBoard {
    public BoardObj(GameObj game, boolean setUp) {
        this.game = game;
        grid = new PieceObj[8][8];
        kings = new ArrayList<King>();
        kings.add(null);
        kings.add(null);
        if (setUp) {
            resetBoard();
        }
    }

    public BoardObj(ChessBoard other) {
        BoardObj otherBoardObj = (BoardObj) other;
        game = otherBoardObj.game;
        grid = new PieceObj[8][8];
        kings = new ArrayList<King>();
        kings.add(null);
        kings.add(null);
        copyGrid(otherBoardObj.grid, kings);
    }

    private void copyGrid(ChessPiece[][] pattern, ArrayList<King> ownKings) {
        King kingCopy;
        ChessGame.TeamColor team;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (pattern[i][j] == null) {continue;}
                switch (pattern[i][j].getPieceType()) {
                    case BISHOP:
                        grid[i][j] = new Bishop((Bishop)pattern[i][j], this);
                        break;
                    case KING:
                        kingCopy = new King((King)pattern[i][j], this);
                        grid[i][j] = kingCopy;
                        team = kingCopy.getTeamColor();
                        ownKings.set(team.ordinal(), kingCopy);
                        break;
                    case KNIGHT:
                        grid[i][j] = new Knight((Knight)pattern[i][j], this);
                        break;
                    case PAWN:
                        grid[i][j] = new Pawn((Pawn)pattern[i][j], this);
                        break;
                    case QUEEN:
                        grid[i][j] = new Queen((Queen)pattern[i][j], this);
                        break;
                    case ROOK:
                        grid[i][j] = new Rook((Rook)pattern[i][j], this);
                        break;
                }
            }
        }
    }

    public void setPiece(ChessPosition position, ChessPiece piece) {
        grid[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        setPiece(position, piece);
    }

    public void setKing(King king) {
        kings.set(king.getTeamColor().ordinal(), king);
    }

    public void setGame(GameObj givenGame) { // This function is only necessary for passing the junit tests
        game = givenGame;
    }

    public ChessPiece getPiece(ChessPosition position) {
        return grid[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // Rooks
        kings.clear();
        kings.add(null);
        kings.add(null);

        setRooks();
        setKnights();
        setBishops();
        setKingsAndQueens(kings);
        setPawns();

        Position pos;
        for (int i = 3; i <= 6; i++) {
            for (int j = 1; j <= 8; j++) {
                pos = new Position(i, j);
                addPiece(pos, null);
            }
        }

    }

    public boolean addVulnerableSpot(ChessGame.TeamColor toWhichColor, Position pos) {
        return game.addVulnerableSpot(toWhichColor, pos);
    }

    public boolean equals(Object other) {
        boolean clause1;
        boolean clause2;
        boolean clause3;

        if (!(other instanceof BoardObj)) {
            return false;
        }
        BoardObj otherB = (BoardObj)other;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                clause1 = (grid[i][j] == null);
                clause2 = (otherB.grid[i][j] == null);
                if (!clause1) {
                    clause3 = ((PieceObj)grid[i][j]).equals( (PieceObj)otherB.grid[i][j]);
                }
                else {
                    clause3 = true;
                }

                if (clause1 != clause2 || !clause3 ) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isVulnerableSpot(Position thisPos, ChessGame.TeamColor toWhichTeam) {
        return game.isVulnerableSpot(thisPos, toWhichTeam);
    }

    public GameObj getGame() {return game;}

    public King getKing(ChessGame.TeamColor team) {
        return kings.get(team.ordinal());
    }
    public void printBoard(TeamColor perspective) {
        printBoard(perspective, null, new HashSet<Position>(), new HashSet<Position>());
    }

    public void printBoard(TeamColor perspective, Position focus, HashSet<Position> legalMoves,
                           HashSet<Position> illegalMoves) {
        if (perspective == null) {
            perspective = TeamColor.WHITE;
        }

        Position pos;
        ChessPiece piece;

        int horizDirection;
        int firstRow;
        int lastRow;
        int vertDirection;
        int firstCol;
        int lastCol;

        if (perspective == TeamColor.WHITE) {
            System.out.print("\n\033[37;40m    ");
            for (char c = 'A'; c < 'I'; c++) {
                System.out.print(c + "  ");
            }
            System.out.print("  ");
            horizDirection = 1;
            vertDirection = -1;
            firstRow = 8;
            lastRow = 1;
            firstCol = 1;
            lastCol = 8;
        }
        else {
            System.out.print("\n\033[107;30m    ");
            for (char c = 'H'; c > '@'; c--) {
                System.out.print(c + "  ");
            }
            System.out.print("  ");
            horizDirection = -1;
            vertDirection = 1;
            firstRow = 1;
            lastRow = 8;
            firstCol = 8;
            lastCol = 1;
        }

        for (int i = firstRow; i != lastRow + vertDirection; i += vertDirection) {
            System.out.print("\033[39;49m\n");
            System.out.print("\033[37;40m " + String.valueOf(i) + ' ');
            for (int j = firstCol; j != lastCol + horizDirection; j += horizDirection) {
                pos = new Position(i, j);
                piece = getPiece(pos);

                if (pos.equals(focus)) {
                    System.out.print("\033[42m");
                }
                else if (legalMoves.contains(pos)) {
                    System.out.print("\033[46m");
                }
                else if (illegalMoves.contains(pos)) {
                    System.out.print("\033[41m");
                }
                else {
                    if (i % 2 != j % 2) {
                        System.out.print("\033[47m");
                    } else {
                        System.out.print("\033[100m");
                    }
                }

                if (piece != null) {
                    switch (piece.getPieceType()) {
                        case BISHOP: {
                            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                System.out.print("\033[39;1m B \033[39;0m");
                            }
                            else {System.out.print("\033[30m b ");}
                            break;
                        }
                        case KING: {
                            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                System.out.print("\033[39;1m K \033[39;0m");
                            }
                            else {System.out.print("\033[30m k ");}
                            break;
                        }
                        case KNIGHT: {
                            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                System.out.print("\033[39;1m N \033[39;0m");
                            }
                            else {System.out.print("\033[30m n ");}
                            break;
                        }
                        case PAWN: {
                            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                System.out.print("\033[39;1m P \033[39;0m");
                            }
                            else {System.out.print("\033[30m p ");}
                            break;
                        }
                        case QUEEN: {
                            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                System.out.print("\033[39;1m Q \033[39;0m");
                            }
                            else {System.out.print("\033[30m q ");}
                            break;
                        }
                        case ROOK: {
                            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                System.out.print("\033[39;1m R \033[39;0m");
                            }
                            else {System.out.print("\033[30m r ");}
                            break;
                        }
                    }
                }
                else {
                    System.out.print("   ");
                }
            }
            System.out.print("\033[107;30m " + String.valueOf(i) + ' ');
        }
        if (perspective == TeamColor.WHITE) {
            System.out.print("\033[39;49m\n\033[107;30m    ");
            for (char c = 'A'; c < 'I'; c++) {
                System.out.print(c + "  ");
            }
            System.out.println("  \033[39;49m\n");
            //System.out.print("\033[39;49m");
        }
        else {
            System.out.print("\033[39;49m\n\033[37;40m    ");
            for (char c = 'H'; c > '@'; c--) {
                System.out.print(c + "  ");
            }
            System.out.println("  \033[39;49m\n");
        }
    }

    private void setRooks() {
        Position pos;
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        int[][] coords = {{1, 8}, {1, 8}};

        for (int i : coords[1]) {
            for (int j : coords[0]) {
                pos = new Position(i, j);
                addPiece(pos, new Rook(this, color, pos));
            }
            color = ChessGame.TeamColor.BLACK;
        }
    }

    private void setKnights() {
        Position pos;
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        int[][] coords = {{2, 7}, {1, 8}};

        for (int i : coords[1]) {
            for (int j : coords[0]) {
                pos = new Position(i, j);
                addPiece(pos, new Knight(this, color, pos));
            }
            color = ChessGame.TeamColor.BLACK;
        }
    }

    private void setBishops() {
        Position pos;
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        int[][] coords = {{3, 6}, {1, 8}};

        for (int i : coords[1]) {
            for (int j : coords[0]) {
                pos = new Position(i, j);
                addPiece(pos, new Bishop(this, color, pos));
            }
            color = ChessGame.TeamColor.BLACK;
        }
    }

    private void setKingsAndQueens(ArrayList<King> kingTracker) {
        Position pos;
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        int[][] coords = {{4, 5}, {1, 8}};
        King king;

        for (int i : coords[1]) {
            for (int j : coords[0]) {
                pos = new Position(i, j);
                if (j == 4) {addPiece(pos, new Queen(this, color, pos));}
                else {
                    king = new King(this, color, pos);
                    addPiece(pos, king);
                    kingTracker.set(color.ordinal(), king);
                }
            }
            color = ChessGame.TeamColor.BLACK;
        }
    }

    private void setPawns() {
        Position pos;
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        int[] rows = {2, 7};
        for (int i : rows) {
            for (int j = 1; j <= 8; j++) {
                pos = new Position(i, j);
                addPiece(pos, new Pawn(this, color, pos));
            }
            color = ChessGame.TeamColor.BLACK;
        }
    }

    private ChessPiece[][] grid;
    //private ArrayList<ArrayList<PieceObj>> grid;
    private GameObj game;
    private ArrayList<King> kings;
}

