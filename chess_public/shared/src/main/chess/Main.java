package chess;


public class Main {
    public static void main(String[] args) throws InvalidMoveException {

        if (args.length > 0 && args[0].equals("board_pattern")) {boardPatternOn = true;}
        GameObj game = new GameObj();
        BoardObj board = (BoardObj)(game.getBoard());
        //board.resetBoard();
      /*  var set = new HashSet<Rook>();
        var item = new Rook(board, ChessGame.TeamColor.BLACK, new Position(5,8));
        set.add(item);
        for (Rook i : set) {
            i.hasNowMoved();
        }*/
        // game.switchTurn();

        game.makeMove(new MoveObj("h2", "h3"));
        game.makeMove(new MoveObj("c7", "c6"));
        game.makeMove(new MoveObj("g1", "f3"));
        game.makeMove(new MoveObj("d8", "c7"));
        game.makeMove(new MoveObj("a2", "a4"));
        game.makeMove(new MoveObj("c7", "h2"));
        game.makeMove(new MoveObj("b2", "b4"));
        game.makeMove(new MoveObj("h2", "g1"));
        game.makeMove(new MoveObj("h1", "h2"));
        game.makeMove(new MoveObj("g1", "f1"));
        game.makeMove(new MoveObj("e1", "f1"));
        game.makeMove(new MoveObj("g7", "g5"));
        game.makeMove(new MoveObj("c2", "c4"));
        game.makeMove(new MoveObj("f7", "f5"));
        game.makeMove(new MoveObj("d2", "d4"));
        game.makeMove(new MoveObj("h7", "h5"));
        game.makeMove(new MoveObj("f1", "g1"));
        game.makeMove(new MoveObj("f5", "f4"));
        game.makeMove(new MoveObj("g1", "h1"));
        game.makeMove(new MoveObj("g5", "g4"));
        game.makeMove(new MoveObj("d4", "d5"));
        game.makeMove(new MoveObj("a7", "a5"));
        game.makeMove(new MoveObj("f3", "g1"));
        game.makeMove(new MoveObj("a5", "b4"));
        game.makeMove(new MoveObj("a1", "a3"));
        game.makeMove(new MoveObj("b4", "a3"));
        game.makeMove(new MoveObj("c1", "b2"));
        game.makeMove(new MoveObj("a3", "b2"));
        game.makeMove(new MoveObj("d1", "c1"));
        game.makeMove(new MoveObj(new Position(2, 2), new Position(1,3), ChessPiece.PieceType.QUEEN));
        game.makeMove(new MoveObj("d5", "c6"));
        game.makeMove(new MoveObj("c1", "b1"));
        game.makeMove(new MoveObj("c4", "c5"));
        game.makeMove(new MoveObj("b1", "b5"));
        game.makeMove(new MoveObj("a4", "a5"));
        game.makeMove(new MoveObj("b5", "c6"));
        game.makeMove(new MoveObj("a5", "a6"));
        game.makeMove(new MoveObj("c6", "a6"));
        game.makeMove(new MoveObj("c5", "c6"));
        game.makeMove(new MoveObj("e7", "e5"));
        game.makeMove(new MoveObj("c6", "c7"));
        game.makeMove(new MoveObj("e5", "e4"));
        game.makeMove(new MoveObj(new Position(7, 3), new Position(8,2), ChessPiece.PieceType.QUEEN));
        game.makeMove(new MoveObj("e4", "e3"));
        game.makeMove(new MoveObj("b8", "b7"));
        game.makeMove(new MoveObj("g4", "g3"));
        game.makeMove(new MoveObj("b7", "a8"));
        game.makeMove(new MoveObj("h5", "h4"));
        game.makeMove(new MoveObj("a8", "c8"));
        game.makeMove(new MoveObj("a6", "c8"));
        game.makeMove(new MoveObj("f2", "f3"));
        game.makeMove(new MoveObj("e8", "d8"));
        game.makeMove(new MoveObj("", ""));
        game.makeMove(new MoveObj("", ""));
        game.makeMove(new MoveObj(new Position(7, 8), new Position(5,8)));
        game.makeMove(new MoveObj(new Position(2, 7), new Position(4,7)));
        game.makeMove(new MoveObj(new Position(5, 8), new Position(4,8)));
        game.makeMove(new MoveObj(new Position(4, 7), new Position(5,7)));
        game.makeMove(new MoveObj(new Position(4, 8), new Position(3,8)));
        game.makeMove(new MoveObj(new Position(2, 3), new Position(3,3)));
        game.makeMove(new MoveObj(new Position(7, 3), new Position(5,3)));
        game.makeMove(new MoveObj(new Position(2, 5), new Position(4,5)));
        game.makeMove(new MoveObj(new Position(5, 3), new Position(4,3)));
        game.makeMove(new MoveObj(new Position(2, 2), new Position(4,2)));
        game.makeMove(new MoveObj(new Position(4, 3), new Position(3,2)));

       /* Position pos = new Position(5, 1);
        //var piece1 = new Rook(board, ChessGame.TeamColor.BLACK, pos);
        var piece1 = game.getKings().get(0);
        board.setPiece(new Position(8, 2), null);
        board.setPiece(new Position(8, 3), null);
        board.setPiece(new Position(7, 1), null);
        board.setPiece(new Position(7, 2), null);
        board.setPiece(new Position(8, 4), null);
        //board.addPiece(pos, piece1);
        pos = new Position(6, 1);
        var piece2 = new Queen(board, ChessGame.TeamColor.WHITE, pos);
        board.addPiece(pos, piece2);
        game.switchTurn();
        board.printBoard();
        piece1.updatePieceMoves();
        var moves = piece1.pieceMoves(board, pos);

        for (ChessMove move : moves) {
            System.out.println(move.getEndPosition().toString());
        }*/
    }
    static boolean boardPatternOn = false;
}

/*
        game.makeMove(new MoveObj(new Position(7, 2), new Position(6,2)));

                game.makeMove(new MoveObj("f1", "d3"));
                //   game.switchTurn();
                game.makeMove(new MoveObj("a7", "a6"));

                // game.switchTurn();
                game.makeMove(new MoveObj("d1", "g4"));

                // game.switchTurn();
                game.makeMove(new MoveObj("f7", "f6"));
                //  game.switchTurn();
                game.makeMove(new MoveObj("d3", "G6"));


                game.makeMove(new MoveObj("H7", "g6"));*/