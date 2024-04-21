package Peer;
import tcpProcess.tcp_client;
import tcpProcess.tcp_server;

import java.net.Socket;

public class peer {
  // contains all the member variables of a peer
  private int PeerId;
  private Socket socket;
  private boolean interest;
  private byte[] bitfield;

  tcp_client client;
  tcp_server server;

  // constructor to launch a peer object from peerProcess
  peer (int PeerId){
    System.out.println("Creating peer with peerID: " + PeerId);
    server = new tcp_server(8000, PeerId);
    client = new tcp_client(8000, PeerId);
    Thread serverThread = new Thread(() -> server.launchServer());
    serverThread.start();
//    server.launchServer();
    System.out.println("Hi");
//    client.requestServer("");
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
