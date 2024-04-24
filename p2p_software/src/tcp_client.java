import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Thread.sleep;

// Contains the "client" capabilities of a peer (requesting / downloading files from other peers)
public class tcp_client {
    int port;
    int targetID;
    public int clientID;

    public int otherPeerID;
    BufferedReader consoleInput; // read input from the command line
    ObjectOutputStream socketOutput; // write to the socket
    ObjectInputStream socketInput; // read from the socket
    private FileManager fileManager;
    private PeerConnectionManager connectionManager;

    ArrayList<Socket> sockets;
    Socket requestSocket;
    Logger logger;
    public tcp_client(int port, int id, int targetID, FileManager fileManager, PeerConnectionManager connectionManager, Logger logger){
        this.port = port;
        this.clientID = id;
        this.fileManager = fileManager;
        this.connectionManager = connectionManager;
        this.logger = logger;
        this.targetID = targetID;
    }

    public void requestServer(String address, int port){
        InetAddress IP;
        try {
            IP = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.out.println("Error: IP address could not be resolved from the hostname");
            throw new RuntimeException(e);
        }
        try {
            this.requestSocket = new Socket(IP, port);
            System.out.println("Client-side socket established by peer " + clientID + " for " + IP.getHostAddress() + ":" + port);
            requestSocket.setSoTimeout(1500);
            logger.makesTCPConnection(targetID);

            this.socketOutput = new ObjectOutputStream(this.requestSocket.getOutputStream());
            this.socketOutput.flush();
            this.socketInput = new ObjectInputStream((this.requestSocket.getInputStream()));
        } catch (Exception e){
            System.out.println("Error in establishing a socket connection!");
            throw new RuntimeException(e);
        }
        maintainConnection(requestSocket);
    }

    public void sendHandshake(Socket socket) {
        try {
            byte[] handshake = "P2PFILESHARINGPROJ".getBytes();
            byte[] zeroBits = new byte[10];
            ByteBuffer buffer = ByteBuffer.allocate(32);
            buffer.put(handshake);
            buffer.put(zeroBits);
            buffer.putInt(clientID);
            socketOutput.writeObject(buffer.array());
            socketOutput.flush();
//            System.out.println("Handshake sent: " + Arrays.toString(buffer.array()));
        } catch (IOException e) {
            System.out.println("Failed to send handshake: " + e.getMessage());
        }
    }

    public byte[] booleanArrayToBytes(boolean[] bools) {
        byte[] bytes = new byte[bools.length];
        for (int i = 0; i < bools.length; i++) {
            bytes[i] = (byte) (bools[i] ? 1 : 0);
        }
        return bytes;
    }

    public void sendPiece(ObjectOutputStream out, int pieceIndex, byte[] pieceData) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(5 + pieceData.length);
        buffer.put((byte) 7);  // piece message code
        buffer.putInt(pieceIndex);
        buffer.put(pieceData);
        out.writeObject(buffer.array());
        out.flush();

