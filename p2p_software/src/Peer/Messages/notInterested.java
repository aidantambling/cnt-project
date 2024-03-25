package Peer.Messages;

import java.nio.ByteBuffer;

public class notInterested { //no payload, only length and type
    public byte[] notInterestedMessage = new byte[5]; // 4 byte message plus 1 byte type
    int messageLength = 4; //4 bytes 
    private byte type = 3; // not interested message is type value 3

    public notInterested() {
        // the 4 byte message length field is set to 1 since not interested is 1 bit long
        notInterestedMessage = ByteBuffer.allocate(messageLength).putInt(1).array(); 
        notInterestedMessage[4] = type;
    }
}
