import java.net.Socket;
import java.util.ArrayList;

public class peer {
  // contains all the member variables of a peer
  private int PeerId;
  private Socket socket;
  private boolean interest;
  private boolean[] bitfield;
  private Logger logger;

  tcp_client client;
  tcp_server server;
  PeerConnectionManager connectionManager;

  // constructor to launch a peer object from peerProcess
  peer (int PeerId, int peerPort, ArrayList<peerInfoParser.peerInfo> peerInfoVector, FileManager fileManager, int unchokingInterval, int optimisticUnchokingInterval, int numNeighbors, int lastPeer, int numPeers){
    System.out.println("Creating peer with peerID: " + PeerId);
    bitfield = fileManager.getBitfield();


    logger = new Logger(PeerId);

    this.connectionManager = new PeerConnectionManager(unchokingInterval, optimisticUnchokingInterval, numNeighbors, numPeers, logger);
    if (fileManager.hasAllPieces()){
      this.connectionManager.hasCompleteFile = true;
    }
    // Deploy the server-side
//    System.out.println("LastPeer: " + lastPeer);
    if (PeerId < lastPeer){ // don't deploy 1003 TODO: don't deploy server for last peer (if PeerID != lastPeer)
      server = new tcp_server(peerPort, PeerId, fileManager, connectionManager, logger);

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Calling shutdown hook");
        if (server != null) {
          server.stopServer();
          System.out.println("Server shutdown successfully.");
        }
      }));

      Thread serverThread = new Thread(() -> server.launchServer());
      serverThread.start();
    }

    // Deploy the client-side: attempt to connect to each of the other peers
    for (peerInfoParser.peerInfo p : peerInfoVector){
//      System.out.println(p.getPeerId());
//      System.out.println(PeerId);
      if (p.getPeerId() == PeerId){
        break; // only attempt to connect to peers that precede this one (which will have already been launched)
      }
      else { // skip the current peer
        int targetPeerPort = p.getPort();
        int targetPeerID = p.getPeerId();
        String targetPeerAddress = "localhost"; // hard-coded IP - change this
        client = new tcp_client(targetPeerPort, PeerId, targetPeerID, fileManager, connectionManager, logger);
        Thread clientThread = new Thread(() -> client.requestServer(targetPeerAddress, targetPeerPort));
        clientThread.start();
        System.out.println("Client: " + PeerId + " is connecting to peer " + p.getPeerId() + " at " + targetPeerAddress + ":" + targetPeerPort);
      }
    }
//    if (logger != null){
//      logger.shutdownLogger();
//    }
//    System.out.println("Exiting peer");
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
