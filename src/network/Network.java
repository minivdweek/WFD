package network;

import tui.Commander;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by joris.vandijk on 05/04/16.
 */
public class Network implements Protocol{


    public static void main(String[] args) {
        Network net = new Network();
    }

    public Network() {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(Protocol.PORT);
            (new Thread(new PacketReceiver(socket, 1024))).start();
            (new Thread(new Commander())).start();
        } catch (SocketException e) {
            System.out.println("Error setting up the socket");
            e.printStackTrace();
        }


    }
}
