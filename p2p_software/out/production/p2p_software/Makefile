# Define a Makefile for compiling Java TCP client, server, and peer components

# Target for all components
all: TCPClient TCPServer Peer PeerMessages

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
PeerMessages: $(addprefix Peer/Messages/, bitfield.class choke.class have.class interested.class notInterested.class piece.class request.class unchoke.class)

Peer/Messages/%.class: Peer/Messages/%.java
	javac $<


# Clean the build
clean:
	rm -f tcpProcess/*.class Peer/*.class Peer/Messages/*.class
