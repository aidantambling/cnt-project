# Define a makefile for compiling Java TCP client and server

# Target for both client and server
all: TCP_Client.class TCP_Server.class

# Compile the TCP client
TCP_Client.class: TCP_Client.java
	javac TCP_Client.java

# Compile the TCP server
TCP_Server.class: TCP_Server.java
	javac TCP_Server.java

# Clean the build
clean:
	rm -f *.class
