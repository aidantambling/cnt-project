package Peer.Messages;

public class NotInterested implements Message {
    private static final byte MESSAGE_TYPE = 3;

    @Override
    public byte[] toBytes() {
        return new byte[]{3}; // Not interested message has no payload, just the message type
    }
}
