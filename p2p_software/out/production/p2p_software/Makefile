# Define a Makefile for compiling Java TCP client, server, peer components, and parsers

# Target for all components
all: TCPClient TCPServer Peer PeerMessages ConfigParser PeerInfoParser

# Compile the TCP client
TCPClient: tcpProcess/TCP_Client.class

tcpProcess/TCP_Client.class: tcpProcess/TCP_Client.java
	javac tcpProcess/TCP_Client.java

# Compile the TCP server
TCPServer: tcpProcess/TCP_Server.class

tcpProcess/TCP_Server.class: tcpProcess/TCP_Server.java
	javac tcpProcess/TCP_Server.java

# Compile the Peer components
Peer: Peer/Peer.class Peer/PeerProcess.class

Peer/Peer.class: Peer/Peer.java
	javac Peer/Peer.java

Peer/PeerProcess.class: Peer/PeerProcess.java
	javac Peer/PeerProcess.java

# Compile the Peer Messages
PeerMessages: Peer/Messages/Bitfield.class Peer/Messages/Choke.class Peer/Messages/Have.class Peer/Messages/Interested.class Peer/Messages/NotInterested.class Peer/Messages/Message.class Peer/Messages/Piece.class Peer/Messages/Request.class Peer/Messages/Unchoke.class

Peer/Messages/%.class: Peer/Messages/%.java
	javac $<

# Compile ConfigParser
ConfigParser: FileManager/configParser.class

FileManager/configParser.class: FileManager/configParser.java
	javac FileManager/configParser.java

# Compile PeerInfoParser
PeerInfoParser: FileManager/peerInfoParser.class

FileManager/peerInfoParser.class: FileManager/peerInfoParser.java
	javac FileManager/peerInfoParser.java

# Clean the build
clean:
	rm -f tcpProcess/*.class Peer/*.class Peer/Messages/*.class FileManager/*.class

