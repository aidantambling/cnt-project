package Peer.Messages;

public class Piece {
    private int pieceIndex; // Index of the piece being sent
    private byte[] pieceData; // Data of the piece being sent

    // Constructor
    public Piece(int pieceIndex, byte[] pieceData) {
        this.pieceIndex = pieceIndex;
        this.pieceData = pieceData;
    }

    // Getter for pieceIndex
    public int getPieceIndex() {
        return pieceIndex;
    }

    // Setter for pieceIndex
    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    // Getter for pieceData
    public byte[] getPieceData() {
        return pieceData;
    }

    // Setter for pieceData
    public void setPieceData(byte[] pieceData) {
        this.pieceData = pieceData;
    }

    // Additional methods or logic related to the Piece message
}
