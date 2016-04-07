package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class PacketSender {
    private DatagramSocket socket;

    public PacketSender(DatagramSocket socket) {
        this.socket = socket;
    }


}
