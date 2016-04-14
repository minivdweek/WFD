package network.processor;

import network.exceptions.BrokenPacketException;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class Resender implements Processor {
    private DatagramPacket packet;

    public Resender(DatagramPacket packet) {
        this.packet = packet;
    }


    @Override
    public void processPacket() {
        int port = packet.getPort();
        try {
            DatagramSocket sock = new DatagramSocket(port);
            sock.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
