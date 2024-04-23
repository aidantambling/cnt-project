package Peer;

import Peer.Messages.*;

public class ClientMessageParser {

    public static Message parseClientMessage(byte[] messageBytes) throws IllegalArgumentException {
        if (messageBytes == null || messageBytes.length < 5) {
            throw new IllegalArgumentException(
                "Invalid message format: Message bytes array is null or has less than 5 bytes"
            );
        }

        int messageLength = bytesToInt(messageBytes, 0);

        if (messageLength != messageBytes.length - 4) {
            throw new IllegalArgumentException(
                "Message length mismatch: Expected length = " + messageLength +
                ", Actual length = " + (messageBytes.length - 4)
            );
        }

        byte messageType = messageBytes[4];

        switch (messageType) {
            case 0: // Choke
                return new Choke();
            case 1: // Unchoke
                return new Unchoke();
            case 2: // Interested
                return new Interested();
            case 3: // Not Interested
                return new NotInterested();
            case 4: // Have
                int pieceIndex = bytesToInt(messageBytes, 5);
                return new Have(pieceIndex);
            case 5: // Bitfield
                byte[] bitfield = new byte[messageLength - 1];
                System.arraycopy(
                    messageBytes, 5, bitfield, 0, bitfield.length
                );
                return new Bitfield(bitfield);
            default:
                throw new IllegalArgumentException(
                    "Unknown message type for client: " + messageType
                );
        }
    }

    private static int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
                ((bytes[offset + 1] & 0xFF) << 16) |
                ((bytes[offset + 2] & 0xFF) << 8) |
                (bytes[offset + 3] & 0xFF);
    }
}
