package Peer;

import FileManager.configParser;
import FileManager.peerInfoParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class PeerProcess {

    static peer peer;
    static byte[] fileData;

    public static void main(String[] args) throws IOException {
        /* General format of the approach:
         * 1. parse the config files, both common and peer
         * 2. initialize the peer and all their information
         * 3. initialize a logger for the peer
         * 4. peer will have 2 conditions:
         *      if has file, it will only be a server
         *      if not, it will be client and server
         */
         //for now this is hardcoded dummy implementation of peer process

        // 1. parse the config files

        // Simulate parsing a configuration 
        String fileName = "thefile";
        int fileSize = 167705; 
        int pieceSize = 16384; 
        int numberOfPieces = (int) Math.ceil(fileSize / pieceSize);

        //simulate parsing peer configuration
        int peerId = 1;
        int port = 7000; 
        boolean hasCompleteFile = true;

        // Parse the .cfg files to get important info abt the peers
        // .readFile() lines commented out - .cfg files not present yet.
        configParser configParser = new configParser();
//        configParser.readFile();
        peerInfoParser peerParser = new peerInfoParser();
//        peerParser.readFile();

        // peerParser stores the initial state of each peer (.hasFile = true/false?)
        // assign the file to a peer_100x folder according to that peer's .hasFile value?

        // Initialize data structures 

        ArrayList<peer> peers = new ArrayList<peer>();
        byte[] bitField = {0, 0, 0, 0, 0, 0, 0};

        // launch the peer we are targeting
        int inputID = Integer.parseInt(args[0]);
//        int inputID = 1001;
        // TODO: check that the input ID is valid
        // TODO: verify this is a valid # (no parseInt values)
        // TODO: OK, it is a # - but is it a peer? check the peerMap from configInfo.

        // Start logger with a dummy message 
        System.out.println("Starting logger for peer " + peerId);


        // if peer has the file, it will just be a server
        if (hasCompleteFile) {
            System.out.println("Peer " + inputID + " with complete file listening on port " + port + " for incoming connections.");
            // we need to load the file's data into this program so we can distribute it to the other peers
            // TODO: do something to bitfield
            // TODO: File was being weird when tried to open without getProperty - is this a problem?
            String directoryPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "peer_" + inputID;
            try {
                File directory = new File(directoryPath);
                System.out.println(directory.getName());
                File[] files = directory.listFiles();
                File target;
                if (files == null){
                    System.out.println("No files found in the given peer's directory");
                }
                else {
                    target = files[0]; // grab the file object the peer will be sending
                    FileInputStream fis = new FileInputStream(target);
                    fileData = new byte[(int)target.length()];
                    fis.read(fileData);
                    System.out.println(fileData);
                    fis.close();
                    System.out.println("Server has been deployed for peer " + inputID + " which is hosting file " + target.getName());
                }
            } catch (Exception e) {
                System.err.println("Error loading the file: " + e.getMessage());
            }
        }
        else { //this peer does not have file - it will be server and client
            // Simulate server
            System.out.println("Peer " + peerId + " listening on port " + port + " for incoming connections.");

            // Simulate client
            System.out.println("Peer " + peerId + " attempting to connect to other peers...");
        }

        // regardless of if the peer is a client / server, it should use threads to connect tto the other peers.

        //TODO: begin server functioning

        // use a thread to deploy the server so it can listen for connections

        // we can iterate over the peer map from config files and (ignoring THIS peer) start a connection with them (via SERVERSOCKET)

        // for each peer, we can use a loop listening for messages. we will decode the messages using our Messages classes
        // and of course, respond appropriately to the messages (piece => update bitfield, bitfield => interested)

        // we will need a timer to implement the choking and unchoking functions, but this can be implemented later imo. it's
        // less relevant to the implementation and more of an "extra" feature

        //TODO: begin client functioning

        // somewhat similar to the above described server functionality

        // use thread to deploy the client so it can run concurrently with the server

        // iterate thru the peer map and (ignoring THIS peer)  start a connection with them (via SOCKET)
    }
}
