public class peer {
    public tcp_server server;
    public tcp_client client;

    public peer (int port, int id){
        server = new tcp_server(port, id);
        client = new tcp_client(port, id);
    }


}
