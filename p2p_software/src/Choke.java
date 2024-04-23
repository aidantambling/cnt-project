package Peer.Messages;

public class Choke implements Message {
    private static final byte MESSAGE_TYPE = 0;

    @Override
    public byte[] toBytes() {
        return new byte[]{0}; // Choke message has no payload, just the message type
    }
}