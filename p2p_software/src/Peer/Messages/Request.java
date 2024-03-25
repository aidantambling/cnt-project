package Peer.Messages;

public class Request implements Message {
    private static final byte MESSAGE_TYPE = 6;
    private int pieceIndex;

    public Request(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[5];
        bytes[0] = MESSAGE_TYPE;
        bytes[1] = (byte) (pieceIndex >> 24);
        bytes[2] = (byte) (pieceIndex >> 16);
        bytes[3] = (byte) (pieceIndex >> 8);
        bytes[4] = (byte) pieceIndex;
        return bytes;
    }
}
