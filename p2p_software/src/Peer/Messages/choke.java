package Peer.Messages;

import java.nio.ByteBuffer;

public class choke { //no payload, only length and type
    public byte[] chokeMessage = new byte[5]; // 4 byte message plus 1 byte type
    int messageLength = 4; //4 bytes 
    private byte type = 0; // choke message is type value 0

    public choke() {
        // the 4 byte message length field is set to 1 since choke is 1 bit long
        chokeMessage = ByteBuffer.allocate(messageLength).putInt(1).array(); 
        chokeMessage[4] = type;
    }
}
