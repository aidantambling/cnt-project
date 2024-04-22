package tcpProcess;
import Peer.FileManager;
import Peer.Messages.Request;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

// Contains the "client" capabilities of a peer (requesting / downloading files from other peers)
public class tcp_client {
    int port;
    public int clientID;

    public int otherPeerID;
    BufferedReader consoleInput; // read input from the command line
    ObjectOutputStream socketOutput; // write to the socket
    ObjectInputStream socketInput; // read from the socket
    private FileManager fileManager;

    ArrayList<Socket> sockets;
    Socket requestSocket;
    public tcp_client(int port, int id, FileManager fileManager){
        this.port = port;
        this.clientID = id;
        this.fileManager = fileManager;
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
            byte[] zeroBits = new byte[10]; // Ensure zero initialization
            ByteBuffer buffer = ByteBuffer.allocate(32);
            buffer.put(handshake);
            buffer.put(zeroBits);
            buffer.putInt(clientID); // Use serverID or another unique identifier
            socketOutput.writeObject(buffer.array()); // Send the handshake
            socketOutput.flush();
            System.out.println("Handshake sent: " + Arrays.toString(buffer.array()));
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

    public void sendBitfield(Socket socket) {
        try {
            byte[] bitfieldAsBytes = booleanArrayToBytes(fileManager.getBitfield());
            ByteBuffer buffer = ByteBuffer.allocate(1 + bitfieldAsBytes.length);
            buffer.put((byte) 5); // 5 is the defined code for a bitfield message.
            buffer.put(bitfieldAsBytes);
            socketOutput.writeObject(buffer.array());
            socketOutput.flush();
            System.out.println("Bitfield has been sent.");
        } catch (IOException e) {
            System.out.println("Failed to send bitfield: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean[] receiveBitfield(byte[] data) {
        // Assuming the first byte is the message type and is skipped when this method is called.
        byte[] bitfieldBytes = Arrays.copyOfRange(data, 1, data.length); // Skip the first byte (message type)
        boolean[] bitfield = new boolean[bitfieldBytes.length];
        for (int i = 0; i < bitfieldBytes.length; i++) {
            bitfield[i] = bitfieldBytes[i] == 1;
        }
        System.out.println("Bitfield received from Peer: ");
        for (boolean b : bitfield) {
            System.out.print(b ? "1" : "0");
        }
        System.out.println(); // Print newline for better format

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
            System.out.println("Header: " + header);
        }

        System.out.println("Zero bytes: ");
        for (byte b : zeroBytes){
            if (b != 0){
                System.out.println("A zero byte was transmitted incorrectly.");
                return false;
            }
            System.out.print(b);
        }
        System.out.println("Peer ID: " + extractedPeerID);
        otherPeerID = extractedPeerID;
        return true;
    }

    public void sendMessage(byte[] message, Socket socket){ // message is a string temporarily - will replace with one of the actual message types later
        // socket validation
        if (socket == null){
            System.out.println("The message cannot be sent - the socket could not be found");
            return;
        }
        if (socket.isClosed()){
            System.out.println("The message cannot be sent - the socket is already closed.");
            return;
        }
        // message validation
        if (message.length == 0){
            System.out.println("The message is empty - it cannot be sent.");
            return;
        }
        try {
            socketOutput.flush();
            socketOutput.writeObject(message);
        } catch (SocketException se) {
            // potential causes: slow network, firewall, idle connection, or code errors
            System.out.println("Error was encountered while trying to access the socket");
        } catch (EOFException eof) {
            // end of the stream was unexpectedly reached
            System.out.println("Error was encountered while trying to access the output stream");
        } catch (IOException e){
            // most general IO exception handling
            System.out.println("Error was encountered while trying to manage IO operations");
        }
    }

    public void sendInterested() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 2); // interested code
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

            // send bitfield right after handshake
            sendBitfield(requestSocket);
            boolean[] otherBitfield;

            // request missing pieces
            // TODO: implement this in server, too.
            boolean[] myBitfield = fileManager.getBitfield();

            // improper request submitter - used until better algorithms implemented
//            for (int i = 0; i < myBitfield.length; i++){
//                if (!myBitfield[i] && otherBitfield[i]){ // other bitfield has a bit we lack...
//                    Request request = new Request(i);
//                    byte[] requestBytes = request.toBytes();
//                    socketOutput.writeObject(requestBytes);
//                    socketOutput.flush();
//                    System.out.println("Requested piece from peer " + otherPeerID + ": " + i);
//                }
//            }

            while (true) {
//                System.out.println("Looping");
                response = socketInput.readObject();

                if (response == null){
                    System.out.println("Null response");
                }
                else if (response instanceof String) {
                    System.out.println("Response from peer " + otherPeerID + ": " + response);
                    if (((String) response).equals("exit")) {
                        break;
                    }
                } else if (response instanceof byte[]) {
//                    System.out.println("Received byte array from peer " + otherPeerID + ", length: " + ((byte[]) response).length);
                    ByteBuffer buffer = ByteBuffer.wrap((byte[]) response);
                    byte messageType = buffer.get();
                    if (messageType == 2){
                        System.out.println(otherPeerID + " is indicating interest!");
                    }
                    else if (messageType == 5){ // bitfield message
                        System.out.println("Bitfield message received.");
                        otherBitfield = receiveBitfield(buffer.array());
                        //TODO: check the pieces this peer lacks, and send "interested" message
                        // interested just indicates general interest in the other peer.
                        for (int i = 0; i < myBitfield.length; i++){
                            if (!myBitfield[i] && otherBitfield[i]){ // other bitfield has a bit we lack...
//                                Request request = new Request(i);
//                                byte[] requestBytes = request.toBytes();
//                                socketOutput.writeObject(requestBytes);
//                                socketOutput.flush();
                                System.out.println("Indicating interest in piece from peer " + otherPeerID + ": " + i);
                                sendInterested();
                                break;
                            }
                        }
                    }
                    else if (messageType == 7 && !fileManager.hasAllPieces()){
//                        System.out.println("Byte array was a piece message");
                        int pieceIndex = buffer.getInt();  // Next 4 bytes: piece index
                        byte[] pieceData = new byte[buffer.remaining()];
                        buffer.get(pieceData);
                        if (!fileManager.hasPiece(pieceIndex)) {
                            fileManager.storePiece(pieceIndex, pieceData);  // Assuming you have a method to store pieces
                            System.out.println("Received and stored piece index: " + pieceIndex + " with length: " + pieceData.length + " - " + otherPeerID);
                        } else {
                            System.out.println("We already have piece at index: " + pieceIndex + " - client for peer " + otherPeerID);
                        }
                        if (fileManager.hasAllPieces()){
                            System.out.println("Bitfield is complete!!!!");
                            fileManager.writeToFile();
                        }
                    }
                }

                for (int i = 0; i < fileManager.getBitfield().length; i++){
                    if (!fileManager.hasPiece(i)){ // doesn't have this part of the file yet
                        break;
                    }
                    if (i == fileManager.getBitfield().length - 1){
                        System.out.println("Bitfield is complete!!!!");
                        fileManager.writeToFile();
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
            socketOutput.close();
            socketInput.close();
            requestSocket.close();
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
