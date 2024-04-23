package FileManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;


public class Logger {
    /*logger will log all the messages of a peer user. 
     * its functions will each represent a type of message to be logged,
     * predetermined in the project description.
     */

    private static int myId;
    private static int numPieces = 0;
    private static File logFile;
    private static BufferedWriter log;

    public static void initializeLogger (int peerId) {
        myId = peerId;

        String logFileName = new File(System.getProperty("user.dir")).getParent() + "/log_peer_" + myId + ".log";
        logFile = new File(logFileName);

        try {
            log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    // messages for tcp connections
    public static void makesTCPConnection (int peerId) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " makes a connection to Peer " + peerId + ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public static void isConnected (int peerId) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " is connected from Peer " + peerId + ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // message for change of preffered neighbors

    public static void changedPrefferedNeighbors (int[] peerId) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " has the preferred neighbors ";

        for (int i : peerId) {
            line += i + ", ";
        } 
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // message for change of optimistically unchoked neighbor

    public static void changedOptimisticNeighbor (int peerId) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " has the optimistically unchoked neighbor " + peerId + ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // message for unchoking

    public static void receivedUnchoked (int peerId) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " is unchoked by " + peerId + ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }


    // message for choking

    public static void receivedChoked (int peerId) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " is choked by " + peerId + ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // receing a have message

    public static void receivedHave (int peerId, int pieceIndex) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " received the 'have' message from " + peerId + " for the piece " + pieceIndex + ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // receiving an interested message

    public static void receivedInterested (int peerId) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " received the 'interested' message from " + peerId +  ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // receiving a not interested message

    public static void receivedNotInterested (int peerId) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " received the 'not interested' message from " + peerId +  ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // downloading a piece message

    public static void hasDownloaded (int peerId, int pieceIndex) {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " has downloaded the piece " + pieceIndex +  " from " + peerId + ".";
        numPieces++;
        line += " Now the number of pieces it has is " + numPieces + ".";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    // download complete message

    public static void downloadComplete () {
        String date = new Date().toString();
        String line = date + ": Peer " + myId + " has downloaded the complete file.";
        
        try {
            log.append(line);
            log.newLine();
            log.flush();
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public static void shutdownLogger () {
        try {
            log.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
     
}
