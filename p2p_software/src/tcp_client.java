import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
public class tcp_client {

        public static void main(String args[])
        {
            // hard coded port of process
            int port = 1664;

            // address is passed in as an argument from command line
            String address;
            try {
                address = args[0];
            } catch (Exception e){
                System.out.println("Error: hostname was not provided");
                throw new RuntimeException(e);
            }

            // resolve an IP address from the passed hostname
            InetAddress IP;
            try {
                IP = InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                System.out.println("Error: IP address could not be resolved from the hostname");
                throw new RuntimeException(e);
            }

            // use the address and port to create a client socket
            BufferedReader consoleInput; // read input from the command line
            ObjectOutputStream socketOutput; // write to the socket
            ObjectInputStream socketInput; // read from the socket
            Socket requestSocket;
            try {
                requestSocket = new Socket(IP, port);
                System.out.println("Client-side socket established.");
                System.out.println("Hostname: " + address);
                System.out.println("Address: " + IP.getHostAddress());
                System.out.println("Port: " + port);

                // input is taken in from the console
                consoleInput = new BufferedReader(new InputStreamReader(System.in));
                // output from the socket is sent to the server socket for reading
                socketOutput = new ObjectOutputStream(requestSocket.getOutputStream());
                socketOutput.flush();
                socketInput = new ObjectInputStream((requestSocket.getInputStream()));
            } catch (Exception e){
                System.out.println("Error in establishing a socket connection!");
                throw new RuntimeException(e);
            }

            String message;
            while (true){
                try {
                    message = consoleInput.readLine();
                    socketOutput.writeObject(message);
                    socketOutput.flush();
                    if (message.equals("Bye")){
                        break;
                    }
                    message = (String)socketInput.readObject();
                    System.out.println(message);
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error in writing simple message");
                }
            }
            try {
                File file = new File("client_content/new_img.png");
                byte[] content = (byte[]) socketInput.readObject();
                Files.write(file.toPath(), content);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

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
}
