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
            this.server = new ServerSocket(port);
            InetAddress local = InetAddress.getLocalHost();
            System.out.println("TCP Server has been launched with port " + port);
            System.out.println("and IP " + local.getHostAddress() + "on peer " + serverID);

            // program stops here until a client issues a connection request
            this.socket = server.accept();
            System.out.println("Incoming connection detected from client");
            sendHandshake(this.socket);

            // takes input from the client socket
            this.socketInput = new ObjectInputStream((socket.getInputStream()));
            this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch(IOException i)
        {
            System.out.println("Error in connection with client or input detection");
            throw new RuntimeException(i);
        }
    }

    public void sendHandshake(Socket socket){
        String tempHandshake = "Handshake from server"; // will replace with an actual handshake message later
        sendMessage(tempHandshake, socket);
    }

    public void readHandshake(byte[] handshakeMessage){
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
            return;
        }
        else {
            System.out.println("Header: " + header);
        }

        System.out.println("Zero bytes: ");
        for (byte b : zeroBytes){
            if (b != 0){
                System.out.println("A zero byte was transmitted incorrectly.");
                return;
            }
            System.out.print(b);
        }
        System.out.println("Peer ID: " + extractedPeerID);
    }

    public void sendMessage(String message, Socket socket){ // message is a string temporarily - will replace with one of the actual message types later
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
        if (message.equals("")){
            System.out.println("The message is empty - it cannot be sent.");
            return;
        }
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            out.writeObject(message);
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


    public void communicate(){
        String message = "";
        while (true)
        {
            try
            {
                message = (String)socketInput.readObject();
                if (message.equals("Bye")){
                    break;
                }
                System.out.println(message + " - message received by peer " + serverID);
                sendMessage("Demo Message", this.socket);
                socketOutput.flush();
            }
            catch(Exception e)
            {
                System.out.println("Error in reading!");
                throw new RuntimeException(e);
            }
        }
    }

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