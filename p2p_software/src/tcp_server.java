import java.net.*;
import java.io.*;
import java.nio.file.Files;

public class tcp_server
{

    public static void main(String args[])
    {
        // hard coded port
        int port = 1664;

        // launches the server on the given platform with the hard coded port
        try
        {
            ServerSocket server = new ServerSocket(port);
            InetAddress local = InetAddress.getLocalHost();
            System.out.println("TCP Server has been launched with port " + port);
            System.out.println("and IP " + local.getHostAddress());

            // program stops here until a client issues a connection request
            Socket socket = server.accept();
            System.out.println("Incoming connection detected from client");

            // takes input from the client socket
            ObjectInputStream socketInput = new ObjectInputStream((socket.getInputStream()));
            ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
            String message = "x";

            while (true)
            {
                try
                {
                    message = (String)socketInput.readObject();
                    if (message.equals("Bye")){
                        break;
                    }
                    System.out.println(message);
                    socketOutput.writeObject("Received");
                    socketOutput.flush();
                }
                catch(Exception e)
                {
                    System.out.println("Error in reading!");
                    throw new RuntimeException(e);
                }
            }
            try {
                sendFile("gator.png", socketOutput);
            } catch(Exception e)
            {
                System.out.println("Error in reading!");
                throw new RuntimeException(e);
            }
            // terminate the connection
            System.out.println("See you later client! Closing connection");
            socketInput.close();
            socketOutput.close();
            socket.close();
        }

        catch(IOException i)
        {
            System.out.println("Error in connection with client or input detection");
            throw new RuntimeException(i);
        }
    }

    // method which takes a file name and sends it from the server to the client requesting that file
    public static void sendFile(String filepath, ObjectOutputStream writeFile) throws IOException {
        File file = new File(filepath);
        byte[] buffer = Files.readAllBytes(file.toPath());
        writeFile.writeObject(buffer);
    }
}