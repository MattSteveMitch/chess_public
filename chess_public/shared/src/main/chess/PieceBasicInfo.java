package chess;

public class PieceBasicInfo {
    public PieceBasicInfo(PieceObj template) {
        Position pos = template.getPos();
        row = pos.getRow();
        column = pos.getColumn();
        type = template.getPieceType();
        color = template.getTeamColor();

        if (template instanceof RKP) {
            hasMoved = ((RKP)template).getMovedStatus();
        }

        if (template instanceof Pawn) {
            vulnerableToEP = ((Pawn)template).getVulnerability();
        }
    }
    private int row;
    private int column;
    private ChessPiece.PieceType type;
    private ChessGame.TeamColor color;
    private Boolean hasMoved;
    private Boolean vulnerableToEP;
}