        connectionManager.connections.get(otherPeerID).updateDownloadedBytes(pieceData.length);
    }

    public void sendBitfield(Socket socket) {
        try {
            byte[] bitfieldAsBytes = booleanArrayToBytes(fileManager.getBitfield());
            ByteBuffer buffer = ByteBuffer.allocate(1 + bitfieldAsBytes.length);
            buffer.put((byte) 5); // 5 is the defined code for a bitfield message.
            buffer.put(bitfieldAsBytes);
            socketOutput.writeObject(buffer.array());
            socketOutput.flush();
            System.out.println("Client: Bitfield has been sent.");
        } catch (IOException e) {
            System.out.println("Failed to send bitfield: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean[] receiveBitfield(byte[] data) {
        byte[] bitfieldBytes = Arrays.copyOfRange(data, 1, data.length);
        boolean[] bitfield = new boolean[bitfieldBytes.length];
        for (int i = 0; i < bitfieldBytes.length; i++) {
            bitfield[i] = bitfieldBytes[i] == 1;
        }
        System.out.println("Client: Bitfield received from Peer (ID = " + otherPeerID + "): ");
        for (int i = 0; i < bitfieldBytes.length; i++) {
            if (!bitfield[i]){
                break;
            }
            if (i == bitfield.length - 1){
                connectionManager.connections.get(otherPeerID).fileComplete = true; // the bitfield we got is complete - that file is complete.
            }
        }
//        System.out.println();

        return bitfield;
    }


    public boolean readHandshake(byte[] handshakeMessage){
        // byte buffer to parse the handshake message
        ByteBuffer handshakeBuffer = ByteBuffer.wrap(handshakeMessage);

        // extract the 18-byte header from the message
        byte[] headerBytes = new byte[18];
        handshakeBuffer.get(headerBytes);
        String header = new String(headerBytes);

        byte[] zeroBytes = new byte[10];
        handshakeBuffer.get(zeroBytes);

        // Extract the 4-byte peer ID
        int extractedPeerID = handshakeBuffer.getInt();

        if (!header.equals("P2PFILESHARINGPROJ")){
            System.out.println("The header does not match the handshake header.");
            return false;
        }
        else {
//            System.out.println("Header: " + header);
        }

//        System.out.println("Zero bytes: ");
        for (byte b : zeroBytes){
            if (b != 0){
                System.out.println("A zero byte was transmitted incorrectly.");
                return false;
            }
//            System.out.print(b);
        }
        System.out.println("Client: " + clientID + " received handshake from Peer: " + extractedPeerID);
        otherPeerID = extractedPeerID;

        // register connection with PCM
        connectionManager.registerConnection(otherPeerID, true);
        connectionManager.printConnections();

        return true;
    }

//    public void sendMessage(byte[] message, Socket socket){ // message is a string temporarily - will replace with one of the actual message types later
//        // socket validation
//        if (socket == null){
//            System.out.println("The message cannot be sent - the socket could not be found");
//            return;
//        }
//        if (socket.isClosed()){
//            System.out.println("The message cannot be sent - the socket is already closed.");
//            return;
//        }
//        // message validation
//        if (message.length == 0){
//            System.out.println("The message is empty - it cannot be sent.");
//            return;
//        }
//        try {
//            socketOutput.flush();
//            socketOutput.writeObject(message);
//        } catch (SocketException se) {
//            // potential causes: slow network, firewall, idle connection, or code errors
//            System.out.println("Error was encountered while trying to access the socket");
//        } catch (EOFException eof) {
//            // end of the stream was unexpectedly reached
//            System.out.println("Error was encountered while trying to access the output stream");
//        } catch (IOException e){
//            // most general IO exception handling
//            System.out.println("Error was encountered while trying to manage IO operations");
//        }
//    }

    public void sendInterested() throws IOException {
        System.out.println("Client: Expressing interest in " + otherPeerID);
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 2); // interested code
        socketOutput.writeObject(buffer.array());
        socketOutput.flush();
    }
    public void sendNotInterested() throws IOException {
        System.out.println("Client: Expressing NO interest in " + otherPeerID);
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 3); // interested code
        socketOutput.writeObject(buffer.array());
        socketOutput.flush();
    }

    public void sendHaveMessage(int index) throws IOException {
        System.out.println("Client sending have message for " + index + " to " + otherPeerID);
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) 4);
        buffer.putInt(index);
        socketOutput.writeObject(buffer.array());
        socketOutput.flush();
    }

    public int readHaveMessage(byte[] haveMessage){
        int pieceIndex = ByteBuffer.wrap(haveMessage, 1, 4).getInt();
        logger.receivedHave(otherPeerID, pieceIndex);
        return pieceIndex;
    }

    public void sendChokeMessage() throws IOException {
        System.out.println("Client: Sending choke message to " + otherPeerID);
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0); // choke code
        socketOutput.writeObject(buffer.array());
        socketOutput.flush();
    }
    public void sendUnchokeMessage() throws IOException {
        System.out.println("Client: Sending unchoke message to " + otherPeerID);
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 1); // choke code
        socketOutput.writeObject(buffer.array());
        socketOutput.flush();
    }
    public void maintainConnection(Socket requestSocket) {
        try {
            sendHandshake(requestSocket);
            Object response;
            response = socketInput.readObject();
            if (response instanceof byte[]){
                readHandshake((byte[]) response);
            }

            boolean[] myBitfield = Arrays.copyOf(fileManager.getBitfield(), fileManager.getBitfield().length);
            // send bitfield right after handshake
            sendBitfield(requestSocket);
            boolean[] otherBitfield = new boolean[0];

            // request missing pieces

            boolean isChoked = true;
            boolean areWeChoked = true;
            boolean wait = false;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    boolean[] currentBitfield = fileManager.getBitfield();
                    for (int i = 0; i < currentBitfield.length; i++) {
                        if (currentBitfield[i] && !myBitfield[i]) { // the fileManager's bitfield is different (i.e., some thread obtain a new byte)
//                            System.out.println("SUSPICIOUS!");
                            myBitfield[i] = true;
                            sendHaveMessage(i); // send a have message to the other peers!
                        }
                    }

                    if (connectionManager.disconnect){
                        System.out.println("Preparing to disconnect from peer " + otherPeerID);
                        sleep(8000);
                        System.out.println("Disconnecting from peer " + otherPeerID);
                        break;
                    }

                    boolean newChoked = connectionManager.connections.get(otherPeerID).isChoked();
                    if (newChoked != isChoked) { // choke status for this thread was changed
                        if (newChoked) { // if newChoked is true, we are setting this thread's peer to choked
                            sendChokeMessage();
                        } else { // newChoked is false, we are setting this thread's peer to unchoked
                            sendUnchokeMessage();
                        }
                        isChoked = newChoked;
                    }

                    if (!areWeChoked && !wait) {
                        ArrayList<Integer> missingPieces = new ArrayList<>();
                        for (int i = 0; i < myBitfield.length; i++) {
                            if (!myBitfield[i] && otherBitfield[i]) {
                                missingPieces.add(i);
                            }
                        }
                        if (!missingPieces.isEmpty()) {
                            int pieceToRequest = connectionManager.requestPiece(missingPieces);
//                            System.out.println(pieceToRequest);
                            if (pieceToRequest != -1) {
                                Request request = new Request(pieceToRequest);
                                byte[] requestBytes = request.toBytes();
                                socketOutput.writeObject(requestBytes);
                                socketOutput.flush();
                                wait = true;
                                System.out.println("Requested piece from peer " + otherPeerID + ": " + pieceToRequest);
                            }
                        }
                    }

                    response = socketInput.readObject();
//                    System.out.println("Identification");

                    if (response == null) {
                        System.out.println("Null response");
                    } else if (response instanceof byte[]) {
                        ByteBuffer buffer = ByteBuffer.wrap((byte[]) response);
                        byte messageType = buffer.get();
                        if (messageType == 0) { // choke message
                            System.out.println("Client: The other peer (" + otherPeerID + ") has choked us.");
                            logger.receivedChoked(otherPeerID);
                            areWeChoked = true;
                        } else if (messageType == 1) { // unchoke message
                            System.out.println("Client: The other peer (" + otherPeerID + ") has unchoked us.");
                            logger.receivedUnchoked(otherPeerID);
                            areWeChoked = false;
                        }
                        if (messageType == 2) {
                            System.out.println("Client :" + otherPeerID + " is indicating interest!");
                            logger.receivedInterested(otherPeerID);
                            connectionManager.peerInterested(otherPeerID, true);
                        } else if (messageType == 3) {
                            System.out.println("Client: " + otherPeerID + " is not interested.");
                            logger.receivedNotInterested(otherPeerID);
                            connectionManager.peerInterested(otherPeerID, false);
                        } else if (messageType == 4) { // have message
                            // update bitfield
                            int newBit = readHaveMessage(buffer.array());
                            otherBitfield[newBit] = true;
                            for (int i = 0; i < otherBitfield.length; i++) {
                                if (!otherBitfield[i]){
                                    break;
                                }
                                if (i == otherBitfield.length - 1){
                                    connectionManager.connections.get(otherPeerID).fileComplete = true; // the bitfield we got is complete - that file is complete.
                                }
                            }
                            if (!myBitfield[newBit]) { // if we don't have the new bit they got, send interested
                                sendInterested(); //TODO: ensure this works!!!! it works if 1001 and 1002 are launched before 1003, but not if all are launched concurrently.
                            }
                            System.out.println("ClientHave: " + otherPeerID + " has a new piece at " + newBit + "!");
                        } else if (messageType == 5) { // bitfield message
                            otherBitfield = receiveBitfield(buffer.array());
                            for (int i = 0; i < myBitfield.length; i++) {
                                if (!myBitfield[i] && otherBitfield[i]) { // other bitfield has a bit we lack...
                                    System.out.println("Client: Indicating interest in piece from peer " + otherPeerID + ": " + i);
                                    sendInterested();
                                    break;
                                }
                                if (i == myBitfield.length - 1) { // we have checked every bit from the other bitfield, and we need none...
                                    System.out.println("Client: Indicating a lack of interest to peer " + otherPeerID);
                                    sendNotInterested();
                                }
                            }
                        } else if (messageType == 6) { // request
                            System.out.println("Client: We received a request");
                            handleIncomingRequests(buffer.array(), socketOutput, fileManager);
                        } else if (messageType == 7 && !fileManager.hasAllPieces()) {
//                        System.out.println("Byte array was a piece message");
                            int pieceIndex = buffer.getInt();
                            byte[] pieceData = new byte[buffer.remaining()];
                            buffer.get(pieceData);
                            if (!fileManager.hasPiece(pieceIndex)) {
                                fileManager.storePiece(pieceIndex, pieceData);
                                logger.hasDownloaded(otherPeerID, pieceIndex);
                                System.out.println("Client: Received and stored piece index: " + pieceIndex + " with length: " + pieceData.length + " - " + otherPeerID);
//                                sendHaveMessage(pieceIndex);
                                wait = false;
                            } else {
                                System.out.println("We already have piece at index: " + pieceIndex + " - client for peer " + otherPeerID);
                            }
                            if (fileManager.hasAllPieces()) {
                                System.out.println("Client: Bitfield is complete!!!!");
                                logger.downloadComplete();
                                fileManager.writeToFile();
                                connectionManager.hasCompleteFile = true;
                            }
                        }
                    }

//                    for (int i = 0; i < fileManager.getBitfield().length; i++) {
//                        if (!fileManager.hasPiece(i)) { // doesn't have this part of the file yet
//                            break;
//                        }
//                        if (i == fileManager.getBitfield().length - 1) {
//                            System.out.println("Bitfield is complete!!!!");
//                            fileManager.writeToFile();
//                        }
//                        System.out.println(fileManager.hasAllPieces());
//                    }
                } catch (SocketTimeoutException ste) {
                    // Log and handle timeout
//                    System.out.println("Read timed out, checking connection status...");
                    if (!connectionManager.connections.get(otherPeerID).isChoked()) {

                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Communication error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeClient();
        }
    }
    public void handleIncomingRequests(byte[] requestBytes, ObjectOutputStream out, FileManager fileManager) throws IOException {
        if (requestBytes.length >= 5) { // byte 1 is to identify msg type, 2-5 are to get index. length must exceed this
            int pieceIndex = ByteBuffer.wrap(requestBytes, 1, 4).getInt();
            if (fileManager.hasPiece(pieceIndex)) {
                byte[] pieceData = fileManager.getPiece(pieceIndex);
                if (pieceData != null) {
                    sendPiece(out, pieceIndex, pieceData);
                    System.out.println("Sent piece: " + pieceIndex);
                } else {
                    System.out.println("Error: No data for piece " + pieceIndex);
                }
            } else {
                System.out.println("Do not have requested piece index: " + pieceIndex);
            }
        } else {
            System.out.println("Invalid request message received");
        }
    }

    public void sendCommunication(){
        String message;
        while (true){
            try {
                message = consoleInput.readLine();
//                sendMessage(message, this.requestSocket);
                socketOutput.writeObject(message);
                socketOutput.flush();
                if (message.equals("Bye")){
                    break;
                }
                message = (String)socketInput.readObject();
                System.out.println(message + " - received by peer " + clientID);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error in writing simple message");
            }
        }
    }

    public void closeClient(){
        // terminate the connection
        System.out.println("Goodbye server! Closing connection.");
        try {
            if (socketOutput != null)  socketOutput.close();
            if (socketInput != null) socketInput.close();
            if (requestSocket != null && !requestSocket.isClosed()) requestSocket.close();
        }
        catch (IOException e) {
            System.out.println("Error in disconnecting the client-server interface!");
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[])
        {

        }

}
