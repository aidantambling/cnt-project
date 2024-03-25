package Peer;
import java.net.Socket;

public class peer {
  // contains all the member variables of a peer
  private int PeerId;
  private Socket socket;
  private boolean interest;
  private byte[] bitfield;

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
