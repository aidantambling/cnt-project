# Target for all components
all: TCPClient TCPServer Peer PeerMessages ConfigParser PeerInfoParser FileManager

# Compile the TCP client
TCPClient: TCP_Client.class
TCP_Client.class: tcp_client.java
	javac --release 11 tcp_client.java

# Compile the TCP server
TCPServer: TCP_Server.class
TCP_Server.class: tcp_server.java
	javac --release 11 tcp_server.java

# Compile the Peer components including FileManager and PeerConnectionManager
Peer: Peer.class PeerProcess.class FileManager.class PeerConnectionManager.class

Peer.class: peer.java
	javac --release 11 peer.java

PeerProcess.class: PeerProcess.java
	javac --release 11 PeerProcess.java

# Compile the FileManager
FileManager.class: FileManager.java
	javac --release 11 FileManager.java

# Compile the PeerConnectionManager
PeerConnectionManager.class: PeerConnectionManager.java
	javac --release 11 PeerConnectionManager.java

# Compile the Peer Messages
PeerMessages: Bitfield.class Choke.class Have.class Interested.class NotInterested.class Message.class Piece.class Request.class Unchoke.class
%.class: %.java
	javac --release 11 $<

# Compile ConfigParser
ConfigParser: configParser.class
configParser.class: configParser.java
	javac --release 11 configParser.java

# Compile PeerInfoParser
PeerInfoParser: peerInfoParser.class
peerInfoParser.class: peerInfoParser.java
	javac --release 11 peerInfoParser.java

# Clean the build
clean:
	rm -f *.class
