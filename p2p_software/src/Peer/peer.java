package Peer;
import FileManager.peerInfoParser;
import tcpProcess.tcp_client;
import tcpProcess.tcp_server;

import java.net.Socket;
import java.util.ArrayList;

public class peer {
  // contains all the member variables of a peer
  private int PeerId;
  private Socket socket;
  private boolean interest;
  private boolean[] bitfield;

  tcp_client client;
  tcp_server server;
  PeerConnectionManager connectionManager;

  // constructor to launch a peer object from peerProcess
  peer (int PeerId, int peerPort, ArrayList<peerInfoParser.peerInfo> peerInfoVector, FileManager fileManager, int unchokingInterval, int optimisticUnchokingInterval, int numNeighbors){
    System.out.println("Creating peer with peerID: " + PeerId);
    bitfield = fileManager.getBitfield();
    System.out.println("Bitfield for peer " + PeerId);
//    for (boolean b : bitfield){
//      System.out.print(b);
//    }

    this.connectionManager = new PeerConnectionManager(unchokingInterval, optimisticUnchokingInterval, numNeighbors);
    if (fileManager.hasAllPieces()){
      this.connectionManager.hasCompleteFile = true;
    }

    // Deploy the server-side
    server = new tcp_server(peerPort, PeerId, fileManager, connectionManager);
    Thread serverThread = new Thread(() -> server.launchServer());
    serverThread.start();

    // Deploy the client-side: attempt to connect to each of the other peers
    for (peerInfoParser.peerInfo p : peerInfoVector){
      if (p.getPeerId() == PeerId){
        break; // only attempt to connect to peers that precede this one (which will have already been launched)
      }
      else { // skip the current peer
        int targetPeerPort = p.getPort();
        String targetPeerAddress = "localhost"; // hard-coded IP - change this
        client = new tcp_client(targetPeerPort, PeerId, fileManager, connectionManager);
        Thread clientThread = new Thread(() -> client.requestServer(targetPeerAddress, targetPeerPort));
        clientThread.start();
        System.out.println("Client: " + PeerId + " is connecting to peer " + p.getPeerId() + " at " + targetPeerAddress + ":" + targetPeerPort);
      }
    }
  }

  public int getPeerId() {
    return PeerId;
  }

  public void setPeerID(int PeerId) {
     this.PeerId = PeerId;
  }

  public Socket getSocket(){
    return socket;
  }

  public void setSocket(Socket socket){
    this.socket = socket;
  }

  public boolean isInterested() {
		return interest;
	}

	public void setInterest(boolean interest) {
		this.interest = interest;
	}

  public boolean[] getBitField() {
    return bitfield;
  }
  
  public void setBitField(boolean[] bitfield) {
    this.bitfield = bitfield;
  }

}
