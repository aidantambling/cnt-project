package FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class peerInfoParser {

    public static class peerInfo {
        public int PeerId;
        public String HostName;
        public int port;
        public boolean hasFile;

        public int getPeerId () {
            return PeerId;
        }

        public String getHostName () {
            return HostName;
        }

        public int getPort () {
            return port;
        }

        public boolean hasCompleteFile () {
            return hasFile;
        }
    }

    public static ArrayList<peerInfo> peerInfoVector = new ArrayList<peerInfo>();


    //this function make a map of variables to values
    private void parseConfigFile(String filePath) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length != 4) {
                    throw new IOException("Invalid config line: " + line);
                }

                peerInfo peerInfo = new peerInfo();
                peerInfo.PeerId = Integer.parseInt(parts[0].trim());
                peerInfo.HostName = parts[1].trim();
                peerInfo.port = Integer.parseInt(parts[2]);
//                System.out.println(parts[3] + " " + Boolean.parseBoolean(parts[3]));
                peerInfo.hasFile = "1".equals(parts[3]);

//                System.out.println(peerInfo.PeerId + " " + peerInfo.HostName + " " + peerInfo.port + " " + peerInfo.hasFile);

                peerInfoVector.add(peerInfo);
            }
        }

    }

    //for now file path is hardcoded this must be changed later
    public void readFile() throws IOException {
        String filePath = System.getProperty("user.dir") + File.separator + "configs" + File.separator + "PeerInfo.cfg";
        parseConfigFile(filePath);
    }
}
