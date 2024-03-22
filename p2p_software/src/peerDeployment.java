public class peerDeployment {
    public static void main (String [] args){

        // Sample code which launches the server in one thread and the corresponding client in another thread

        // server messages: choke, unchoke, *piece*
        // client messages: interested, not interested, *request*
        // ???: have, bitfield

        // 5 different files - one for each peer? i.e. peerDeployment1, peerDeployment2...

        peer peer1 = new peer(1664, 1);
        // Launch server in a new thread
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                peer1.server.launchServer();
                peer1.server.communicate();
            }
        });
        serverThread.start();

        peer peer2 = new peer(2000, 2);
        // Wait for a second before connecting
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Connect to the server
        peer2.client.requestServer("192.168.56.1");
        peer2.client.sendCommunication();


    }
}
