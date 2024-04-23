package Peer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import FileManager.Logger;

//TODO: check if the other peer (otherPeerID) is the "right neighbor"
// choke and unchoke:
// the peer uploads only to preferred neighbors (at most k of them) and an optimistically unchoked one
// the preferred neighbors are determined every p seconds
// ,
// to do this, calculate downloading rate of each neighbors over the previous interval
// among neighbors that are interested, pick k neighbors that downloaded at highest rate (if 2x, randomly)
// send unchoke messages to those neighbors (expect requests) and choke the others
// ,
// HOWEVER: if peer has a complete file, it determines preferred neighbors randomly among the interested

//TODO: optimistic unchoking

//TODO: IMPLEMENT THE ABOVE via a ConnectionManager class that a Peer has an object of. The connectionManager
// oversees the peer as a whole, and can follow the algorithm above to determine peers etc. It can be passed
// down to both server(here) and client. and the server and client can update it with rates and can also monitor
// it to determine if any choking / unchoking happens

//TODO: this approach is to centralize the monitoring of a peer's connections. our classes tcp_client and
// tcp_server are split in a way that a peer has some connections on one, some on the other. so to keep state
// between them, we need this connectionManager. it is needed in a similar sense as fileManager is needed
// to update the bitfields consistently across tcp_server/tcp_client.

public class PeerConnectionManager {
    public ConcurrentHashMap<Integer, ConnectionInfo> connections = new ConcurrentHashMap<>();
    private Set<Integer> requestedPieces; // Tracks pieces that have been requested across all connections

    private Timer timer;
    public int unchokingInterval;
    public int optimisticUnchokingInterval;
    public int optimisticUnchokedPeerId;
    public int numNeighbors;
    public boolean hasCompleteFile;

    public Logger logger;

    public boolean disconnect = false;

    public PeerConnectionManager(int unchokingInterval, int optimisticUnchokingInterval, int numNeighbors, Logger logger) {
        System.out.println("Initializing PCM");
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.optimisticUnchokedPeerId = -1;
        this.timer = new Timer();
        this.numNeighbors = numNeighbors;
        this.hasCompleteFile = false;
        this.logger = logger;
        requestedPieces = ConcurrentHashMap.newKeySet();

        // schedule the recomputation task evey unchokingInterval seconds;
        this.timer.schedule(new ChokeUnchokeTask(this), 0, 1000L * unchokingInterval);
    }

    public synchronized int requestPiece(List<Integer> availablePieces) {
        Collections.shuffle(availablePieces); // randomize the order of available pieces
        for (Integer pieceIndex : availablePieces) { // we iterate thru them...
            if (!requestedPieces.contains(pieceIndex)) { // until we find one this peer has not requested.
                requestedPieces.add(pieceIndex);
                return pieceIndex;
            }
        }
        return -1;
    }

    public synchronized void pieceReceived(int pieceIndex) {
        requestedPieces.remove(pieceIndex);
    }

    class ChokeUnchokeTask extends TimerTask {
        private PeerConnectionManager manager;

        public ChokeUnchokeTask(PeerConnectionManager manager) {
            this.manager = manager;
        }

        public void run() {
            printConnections();
            manager.evaluatePeers();
            checkShutdown();
//            manager.connections.values().forEach(conn -> {
//                if (conn.isChoked()) {
//                    // Send choke message
//                } else {
//                    // Send unchoke message
//                }
//            });
        }
    }

    public void checkShutdown(){
        boolean allDownloaded = true;
        for (ConnectionInfo connection : connections.values()){
            if (!connection.fileComplete){
                allDownloaded = false;
                break;
            }
        }

        if (hasCompleteFile && allDownloaded && connections.values().size() > 1) { // this peer has the file, and so do the others
            // we should gracefully disconnect all peers now.
            System.out.println("All have been downloaded......");
            disconnect = true;
            this.timer.cancel();
            this.timer.purge();
        }
    }


