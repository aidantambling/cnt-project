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

    public void sendBitfield(Socket socket) {
        try {
            socketOutput.writeObject(fileManager.getBitfield());
            socketOutput.flush();
            System.out.println("Bitfield has been sent.");
        } catch (IOException e) {
            System.out.println("Failed to send bitfield: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean[] receiveBitfield(Socket socket){
        try {
            boolean[] bitfield = (boolean[]) socketInput.readObject();
            System.out.println("Bitfield received from Peer " + otherPeerID + ": ");
            for (boolean b : bitfield){
                System.out.print(b);
            }
            return bitfield;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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
            boolean[] otherBitfield = receiveBitfield(requestSocket);

            // TODO: peers need to be able to request pieces they lack, and respond to requests for pieces they own

            // request missing pieces
            // TODO: implement this in server, too.
            boolean[] myBitfield = fileManager.getBitfield();
            for (int i = 0; i < myBitfield.length; i++){
                if (!myBitfield[i] && otherBitfield[i]){ // other bitfield has a bit we lack...
                    Request request = new Request(i);
                    byte[] requestBytes = request.toBytes();
                    socketOutput.writeObject(requestBytes);
                    socketOutput.flush();
                    System.out.println("Requested piece: " + i);
                }
            }

            while (true) {
                System.out.println("Looping");
                response = socketInput.readObject();

                if (response == null){
                    System.out.println("Null response");
                }
                else if (response instanceof String) {
                    System.out.println("Response from server: " + response);
                    if (((String) response).equals("exit")) {
                        break;
                    }
                } else if (response instanceof byte[]) {
                    System.out.println("Received byte array from server, length: " + ((byte[]) response).length);
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
