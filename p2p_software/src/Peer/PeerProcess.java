package Peer;

import FileManager.configParser;
import FileManager.peerInfoParser;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class PeerProcess {

    static peer peer;
    private static FileManager fileManager;
//    static byte[] fileData;
    static String directoryPath;

    public static void main(String[] args) throws IOException {
        /* General format of the approach:
         * 1. parse the config files, both common and peer
         * 2. initialize the peer and all their information
         * 3. initialize a logger for the peer
         * 4. peer will have 2 conditions:
         *      if has file, it will only be a server
         *      if not, it will be client and server
         */

        // 1. parse the config files

        // Parse the .cfg files to get important info abt the peers
        configParser configParser = new configParser();
        configParser.readFile();
        peerInfoParser peerParser = new peerInfoParser();
        peerParser.readFile();

        ArrayList<peer> peers = new ArrayList<peer>();
        byte[] bitField = {0, 0, 0, 0, 0, 0, 0};

        // launch the peer we are targeting
        int peerID = Integer.parseInt(args[0]);

        // grab the relevant information from PeerInfo.cfg
        String hostname;
        int port = 0;
        boolean hasFile = false;
        for (int i = 0; i < peerInfoParser.peerInfoVector.size(); i++){
            peerInfoParser.peerInfo p = peerInfoParser.peerInfoVector.get(i);
            if (peerID == p.getPeerId()){
                System.out.println("Match found in PeerInfo.cfg - the peerID argument is valid");
                hostname = p.getHostName();
                port = p.getPort();
                hasFile = p.hasCompleteFile();
                System.out.println(port + " " + hasFile);
                break;
            }
            if (i == peerInfoParser.peerInfoVector.size() - 1){
                System.out.println("A match could not be found in PeerInfo.cfg - the peerID argument is not valid.");
                return;
            }
        }

        // Start logger with a dummy message 
//        System.out.println("Starting logger for peer " + peerID);

        // if peer has the file, it will just be a server
        if (hasFile) {
            System.out.println("Peer " + peerID + " with complete file listening on port " + port + " for incoming connections.");
            // we need to load the file's data into this program so we can distribute it to the other peers
            // TODO: do something to bitfield
            directoryPath = System.getProperty("user.dir") + File.separator + "peer_" + peerID;
            try {
                File directory = new File(directoryPath);
                System.out.println(directory.getAbsolutePath());
                File[] files = directory.listFiles();
                File target;
                if (files != null && files.length > 0){
                    target = files[0];
                    fileManager = new FileManager(target.getAbsolutePath(), configParser.getFileName(), (int) configParser.getPieceSize());
                }
                else {
                    System.out.println("No files found / error reading the files.");
                    return;
//                    target = files[0]; // grab the file object the peer will be sending
//                    FileInputStream fis = new FileInputStream(target);
//                    fileData = new byte[(int)target.length()];
//                    fis.read(fileData);
//                    System.out.println(fileData);
//                    fis.close();
//                    System.out.println("Server has been deployed for peer " + peerID + " which is hosting file " + target.getName());
                }
            } catch (Exception e) {
                System.err.println("Error loading the file: " + e.getMessage());
            }
        }
        else { //this peer does not have file - it will be server and client
            directoryPath = System.getProperty("user.dir") + File.separator + "peer_" + peerID;
            fileManager = new FileManager((int) configParser.getFileSize(), directoryPath, configParser.getFileName(), (int) configParser.getPieceSize()); // Alternative constructor for non-file-owners
        }

        // regardless of if the peer is a client / server, it should use threads to connect tto the other peers.
        peer = new peer(peerID, port, peerInfoParser.peerInfoVector, fileManager, configParser.getUnchokingInterval(), configParser.getOptimisticUnchokingInterval(), configParser.getNumberOfPrefferedNeighbors());

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
