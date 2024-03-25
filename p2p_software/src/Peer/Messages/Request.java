package Peer.Messages;

public class Request {
    private int pieceIndex; // Index of the piece requested by the peer

    // Constructor
    public Request(int pieceIndex) {
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

    // Additional methods or logic related to the Request message
}
