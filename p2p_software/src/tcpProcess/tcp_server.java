package tcpProcess;
import Peer.FileManager;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
// Contains the "server" capabilities of a peer (uploading files to other peers)

public class tcp_server
{

    public int port;
    public int serverID;
    ServerSocket server;
    private FileManager fileManager;
//    Socket socket;

    public tcp_server(int port, int id, FileManager fileManager){
        this.port = port;
        this.serverID = id;
        this.fileManager = fileManager;
    }

    public void launchServer() {
        try {
            server = new ServerSocket(port);
            InetAddress local = InetAddress.getLocalHost();
            System.out.println("Peer " + serverID + " has launched a TCP Server with port " + port);
            System.out.println("and IP " + local.getHostAddress() + " on peer " + serverID);

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("Incoming connection detected from client");

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException i) {
            System.out.println("Error in connection with client or input detection");
            throw new RuntimeException(i);
        }
    }

    class ClientHandler implements Runnable {
        private Socket clientSocket;
        ObjectInputStream socketInput;
        ObjectOutputStream socketOutput;

        public int otherPeerID;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                socketOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                socketOutput.flush();
                socketInput = new ObjectInputStream(clientSocket.getInputStream());

                sendHandshake(clientSocket); // Send a handshake upon connecting
                Object response;
                response = socketInput.readObject();
                if (response instanceof byte[]){
                    readHandshake((byte[]) response);
                }

                // send bitfield right after handshake
                sendBitfield(clientSocket);
//                receiveBitfield(clientSocket);

                // TODO: peers need to be able to request pieces they lack, and respond to requests for pieces they own

                //TODO: reciprocate this in tcp_client
                while (true) {
                    response = socketInput.readObject();

                    if (response instanceof byte[]){
                        ByteBuffer buffer = ByteBuffer.wrap((byte[]) response);
                        byte messageType = buffer.get();
//                        byte[] remainingBytes = new byte[buffer.remaining()];
                        if (messageType == 0){ // choke message

                        }
                        else if (messageType == 1){ // unchoke message

                        }
                        else if (messageType == 2){ // interested message

                        }
                        else if (messageType == 3){ // not-interested message

                        }
                        else if (messageType == 4){ // have message

                        }
                        else if (messageType == 5){ // bitfield message
                            System.out.println("Bitfield message received.");
                            receiveBitfield(buffer.array());
                        }
                        else if (messageType == 6){ // request message
                            System.out.println("We received a request");
                            handleIncomingRequests(buffer.array(), socketOutput, fileManager);
                        }
                        else if (messageType == 7){ // piece message

                        }
                    }
                    else if (response instanceof String) {
                        System.out.println("Response from server: " + response);
                        if (response.equals("exit")) {
                            break;
                        }
                    } else if (response instanceof byte[]) {
                        System.out.println("Received byte array from server, length: " + ((byte[]) response).length);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client [" + clientSocket + "]: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing connection for client [" + clientSocket + "]");
                }
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

        public void sendPiece(ObjectOutputStream out, int pieceIndex, byte[] pieceData) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(5 + pieceData.length);
            buffer.put((byte) 7);  // 7 is the defined code for a piece message.
            buffer.putInt(pieceIndex);
            buffer.put(pieceData);
            out.writeObject(buffer.array());
            out.flush();
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

        public void closeServer() throws IOException {
            System.out.println("See you later client! Closing connection");
            socketInput.close();
            socketOutput.close();
//            socket.close();
        }

        public void sendHandshake(Socket socket) {
            try {
                byte[] handshake = "P2PFILESHARINGPROJ".getBytes();
                byte[] zeroBits = new byte[10]; // Ensure zero initialization
                ByteBuffer buffer = ByteBuffer.allocate(32);
                buffer.put(handshake);
                buffer.put(zeroBits);
                buffer.putInt(serverID); // Use serverID or another unique identifier
                socketOutput.writeObject(buffer.array()); // Send the handshake
                socketOutput.flush();
                System.out.println("Handshake sent: " + Arrays.toString(buffer.array()));
            } catch (IOException e) {
                System.out.println("Failed to send handshake: " + e.getMessage());
            }
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
            System.out.println();
            System.out.println("Peer ID: " + extractedPeerID);
            this.otherPeerID = extractedPeerID;
            return true;
        }

    }

//    public void communicate(){
//        String message = "";
//        while (true)
//        {
//            try
//            {
//                message = (String)socketInput.readObject();
//                if (message.equals("Bye")){
//                    break;
//                }
//                System.out.println(message + " - message received by peer " + serverID);
//                sendMessage("Demo Message", this.socket);
//                socketOutput.flush();
//            }
//            catch(Exception e)
//            {
//                System.out.println("Error in reading!");
//                throw new RuntimeException(e);
//            }
//        }
//    }

    public static void main(String args[])
    {

    }

    // method which takes a file name and sends it from the server to the client requesting that file
    public static void sendFile(String filepath, ObjectOutputStream writeFile) throws IOException {
        File file = new File(filepath);
        byte[] buffer = Files.readAllBytes(file.toPath());
        writeFile.writeObject(buffer);
    }
}