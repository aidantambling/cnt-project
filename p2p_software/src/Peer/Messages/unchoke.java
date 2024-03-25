package Peer.Messages;

import java.nio.ByteBuffer;

public class unchoke { //no payload, only length and type
    public byte[] unchokeMessage = new byte[5]; // 4 byte message plus 1 byte type
    int messageLength = 4; //4 bytes 
    private byte type = 1; // unchoke message is type value 1

    public unchoke() {
        // the 4 byte message length field is set to 1 since unchoke is 1 bit long
        unchokeMessage = ByteBuffer.allocate(messageLength).putInt(1).array(); 
        unchokeMessage[4] = type;
    }
}
