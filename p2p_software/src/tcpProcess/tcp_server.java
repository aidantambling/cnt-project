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

                boolean[] myBitfield = fileManager.getBitfield();
                boolean[] otherBitfield;

                sendHandshake(clientSocket); // Send a handshake upon connecting
                Object response;
                response = socketInput.readObject();
                if (response instanceof byte[]){
                    readHandshake((byte[]) response);
                }

                //TODO: check if the other peer (otherPeerID) is the "right neighbor"
                // choke and unchoke:
                // the peer uploads only to preferred neighbors (at most k of them) and an optimistically unchoked one
                // the preferred neighbors are determined every p seconds
                // to do this, calculate downloading rate of each neighbors over the previous interval
                // among neighbors that are interested, pick k neighbors that downloaded at highest rate (if 2x, randomly)
                // send unchoke messages to those neighbors (expect requests) and choke the others
                // HOWEVER: if peer has a complete file, it determines preferred neighbors randomly among the interested

                //TODO: optimistic unchoking

                //TODO: IMPLEMENT THE ABOVE via a ConnectionManager class that a Peer has an object of. The connectionManager
                // oversees the peer as a whole, and can follow the algorithm above to determine peers etc. It can be passed
                // down to both server(here) and client. and the server and client can update it with rates and can also monitor
                // it to determine if any choking / unchoking happens

                //TODO: this approach is to centralize the monitoring of a peer's connections. our classes tcp_client and
                // tcp_server are split in a way that a peer has some connections on one, some on the other. so to keep state
                // between them, we need this connectionManager. it is needed in a similar sense as fileManager is needed
                // to update the bitfields consistently across tcp_server/tcp_client.


                // send bitfield right after handshake
                sendBitfield(clientSocket);

                while (true) {
                    response = socketInput.readObject();

                    if (response instanceof byte[]){
                        ByteBuffer buffer = ByteBuffer.wrap((byte[]) response);
                        byte messageType = buffer.get();
                        if (messageType == 0){ // choke message

                        }
                        else if (messageType == 1){ // unchoke message

                        }
                        else if (messageType == 2){ // interested message
                            System.out.println(otherPeerID + " is indicating interest!");
                        }
                        else if (messageType == 3){ // not-interested message
                            System.out.println(otherPeerID + " is not interested.");
                        }
                        else if (messageType == 4){ // have message

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
                                if (i == myBitfield.length - 1){ // we have checked every bit from the other bitfield, and we need none...
                                    System.out.println("Indicating a lack of interest to peer " + otherPeerID + ": " + i);
                                    sendNotInterested();
                                }
                            }
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

        public void sendNotInterested() throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(1);
            buffer.put((byte) 3); // not intersted code
            socketOutput.writeObject(buffer.array());
            socketOutput.flush();
        }

        public void sendPiece(ObjectOutputStream out, int pieceIndex, byte[] pieceData) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(5 + pieceData.length);
            buffer.put((byte) 7);  // piece message code
            buffer.putInt(pieceIndex);
            buffer.put(pieceData);
            out.writeObject(buffer.array());
            out.flush();
        }

        public void sendInterested() throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(1);
            buffer.put((byte) 2); // interested code
            socketOutput.writeObject(buffer.array());
            socketOutput.flush();
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
            byte[] bitfieldBytes = Arrays.copyOfRange(data, 1, data.length);
            boolean[] bitfield = new boolean[bitfieldBytes.length];
            for (int i = 0; i < bitfieldBytes.length; i++) {
                bitfield[i] = bitfieldBytes[i] == 1;
            }
            System.out.println("Bitfield received from Peer: ");
            for (boolean b : bitfield) {
                System.out.print(b ? "1" : "0");
            }
            System.out.println();

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