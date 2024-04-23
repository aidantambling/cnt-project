package Peer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    public int numNeighbors;
    public boolean hasCompleteFile;

    public PeerConnectionManager(int unchokingInterval, int optimisticUnchokingInterval, int numNeighbors) {
        System.out.println("Initializing PCM");
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.timer = new Timer();
        this.numNeighbors = numNeighbors;
        this.hasCompleteFile = false;
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
            manager.connections.values().forEach(conn -> {
                if (conn.isChoked()) {
                    // Send choke message
                } else {
                    // Send unchoke message
                }
            });
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

        interestedPeers.stream().limit(numNeighbors).forEach(ConnectionInfo::unchoke);
        if (interestedPeers.size() > numNeighbors) {
            interestedPeers.subList(numNeighbors, interestedPeers.size()).forEach(ConnectionInfo::choke);
        }

        //TODO: handle optimistic choking
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
        }
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

        ConnectionInfo(int peerId, boolean isClient) {
            this.peerId = peerId;
            this.downloadedBytes = 0;
            this.lastTimeChecked = System.currentTimeMillis();
            this.isClient = isClient;
            this.isChoked = true;
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
            this.isChoked = true;
        }

        public void unchoke(){
            this.isChoked = false;
        }

        public boolean isInterested(){
            return isInterested;
        }

    }

}
