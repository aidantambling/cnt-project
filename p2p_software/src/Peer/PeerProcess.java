package Peer;

import java.util.ArrayList;

public class PeerProcess {

    public static void main(String[] args) {
        /*this is the main peerProcess, the center of the program. 
        from here the we call all the other files to get the program running
        the general flow will be the following: */

        /* 1. parse the config files, both common and peer
         * 2. initialize the peer and all their information
         * 3. initialize a logger for the peer
         * 4. peer will have 2 conditions:
         *      if has file, it will only be a server
         *      if not, it will be client and server
         */
         //for now this is hardcoded dummy implementation of peer process

        // Simulate parsing a configuration 
        String fileName = "thefile";
        int fileSize = 167705; 
        int pieceSize = 16384; 
        int numberOfPieces = (int) Math.ceil(fileSize / pieceSize);

        //simulate parsing peer configuration
        int peerId = 1;
        int port = 7000; 
        boolean hasCompleteFile = false;

        // Initialize data structures 

        ArrayList<peer> peers = new ArrayList<peer>();
        byte[] bitField = {0, 0, 0, 0, 0, 0, 0};


        // Start logger with a dummy message 
        System.out.println("Starting logger for peer " + peerId);

        if (hasCompleteFile) {
        
            // peer with complete file will just serve
            System.out.println("Peer " + peerId + " with complete file listening on port " + port + " for incoming connections.");

        } else { //this peer does not have file so it will be server and client

            // Simulate server
            System.out.println("Peer " + peerId + " listening on port " + port + " for incoming connections.");

            // Simulate client
            System.out.println("Peer " + peerId + " attempting to connect to other peers...");

        }

    }
}