    public void evaluatePeers() {
        //TODO: servers/clients need to let PCM know their download rate.
        // based on the rate, we re-evaluate the c/uc connections
        List<ConnectionInfo> interestedPeers = connections.values().stream()
                .filter(ConnectionInfo::isInterested)
                .collect(Collectors.toList());

        if (this.hasCompleteFile) {
            Collections.shuffle(interestedPeers);
        } else {
            interestedPeers.sort(Comparator.comparingLong(ConnectionInfo::getDownloadRate).reversed());
        }

        List<ConnectionInfo> preferredNeighbors = interestedPeers.stream()
                .limit(numNeighbors).toList();

        preferredNeighbors.forEach(ConnectionInfo::unchoke);

        if (interestedPeers.size() > numNeighbors) {
            interestedPeers.subList(numNeighbors, interestedPeers.size()).forEach(ConnectionInfo::choke);
        }

        int[] preferredIds = preferredNeighbors.stream()
                .mapToInt(ConnectionInfo::getPeerId)
                .toArray();
//        interestedPeers.stream().limit(numNeighbors).forEach(ConnectionInfo::unchoke);
//        if (interestedPeers.size() > numNeighbors) {
//            interestedPeers.subList(numNeighbors, interestedPeers.size()).forEach(ConnectionInfo::choke);
//        }

        logger.changedPrefferedNeighbors(preferredIds);

        //TODO: handle optimistic choking

//        logger.changedOptimisticNeighbor(xxxx);
        //Handle optimistic choking
//        optimisticallyUnchokeNeighbor();
    }

    public void optimisticallyUnchokeNeighbor() {
        // If there are interested peers, select a random one to optimistically unchoke
        List<Integer> interestedPeerIds = connections.values().stream()
                .filter(ConnectionInfo::isInterested)
                .map(conn -> conn.peerId)
                .collect(Collectors.toList());

        if (!interestedPeerIds.isEmpty()) {
            // Select a random peer to optimistically unchoke
            Random random = new Random();
            int index = random.nextInt(interestedPeerIds.size());
            optimisticUnchokedPeerId = interestedPeerIds.get(index);

            logger.changedOptimisticNeighbor(optimisticUnchokedPeerId);

            // Unchoke the selected peer
            ConnectionInfo optimisticPeer = connections.get(optimisticUnchokedPeerId);
            if (optimisticPeer != null) {
                optimisticPeer.unchoke();
                System.out.println("Optimistically unchoked peer: " + optimisticUnchokedPeerId);
            }
        } else {
            // No interested peers, reset optimistic unchoked peer
            optimisticUnchokedPeerId = -1;
        }
    }

    public void registerConnection(int peerId, boolean isClient) {
        connections.put(peerId, new ConnectionInfo(peerId, isClient));
    }

    public void peerInterested(int peerId, boolean isInterested){
        connections.get(peerId).isInterested = isInterested;
    }

    public void unregisterConnection(int peerId) {
        connections.remove(peerId);
    }

    public void updateChokeStatus() {
        connections.forEach((id, connInfo) -> {
            if (someConditionBasedOn(connInfo)) {
                System.out.println("Connection " + id + " is now " + (connInfo.isChoked ? "choked" : "unchoked"));
            }
        });
    }

    public void printConnections(){
        System.out.println("**********************************************************");
        System.out.println("This peer's connections: ");
        for (Integer id : connections.keySet()) {
            ConnectionInfo connection = connections.get(id);
            System.out.print("Peer ID: " + id + ", Connection is via " );
            if (connection.isClient){
                System.out.print(" client ");
            }
            else {
                System.out.print( " server ");
            }
            System.out.println("And that connection is: " + (connection.isChoked ? "choked" : "unchoked"));

            if (!connection.fileComplete){
                System.out.println("Incomplete");
            } else {
                System.out.println("Complete");
            }
        }
        System.out.println("**********************************************************");
    }

    private boolean someConditionBasedOn(ConnectionInfo connInfo) {
        return true;
    }

    // represents a "connection object" that a peer has (e.g., Peer1 has a ConnectionInfo for Peer2, Peer3, etc., and vice versa)
    public static class ConnectionInfo {
        int peerId;
        long downloadedBytes;
        long lastTimeChecked;
        boolean isClient;
        boolean isChoked;
        boolean isInterested;
        public boolean fileComplete;

        ConnectionInfo(int peerId, boolean isClient) {
            this.peerId = peerId;
            this.downloadedBytes = 0;
            this.lastTimeChecked = System.currentTimeMillis();
            this.isClient = isClient;
            this.isChoked = false;
            this.isInterested = false;
        }

        public void updateDownloadedBytes(long amt){
            this.downloadedBytes += amt;
        }

        public boolean isChoked() {
            return isChoked;
        }

        public long getDownloadRate(){
            return this.downloadedBytes;
        }

        public void choke(){
            this.isChoked = false;
        }

        public void unchoke(){
            this.isChoked = false;
        }

        public boolean isInterested(){
            return isInterested;
        }

        public int getPeerId() { return peerId; }

    }

}
