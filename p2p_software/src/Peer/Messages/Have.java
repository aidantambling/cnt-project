package Peer.Messages;

public class Have {
    private int pieceIndex; // Index of the piece the peer has

    // Constructor
    public Have(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    // Getter for pieceIndex
    public int getPieceIndex() {
        return pieceIndex;
    }

    // Setter for pieceIndex
    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    // Additional methods or logic related to the Have message
}
