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

    private int myId;
    private static int numPieces = 0;
    private static File logFile;
    private BufferedWriter log;

    public Logger(int peerId){
        myId = peerId;
        String logFileName = new File(System.getProperty("user.dir")) + "/log_peer_" + myId + ".log";
        logFile = new File(logFileName);

        try {
            log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

//    public static void initializeLogger (int peerId) {
//
//        String logFileName = new File(System.getProperty("user.dir")).getParent() + "/log_peer_" + myId + ".log";
//        logFile = new File(logFileName);
//
//        try {
//            log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)));
//        } catch (Exception e) {
//            System.err.println(e);
//        }
//    }

    // messages for tcp connections
    // client
    public void makesTCPConnection (int peerId) {
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

    // server
    public void isConnected (int peerId) {
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

    public void changedPrefferedNeighbors (int[] peerId) {
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

    public void changedOptimisticNeighbor (int peerId) {
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

    public void receivedUnchoked (int peerId) {
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

    public void receivedChoked (int peerId) {
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

    public void receivedHave (int peerId, int pieceIndex) {
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

    public void receivedInterested (int peerId) {
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

    public void receivedNotInterested (int peerId) {
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

    public void hasDownloaded (int peerId, int pieceIndex) {
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

    public void downloadComplete () {
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

    public void shutdownLogger () {
        try {
            log.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
     
}
