package Peer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.TimerTask;
import java.util.Timer;

public class PeerConnectionManager {
    private ConcurrentHashMap<Integer, ConnectionInfo> connections = new ConcurrentHashMap<>();
    private Timer timer;
    public int unchokingInterval;
    public int optimisticUnchokingInterval;
    public int numNeighbors;

    public PeerConnectionManager(int unchokingInterval, int optimisticUnchokingInterval, int numNeighbors) {
        System.out.println("Initializing PCM");
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.timer = new Timer();
//        this.timer.schedule(new ChokeUnchokeTask(this), 0, 1000L * unchokingInterval); // Assuming 'p' is the interval in seconds
    }

    public void registerConnection(int peerId, boolean isClient) {
        connections.put(peerId, new ConnectionInfo(peerId, isClient));
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
        }
    }

    private boolean someConditionBasedOn(ConnectionInfo connInfo) {
        return true;
    }

    static class ConnectionInfo {
        int peerId;
        boolean isClient;
        boolean isChoked;

        ConnectionInfo(int peerId, boolean isClient) {
            this.peerId = peerId;
            this.isClient = isClient;
            this.isChoked = true;
        }
    }

}
