package Peer.Messages;

public class Unchoke implements Message {
    private static final byte MESSAGE_TYPE = 1;

    @Override
    public byte[] toBytes() {
        return new byte[]{1}; // Unchoke message has no payload, just the message type
    }
}
