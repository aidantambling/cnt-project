import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
public class tcp_client {

        public static void main(String args[])
        {
            long B1 = System.currentTimeMillis(); // first measurement of TCP setup time

            // hard coded port - last 4 digits of UFID
            int port = 1664;

            // address is passed in as an argument from command line
            String address;
            try {
                address = args[0];
            } catch (Exception e){
                System.out.println("Error: no hostname provided!");
                throw new RuntimeException(e);
            }

            // resolve an IP address from the passed hostname
            InetAddress IP;
            try {
                IP = InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                System.out.println("Error in resolving IP address!");
                throw new RuntimeException(e);
            }

            // use the address and port to create a client socket
            BufferedReader input;
            DataOutputStream output;
            Socket socket;
            try {
                socket = new Socket(IP, port);
                System.out.println("Client-side socket established.");
                System.out.println("Hostname: " + address);
                System.out.println("Address: " + IP.getHostAddress());
                System.out.println("Port: " + port);

                // input is taken in from the console
                input = new BufferedReader(new InputStreamReader(System.in));

                // output from the socket is sent to the server socket for reading
                output = new DataOutputStream(socket.getOutputStream());
            }
            catch (Exception e){
                System.out.println("Error in establishing a socket connection!");
                throw new RuntimeException(e);
            }

            long B2 = System.currentTimeMillis(); // second measurement of TCP setup time
            long B = B2 - B1; // measures the duration of the TCP setup time

            // randomly generate the order of the memes
            Random rand = new Random();
            ArrayList<String> vector = new ArrayList<>();
            while (vector.size() != 10){
                int randomNum = rand.nextInt(10) + 1;
                String str = "Meme " + randomNum;
                if (!vector.contains(str)){
                    vector.add(str);
                }
            }
            vector.add("bye"); // last element will terminate the connection

            long[] timeArr1 = new long[10];

            // iterate through each randomly selected meme
            String consoleInput = "";
            for (int i = 0; i < 10; i++) {
                try {
                    long A1 = System.currentTimeMillis(); // first measurement of round-trip time to retrieve each meme
                    consoleInput = vector.get(i);
                    output.writeUTF(consoleInput);

                    // select the appropriate filename based on the meme we are at
                    FileOutputStream writeFile = null;
                    if (consoleInput.equals("Meme 1")){
                        writeFile = new FileOutputStream("meme1.jpg");
                    }
                    else if (consoleInput.equals("Meme 2")){
                        writeFile = new FileOutputStream("meme2.jpeg");
                    }
                    else if (consoleInput.equals("Meme 3")){
                        writeFile = new FileOutputStream("meme3.jpg");
                    }
                    else if (consoleInput.equals("Meme 4")){
                        writeFile = new FileOutputStream("meme4.jpeg");
                    }
                    else if (consoleInput.equals("Meme 5")){
                        writeFile = new FileOutputStream("meme5.jpg");
                    }
                    else if (consoleInput.equals("Meme 6")){
                        writeFile = new FileOutputStream("meme6.jpg");
                    }
                    else if (consoleInput.equals("Meme 7")){
                        writeFile = new FileOutputStream("meme7.jpg");
                    }
                    else if (consoleInput.equals("Meme 8")){
                        writeFile = new FileOutputStream("meme8.jpg");
                    }
                    else if (consoleInput.equals("Meme 9")){
                        writeFile = new FileOutputStream("meme9.png");
                    }
                    else if (consoleInput.equals("Meme 10")){
                        writeFile = new FileOutputStream("meme10.jpg");
                    }

                    if (consoleInput.equals("bye") || writeFile == null){
                        break;
                    }

                    // read in the image from the server and store it in a local file
                    DataInputStream fileInput = new DataInputStream(socket.getInputStream());
                    long fileSize = fileInput.readLong();
                    byte[] buffer = new byte[8192];
                    int bytesRead = fileInput.read(buffer, 0, buffer.length);
                    while (bytesRead != -1){
                        writeFile.write(buffer, 0 ,bytesRead);
                        fileSize -= bytesRead;
                        if (fileSize <= 0){
                            break;
                        }
                        bytesRead = fileInput.read(buffer, 0, buffer.length);
                    }
                    System.out.println(consoleInput + " has been received");
                    long A2 = System.currentTimeMillis(); // second measurement of round-trip time to retrieve each meme
                    timeArr1[i] = A2-A1; // track time to retrieve joke from server
                    writeFile.close();
                }
                catch (IOException e) {
                    System.out.println("Error in reading from the console!");
                    throw new RuntimeException(e);
                }
            }

            // time calculations and displaying of values
            System.out.println("----------------------------------------------------");
            System.out.println("Round-trip time to retrieve each of 10 memes (ms): ");
            for (int i = 0; i < 10; i++){
                System.out.println(timeArr1[i]);
            }
            long min = timeArr1[0];
            for (int i = 0; i < 10; i++){
                if (timeArr1[i] < min){
                    min = timeArr1[i];
                }
            }

            long max = timeArr1[0];
            for (int i = 0; i < 10; i++){
                if (timeArr1[i] > max){
                    max = timeArr1[i];
                }
            }

            long sum = 0;
            double mean = 0;
            for (int i = 0; i < 10; i++){
                sum += timeArr1[i];
            }
            mean = sum / 10.0;

            double sd = 0;
            sum = 0;
            for (int i = 0; i < 10; i++){
                sum += (timeArr1[i] - mean) * (timeArr1[i] - mean);
            }
            sd = Math.sqrt(sum / 9.0);

            System.out.println("Min: " + min);
            System.out.println("Mean: " + mean);
            System.out.println("Max: " + max);
            System.out.println("Standard Deviation: " + sd);
            System.out.println("----------------------------------------------------");

            System.out.println("TCP Setup Time (ms): " + B);
            System.out.println("----------------------------------------------------");

            // terminate the connection
            System.out.println("Goodbye server! Closing connection.");
            try {
                input.close();
                output.close();
                socket.close();
            }
            catch (IOException e) {
                System.out.println("Error in disconnecting the client-server interface!");
                throw new RuntimeException(e);
            }
        }
}
