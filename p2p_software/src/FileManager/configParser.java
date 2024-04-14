package FileManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class configParser {
    private int NumberOfPreferredNeighbors;
    private int UnchokingInterval;
    private int OptimisticUnchokingInterval;
    private String FileName;
    private int FileSize;
    private int PieceSize;
    private static Map<String, String> configMap;
    
    //this function make a map of variables to values
    private static Map<String, String> parseConfigFile(String filePath) throws IOException {
        configMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    throw new IOException("Invalid config line: " + line);
                }

                String key = parts[0].trim();
                String value = parts[1].trim();
                configMap.put(key, value);
            }
        }

        return configMap;
    }

    public void readFile() throws IOException {
        String filePath = "../Configs/small/common.cfg";
        configMap = parseConfigFile(filePath);
    }

    public int getNumberOfPrefferedNeighbors() {
        this.NumberOfPreferredNeighbors = Integer.valueOf(configMap.get("NumberOfPreferredNeighbors"));
        return NumberOfPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        this.UnchokingInterval = Integer.valueOf(configMap.get("UnchokingInterval"));
        return UnchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        this.OptimisticUnchokingInterval = Integer.valueOf(configMap.get("OptimisticUnchokingInterval"));
        return OptimisticUnchokingInterval;
    }

    public String getFileName() {
        this.FileName = (configMap.get("FileName"));
        return FileName;
    }

    public long getFileSize() {
        this.FileSize = Integer.valueOf(configMap.get("FileSize"));
        return FileSize;
    }

    public long getPieceSize() {
        this.PieceSize = Integer.valueOf(configMap.get("PieceSize"));
        return PieceSize;
    }
    
}
