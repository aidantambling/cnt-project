# Target for all components
all: TCPClient TCPServer Peer PeerMessages ConfigParser PeerInfoParser FileManager

# Compile the TCP client
TCPClient: tcpProcess/TCP_Client.class
tcpProcess/TCP_Client.class: tcpProcess/TCP_Client.java
	javac --release 11 tcpProcess/TCP_Client.java

# Compile the TCP server
TCPServer: tcpProcess/TCP_Server.class
tcpProcess/TCP_Server.class: tcpProcess/TCP_Server.java
	javac --release 11 tcpProcess/TCP_Server.java

# Compile the Peer components including FileManager and PeerConnectionManager
Peer: Peer/Peer.class Peer/PeerProcess.class Peer/FileManager.class Peer/PeerConnectionManager.class

Peer/Peer.class: Peer/Peer.java
	javac --release 11 Peer/Peer.java

Peer/PeerProcess.class: Peer/PeerProcess.java
	javac --release 11 Peer/PeerProcess.java

# Compile the FileManager within the Peer directory
Peer/FileManager.class: Peer/FileManager.java
	javac --release 11 Peer/FileManager.java

# Compile the PeerConnectionManager within the Peer directory
Peer/PeerConnectionManager.class: Peer/PeerConnectionManager.java
	javac --release 11 Peer/PeerConnectionManager.java

# Compile the Peer Messages
PeerMessages: Peer/Messages/Bitfield.class Peer/Messages/Choke.class Peer/Messages/Have.class Peer/Messages/Interested.class Peer/Messages/NotInterested.class Peer/Messages/Message.class Peer/Messages/Piece.class Peer/Messages/Request.class Peer/Messages/Unchoke.class
Peer/Messages/%.class: Peer/Messages/%.java
	javac --release 11 $<

# Compile ConfigParser
ConfigParser: FileManager/configParser.class
FileManager/configParser.class: FileManager/configParser.java
	javac --release 11 FileManager/configParser.java

# Compile PeerInfoParser
PeerInfoParser: FileManager/peerInfoParser.class
FileManager/peerInfoParser.class: FileManager/peerInfoParser.java
	javac --release 11 FileManager/peerInfoParser.java

# Clean the build
clean:
	rm -f tcpProcess/*.class Peer/*.class Peer/Messages/*.class FileManager/*.class
