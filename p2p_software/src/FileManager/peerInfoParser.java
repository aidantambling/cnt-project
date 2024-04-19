package FileManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class peerInfoParser {
    private int PeerId;
    private String HostName;
    private int port;
    private boolean hasFile;

    //this function make a map of variables to values
    private void parseConfigFile(String filePath) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length != 4) {
                    throw new IOException("Invalid config line: " + line);
                }

                this.PeerId = Integer.valueOf(parts[0].trim());
                this.HostName = parts[1].trim();
                this.port = Integer.valueOf(parts[2]);
                this.hasFile = Boolean.valueOf(parts[3]);
            }
        }

    }

    //for now file path is hardcoded this must be changed later
    public void readFile() throws IOException {
        String filePath = "../Configs/small/peerInfo.cfg";
        parseConfigFile(filePath);
    }

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
