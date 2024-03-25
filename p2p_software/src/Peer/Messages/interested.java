package Peer.Messages;

public class Interested implements Message {
    private static final byte MESSAGE_TYPE = 2;

    @Override
    public byte[] toBytes() {
        return new byte[]{2}; // Interested message has no payload, just the message type
    }
}
