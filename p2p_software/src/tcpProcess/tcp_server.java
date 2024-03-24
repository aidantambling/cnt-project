package tcpProcess;
import java.net.*;
import java.io.*;
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

            // takes input from the client socket
            this.socketInput = new ObjectInputStream((socket.getInputStream()));
            this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch(IOException i)
        {
            System.out.println("Error in connection with client or input detection");
            throw new RuntimeException(i);
        }
    }

    public void sendMessage(String message, Socket socket){
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            out.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
//        // hard coded port
//        int port = 1664;
//
//        // launches the server on the given platform with the hard coded port
//        try
//        {
//            ServerSocket server = new ServerSocket(port);
//            InetAddress local = InetAddress.getLocalHost();
//            System.out.println("TCP Server has been launched with port " + port);
//            System.out.println("and IP " + local.getHostAddress());
//
//            // program stops here until a client issues a connection request
//            Socket socket = server.accept();
//            System.out.println("Incoming connection detected from client");
//
//            // takes input from the client socket
//            ObjectInputStream socketInput = new ObjectInputStream((socket.getInputStream()));
//            ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
//            String message = "x";
//
//            while (true)
//            {
//                try
//                {
//                    message = (String)socketInput.readObject();
//                    if (message.equals("Bye")){
//                        break;
//                    }
//                    System.out.println(message);
//                    socketOutput.writeObject("Received");
//                    socketOutput.flush();
//                }
//                catch(Exception e)
//                {
//                    System.out.println("Error in reading!");
//                    throw new RuntimeException(e);
//                }
//            }
//            try {
//                sendFile("gator.png", socketOutput);
//            } catch(Exception e)
//            {
//                System.out.println("Error in reading!");
//                throw new RuntimeException(e);
//            }
//            // terminate the connection
//            System.out.println("See you later client! Closing connection");
//            socketInput.close();
//            socketOutput.close();
//            socket.close();
//        }
//
//        catch(IOException i)
//        {
//            System.out.println("Error in connection with client or input detection");
//            throw new RuntimeException(i);
//        }
    }

    // method which takes a file name and sends it from the server to the client requesting that file
    public static void sendFile(String filepath, ObjectOutputStream writeFile) throws IOException {
        File file = new File(filepath);
        byte[] buffer = Files.readAllBytes(file.toPath());
        writeFile.writeObject(buffer);
    }
}