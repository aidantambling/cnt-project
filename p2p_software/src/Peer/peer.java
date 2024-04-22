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
  private byte[] bitfield;

  tcp_client client;
  tcp_server server;

  // constructor to launch a peer object from peerProcess
  peer (int PeerId, int peerPort, ArrayList<peerInfoParser.peerInfo> peerInfoVector){
    System.out.println("Creating peer with peerID: " + PeerId);

    // Deploy the server-side
    System.out.println("Deploying the server-side...");
    server = new tcp_server(peerPort, PeerId);
    Thread serverThread = new Thread(() -> server.launchServer());
    serverThread.start();

    // Deploy the client-side: attempt to connect to each of the other peers
    System.out.println("Server-side deployed.");
    System.out.println("Deploying the client-side");
    for (peerInfoParser.peerInfo p : peerInfoVector){
      if (p.getPeerId() == PeerId){
        break; // only attempt to connect to peers that precede this one (which will have already been launched)
      }
      else { // skip the current peer
        int targetPeerPort = p.getPort(); // 6001
        String targetPeerAddress = "10.228.13.48";
        client = new tcp_client(targetPeerPort, PeerId);
        Thread clientThread = new Thread(() -> client.requestServer(targetPeerAddress, targetPeerPort));
        clientThread.start();
        System.out.println("Connecting to peer " + p.getPeerId() + " at " + targetPeerAddress + ":" + targetPeerPort);
      }
    }
    System.out.println("Client-side deployed.");
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

  public byte[] getBitField() {
    return bitfield;
  }
  
  public void setBitField(byte[] bitfield) {
    this.bitfield = bitfield;
  }

}
