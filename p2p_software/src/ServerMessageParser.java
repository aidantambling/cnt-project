public class ServerMessageParser {

    public static Message parseServerMessage(byte[] messageBytes) throws IllegalArgumentException {
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
            case 6: // Request
                int requestPieceIndex = bytesToInt(messageBytes, 5);
                return new Request(requestPieceIndex);
            case 7: // Piece
                int pieceIndexPiece = bytesToInt(messageBytes, 5);
                byte[] pieceData = new byte[messageLength - 5];
                System.arraycopy(
                    messageBytes, 9, pieceData, 0, pieceData.length
                );
                return new Piece(pieceIndexPiece, pieceData);
            default:
                throw new IllegalArgumentException(
                    "Unknown message type for server: " + messageType
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
