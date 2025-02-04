package chess;

public class Position implements ChessPosition {
    public static boolean isValidPos(int row, int col) {
        return (row <= 8 && row >= 1 && col <= 8 && col >= 1);
    }
    public static boolean isValidPos(String position) {
        if (position == null || position.length() < 2) {
            return false;
        }
        String pos = position.toUpperCase();
        int r = pos.charAt(1) - '0';
        int col = pos.charAt(0) - '@';
        return isValidPos(r, col);
    }

    public static boolean isValidAddition(Position pos, int rowAddend, int colAddend) {
        return (pos.row + rowAddend <= 8 && pos.row + rowAddend >= 1 &&
                pos.column + colAddend <= 8 && pos.column + colAddend >= 1);
    }

    public Position(int row, int column) {
        if (!isValidPos(row, column)) {
            System.out.println("Error: Position not on board");
            System.exit(1);
            //throw new InvalidMoveException("Position not on board");
        }

        this.row = row;
        this.column = column;
    }

    public Position(String position) {
        String pos = position.toUpperCase();
        row = pos.charAt(1) - '0';
        column = pos.charAt(0) - '@';
        if (!isValidPos(row, column)) {
            System.out.println("Error: Position not on board");
            System.exit(1);
            //throw new InvalidMoveException("Error: Position not on board");
        }
    }

    public Position(Position other) {
        this.row = other.row;
        this.column = other.column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Position addToColumn(int addendum) {
        return new Position(row, column + addendum);
    }

    public Position addToRow(int addendum) {
        return new Position(row + addendum, column);
    }

    public Position plus(int rowAddend, int ColAddend) {
        return new Position(row + rowAddend, column + ColAddend);
    }

    public boolean equals(Object other) {
        if (!(other instanceof Position)) {return false;}
        Position newPos = (Position)other;
        return (row == newPos.row && column == newPos.column);
    }

    public int hashCode() {
        return (row + 3) * (column + 7) + column + row + 31;
    }

    public String toString() {
        StringBuilder returnVal = new StringBuilder("--");
        returnVal.setCharAt(0, (char)('@' + this.getColumn()));
        returnVal.setCharAt(1, (char)('0' + this.getRow()));

        return returnVal.toString();
    }

    private int row;
    private int column;
}
