package Peer.Messages;

import java.nio.ByteBuffer;

public class interested { //no payload, only length and type
    public byte[] interestedMessage = new byte[5]; // 4 byte message plus 1 byte type
    int messageLength = 4; //4 bytes 
    private byte type = 2; // interested message is type value 2

    public interested() {
        // the 4 byte message length field is set to 1 since interested is 1 bit long
        interestedMessage = ByteBuffer.allocate(messageLength).putInt(1).array(); 
        interestedMessage[4] = type;
    }
}
