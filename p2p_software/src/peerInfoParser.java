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


    private void parseConfigFile(String filePath) throws IOException {
        System.out.println("Reading PeerInfo.cfg");

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

//                System.out.println("PeerID: " + peerInfo.PeerId);
//                System.out.println("Peer Hostname: " + peerInfo.HostName);
//                System.out.println("Peer Port: " + peerInfo.port);
//                System.out.println("HasFile: " + peerInfo.hasFile);

                peerInfoVector.add(peerInfo);
            }
        }

    }

    //for now file path is hardcoded this must be changed later
    public void readFile() throws IOException {
        String filePath = System.getProperty("user.dir") + File.separator + "PeerInfo.cfg";
        parseConfigFile(filePath);
    }
}
