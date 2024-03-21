import java.net.*;
import java.io.*;
public class tcp_server
{

    public static void main(String args[])
    {
        // hard coded port - last 4 digits of UFID
        int port = 1664;

        // launches the server on the given platform with the hard coded port
        try
        {
            ServerSocket server = new ServerSocket(port);
            InetAddress local = InetAddress.getLocalHost();
            System.out.println("TCP Server has been launched with port " + port);
            System.out.println("and IP " + local.getHostAddress());

            // when a client connects with the appropriate address + port, the program continues
            Socket socket = server.accept();
            System.out.println("Incoming connection detected from client");

            // takes input from the client socket
            DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            String clientInput = "";

            long[] timeArr2 = new long[10];
            int counter = 0;

            // reads commands from clients; provides corresponding meme
            for (int i = 0; i < 10; i++)
            {
                try
                {
                    clientInput = input.readUTF();

                    long C1 = System.currentTimeMillis(); // first measurement of meme access time

                    // send the appropriate meme file based on the requested image
                    if (clientInput.equals("Meme 1")){
                        sendFile("meme1.jpg", output);
                    }
                    else if (clientInput.equals("Meme 2")){
                        sendFile("meme2.jpeg", output);
                    }
                    else if (clientInput.equals("Meme 3")){
                        sendFile("meme3.jpg", output);
                    }
                    else if (clientInput.equals("Meme 4")){
                        sendFile("meme4.jpeg", output);
                    }
                    else if (clientInput.equals("Meme 5")){
                        sendFile("meme5.jpg", output);
                    }
                    else if (clientInput.equals("Meme 6")){
                        sendFile("meme6.jpg", output);
                    }
                    else if (clientInput.equals("Meme 7")){
                        sendFile("meme7.jpg", output);
                    }
                    else if (clientInput.equals("Meme 8")){
                        sendFile("meme8.jpg", output);
                    }
                    else if (clientInput.equals("Meme 9")){
                        sendFile("meme9.png", output);
                    }
                    else if (clientInput.equals("Meme 10")){
                        sendFile("meme10.jpg", output);
                    }
                    else if (clientInput.equals("bye")){
                        break;
                    }
                    else {
                        System.out.println("Error: " + clientInput + " is not a command!");
                    }
                    long C2 = System.currentTimeMillis(); // second measurement of meme access time
                    timeArr2[i] = C2 - C1;

                    System.out.println(clientInput + " has been sent");
                }
                catch(Exception e)
                {
                    System.out.println("Error in reading!");
                    throw new RuntimeException(e);
                }
            }

            // time calculations and displaying of values
            System.out.println("----------------------------------------------------");
            System.out.println("Time to access each of 10 memes (ms): ");
            for (int i = 0; i < 10; i++){
                System.out.println(timeArr2[i]);
            }
            long min = timeArr2[0];
            for (int i = 0; i < 10; i++){
                if (timeArr2[i] < min){
                    min = timeArr2[i];
                }
            }

            long max = timeArr2[0];
            for (int i = 0; i < 10; i++){
                if (timeArr2[i] > max){
                    max = timeArr2[i];
                }
            }

            long sum = 0;
            double mean = 0;
            for (int i = 0; i < 10; i++){
                sum += timeArr2[i];
            }
            mean = sum / 10.0;

            double sd = 0;
            sum = 0;
            for (int i = 0; i < 10; i++){
                sum += (timeArr2[i] - mean) * (timeArr2[i] - mean);
            }
            sd = Math.sqrt(sum / 9.0);

            System.out.println("Min: " + min);
            System.out.println("Mean: " + mean);
            System.out.println("Max: " + max);
            System.out.println("Standard Deviation: " + sd);
            System.out.println("----------------------------------------------------");

            // terminate the connection
            System.out.println("See you later client! Closing connection");
            socket.close();
            input.close();
        }

        catch(IOException i)
        {
            System.out.println("Error in connection with client or input detection");
            throw new RuntimeException(i);
        }
    }

    // method which takes a file name and sends it from the server to the client requesting that file
    public static void sendFile(String filepath, DataOutputStream writeFile) throws IOException {
        File file = new File(filepath);
        FileInputStream readFile = new FileInputStream(file);
        writeFile.writeLong(file.length());
        byte[] buffer = new byte[8192];
        int bytesLeft = readFile.read(buffer);
        while (bytesLeft != -1){
            writeFile.write(buffer, 0, bytesLeft);
            writeFile.flush();
            bytesLeft = readFile.read(buffer);
        }
        readFile.close();
    }
}