package tcpProcess;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
// Contains the "server" capabilities of a peer (uploading files to other peers)

public class tcp_server
{

    public int port;
    public int serverID;
    ObjectInputStream socketInput;
    ObjectOutputStream socketOutput;
    ServerSocket server;
    Socket socket;

    public tcp_server(int port, int id){
        this.port = port;
        this.serverID = id;
    }

    public void launchServer(){
        try
        {
            this.server = new ServerSocket(port); //TODO: get port number from configs
            InetAddress local = InetAddress.getLocalHost();
            System.out.println("Peer " + serverID + " has launched a TCP Server with port " + port);
            System.out.println("and IP " + local.getHostAddress() + " on peer " + serverID);

            // program stops here until a client issues a connection request
            this.socket = server.accept();
            System.out.println("Incoming connection detected from client");

            // takes input from the client socket
            this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
            this.socketInput = new ObjectInputStream((socket.getInputStream()));

            // receive handshake
            boolean handshakeStatus;
            try {
                handshakeStatus = readHandshake((byte[]) socketInput.readObject());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (!handshakeStatus){
                System.out.println("The handshake was unsuccessful.");
                return;
            }
            System.out.println("The handshake was successful.");
//            sendHandshake(this.socket);

            // Keep listening for messages
            while (true) {
                Object message = socketInput.readObject();
                if (message instanceof String) { // Assuming message is a String
                    System.out.println("Received: " + message);
                    // Process message or respond
                    socketOutput.writeObject("Ack: " + message);
                    socketOutput.flush();
                }
                // Implement your protocol's termination condition here
                if (message.equals("exit")) {
                    break;
                }
            }
        } catch(IOException i)
        {
            System.out.println("Error in connection with client or input detection");
            throw new RuntimeException(i);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendHandshake(Socket socket) {
        ByteBuffer buffer = ByteBuffer.allocate(32);
        byte[] header = "P2PFILESHARINGPROJ".getBytes();
        byte[] zeroBits = new byte[10]; // Ensure this is zero-initialized
        int peerID = serverID; // Assuming `serverID` is available here

        buffer.put(header);
        buffer.put(zeroBits);
        buffer.putInt(peerID);

        sendMessage(buffer.array(), socket);
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

    public void closeServer() throws IOException {
        System.out.println("See you later client! Closing connection");
        socketInput.close();
        socketOutput.close();
        socket.close();
    }
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