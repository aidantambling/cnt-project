package Peer.Messages;

public class Piece implements Message {
    private static final byte MESSAGE_TYPE = 7;
    private int pieceIndex;
    private byte[] pieceData;

    public Piece(int pieceIndex, byte[] pieceData) {
        this.pieceIndex = pieceIndex;
        this.pieceData = pieceData;
    }

    @Override
    public byte[] toBytes() {
        int length = 5 + pieceData.length;
        byte[] bytes = new byte[length];
        bytes[0] = MESSAGE_TYPE;
        bytes[1] = (byte) (pieceIndex >> 24);
        bytes[2] = (byte) (pieceIndex >> 16);
        bytes[3] = (byte) (pieceIndex >> 8);
        bytes[4] = (byte) pieceIndex;
        System.arraycopy(pieceData, 0, bytes, 5, pieceData.length);
        return bytes;
    }
}
