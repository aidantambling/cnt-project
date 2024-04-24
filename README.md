# P2P File Sharing Software Project 
by Aidan Tambling, Fredderick Blanco, Gabriel Hunter Turmail --
coded in Java

This project involves building a Peer-to-Peer (P2P) file sharing application similar to BitTorrent.  

# Individual Contributions

- Aidan Tambling: peerProcesses, peer data structure, communication between threads
- Fredderick Blanco: Logging, peer data structure, configuration parsing
- Gabriel Hunter Turmail: Optimistic unchoking, message structures

# How to Run the Project

(Note: PeerInfo.cfg and Config.cfg must be supplied. The amount of peers in PeerInfo.cfg is the amount that must, together, have the file for the program to terminate. Also, a directory must be provided inside of src for each peer (e.g. peer_1001, peer_1002), etc. and if PeerInfo.cfg specifies that a given peer has a file, that peer directory must contain the file.)
1. Ensure you have at least Java version 11 installed
2. Navigate terminal to p2p_software/src
3. Run make to compile the program
4. Start a peer from the src directory using "java PeerProcess #" where # is replaced by the peerID

# Details About Implemented Protocols

PeerProcess.java creates a "Peer" object which then spawns threads with event loops to interact with other deployed peer objects. The peer.java class is where most of the project is implemented. Peer.java has two objects: tcp_server and tcp_client.
- tcp_client: interacts with peers preceding this one
- tcp_server: interacts with peers following this one
The initial peer does not deploy a client, and the final peer does not deploy a server.

A challenge of implementing this structure is that a peer manages some connections through its tcp_client object, and others through its tcp_server. This makes state management (e.g. bitfield, choking/unchoking) challenging, because the peer must act cohesively across these two objects
- The tcp_server and tcp_client each have an event loop in them, where thread connections are maintained
- To maintain state across tcp_client and tcp_server, FileManager.java and PeerConnectionManager.java are used. The fields and functions in these classes allow a peer to act in cohesion across the tcp_client and tcp_server
- In retrospect, this was a questionable implementation decision. The challenges of maintaining state across multiple threads like this would have been far simpler without tcp_client or tcp_server (i.e., implementing the threading and TCP communication in peer.java).
- Also, there is a good amount of redundancy between tcp_client and tcp_server.
- Nevertheless, the decision was made and it was decided to not revise the structure once the program depended on it too heavily. The program works as intended, it just could have been implemented with more foresight.

## tcp_client functions
| function                 | explanation                                           |
|--------------------------|-------------------------------------------------------|
| requestServer()          | connect this client object to a given server          |
| sendHandshake()          | send the server a handshake                           |
| readHandshake()          | receive the server's handshake                        |
| sendBitfield()           | send the server the bitfield                          |
| receiveBitfield()        | receive the server's bitfield                         |
| booleanArrayToBytes()    | helper function for encoding bitfield                 |
| sendInterested()         | send the server an interested message                 |
| sendNotInterested()      | send the server a not interested message              |
| sendHaveMessage()        | send the server the index of a newly-received byte    |
| readHaveMessage()        | read the server's index of newly-received byte        |
| sendChokeMessage()       | choke the server                                      |
| sendUnchokeMessage()     | unchoke the server                                    |
| handleIncomingRequests() | respond to a server's request                         |
| sendPiece()              | send an individual piece of data to the server        |
| maintainConnection()     | operate a TCP connection with the server (event loop) |
| closeClient()            | terminate the connection                              |

## tcp_server functions

| function                 | explanation                                           |
|--------------------------|-------------------------------------------------------|
| launchServer()           | await connection from a client                        |
| sendHandshake()          | send the client a handshake                           |
| readHandshake()          | receive the client's handshake                        |
| sendBitfield()           | send the client the bitfield                          |
| receiveBitfield()        | receive the client's bitfield                         |
| booleanArrayToBytes()    | helper function for encoding bitfield                 |
| sendInterested()         | send the client an interested message                 |
| sendNotInterested()      | send the client a not interested message              |
| sendHaveMessage()        | send the client the index of a newly-received byte    |
| readHaveMessage()        | read the client's index of newly-received byte        |
| sendChokeMessage()       | choke the client                                      |
| sendUnchokeMessage()     | unchoke the client                                    |
| handleIncomingRequests() | respond to a client's request                         |
| sendPiece()              | send an individual piece of data to the client        |
| run()                    | operate a TCP connection with the client (event loop) |
| stopServer()             | terminate the connection                              |

## PeerConnectionManager functions
| function                        | explanation                                                           |
|---------------------------------|-----------------------------------------------------------------------|
| requestPiece()                  | track a peer's requested pieces so it doesn't request a duplicate     |
| run()                           | use a timer to run periodic checks on the peer                        |
| checkShutdown()                 | determine if peers should disconnect (has everyone received the file) |
| evaluatePeers()                 | re-evaluate choked/unchoked peers                                     |
| optimisticallyUnchokeNeighbor() | perform check on optimistically unchoking neighbor                    |
| registerConnection()            | add a new connection for the manager to track                         |
| peerInterested()                | indicate if a peer is interested in this one                          |

## FileManager functions
| function             | explanation                                                      |
|----------------------|------------------------------------------------------------------|
| writeToFile()        | when a peer has received all bytes, use them to create a file    |
| loadFile()           | read in a file's bytes to this peer (if it begins with the file) |
| hasPiece()           | check if the peer has the piece (across any thread)              |
| getPiece()           | return a piece of the file                                       |
| calculateNumPieces() | determine the number of fragments the file was split into        |
| storePiece()         | register that the peer has received a new piece                  |
| getBitfield()        | return bitfield                                                  |
| hasAllPieces()       | check if the peer has the entire file                            |

# Project Goal:

The primary objective of this project is to implement a core functionality of BitTorrent, the choking-unchoking mechanism. This mechanism plays a crucial role in optimizing resource allocation within the P2P network.

# Implementation:

Peers use client and server objects with TCP connections.
The process PeerProcess, implements the choking functionalities. 
