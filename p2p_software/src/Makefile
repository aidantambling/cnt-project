# Target for all components
all: TCPClient TCPServer Peer PeerMessages ConfigParser PeerInfoParser FileManager

# Compile the TCP client
TCPClient: TCP_Client.class
TCP_Client.class: tcp_client.java
	javac tcp_client.java

# Compile the TCP server
TCPServer: TCP_Server.class
TCP_Server.class: tcp_server.java
	javac tcp_server.java

# Compile the Peer components including FileManager and PeerConnectionManager
Peer: Peer.class PeerProcess.class FileManager.class PeerConnectionManager.class

Peer.class: peer.java
	javac peer.java

PeerProcess.class: PeerProcess.java
	javac PeerProcess.java

# Compile the FileManager
FileManager.class: FileManager.java
	javac FileManager.java

# Compile the PeerConnectionManager
PeerConnectionManager.class: PeerConnectionManager.java
	javac PeerConnectionManager.java

# Compile the Peer Messages
PeerMessages: Bitfield.class Choke.class Have.class Interested.class NotInterested.class Message.class Piece.class Request.class Unchoke.class
%.class: %.java
	javac $<

# Compile ConfigParser
ConfigParser: configParser.class
configParser.class: configParser.java
	javac configParser.java

# Compile PeerInfoParser
PeerInfoParser: peerInfoParser.class
peerInfoParser.class: peerInfoParser.java
	javac peerInfoParser.java

# Clean the build
clean:
	rm -f *.class
