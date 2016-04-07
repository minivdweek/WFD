package network;

import tui.Commander;

import java.io.IOException;
import java.net.*;

/**
 * Created by joris.vandijk on 05/04/16.
 */
public class Network implements Protocol{
    private DatagramSocket socket;
    private PacketSender sender;

    public static void main(String[] args) {
        Network net = new Network();
    }

    public Network() {
        try {
            socket = new DatagramSocket(Protocol.PORT);
        } catch (SocketException e) {
            System.out.println("Error setting up the socket");
            e.printStackTrace();
        }
        sender = new PacketSender(socket);
        (new Thread(new PacketReceiver(socket, 1024))).start();
        (new Thread(new Commander())).start();
    }
}
