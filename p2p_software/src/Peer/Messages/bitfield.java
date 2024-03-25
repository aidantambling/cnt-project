package Peer.Messages;

public class Bitfield implements Message {
    private static final byte MESSAGE_TYPE = 5;
    private byte[] bitfield;

    public Bitfield(byte[] bitfield) {
        this.bitfield = bitfield;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[bitfield.length + 1];
        bytes[0] = MESSAGE_TYPE;
        System.arraycopy(bitfield, 0, bytes, 1, bitfield.length);
        return bytes;
    }
}