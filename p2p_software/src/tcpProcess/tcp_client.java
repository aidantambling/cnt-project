package tcpProcess;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

// Contains the "client" capabilities of a peer (requesting / downloading files from other peers)
public class tcp_client {
    int port;
    public int clientID;
    BufferedReader consoleInput; // read input from the command line
    ObjectOutputStream socketOutput; // write to the socket
    ObjectInputStream socketInput; // read from the socket

    ArrayList<Socket> sockets;
    Socket requestSocket;
    public tcp_client(int port, int id){
        this.port = port;
        this.clientID = id;
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

//            // input is taken in from the console
//            this.consoleInput = new BufferedReader(new InputStreamReader(System.in));
//            // output from the socket is sent to the server socket for reading
            this.socketOutput = new ObjectOutputStream(this.requestSocket.getOutputStream());
            this.socketOutput.flush();
            this.socketInput = new ObjectInputStream((this.requestSocket.getInputStream()));
        } catch (Exception e){
            System.out.println("Error in establishing a socket connection!");
            throw new RuntimeException(e);
        }
        // send handshake
//        sendHandshake(requestSocket);
        // receive handshake
//        boolean handshakeStatus;
//        try {
//            handshakeStatus = readHandshake((byte[]) socketInput.readObject());
//        } catch (IOException | ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        if (!handshakeStatus){
//            System.out.println("The handshake was unsuccessful.");
//            return;
//        }
//        System.out.println("The handshake was successful.");

        maintainConnection(requestSocket);

        // now, actual messages can be sent.
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

    public void maintainConnection(Socket requestSocket) {
        try {
            sendHandshake(requestSocket);
            Object response;
            response = socketInput.readObject();
            if (response instanceof byte[]){
                readHandshake((byte[]) response);
            }
            while (true) {
                // Read an object from the stream
                response = socketInput.readObject();

                // Handle different types of responses appropriately
                if (response instanceof String) {
                    System.out.println("Response from server: " + response);
                    // Example check for termination condition
                    if (((String) response).equals("exit")) {
                        break;
                    }
                } else if (response instanceof byte[]) {
                    System.out.println("Received byte array from server, length: " + ((byte[]) response).length);
                    // Additional handling for byte arrays if needed
                }
                // Add handling for other expected types if necessary
            }
        } catch (Exception e) {
            System.out.println("Communication error: " + e.getMessage());
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
