package network;

import network.exceptions.BrokenPacketException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class PacketReceiver implements Runnable {
    private DatagramSocket dSock;
    private int buffersize;
    private IncomingPacketDSTReader reader;

    public PacketReceiver(DatagramSocket datagramSocket, int buffersize) {
        this.dSock = datagramSocket;
        this.buffersize = buffersize;
        reader = new IncomingPacketDSTReader();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[buffersize];
        while (!dSock.isClosed()) {
            DatagramPacket dPack = new DatagramPacket(buffer, buffersize);
            try {
                dSock.receive(dPack);
            } catch (IOException e) {
                System.out.println("Error recieving packet");
                e.printStackTrace();
            }
            try {
                reader.process(dPack);
            } catch (BrokenPacketException e) {
                System.out.println("Broken packet received, so I fumbled it.");
            }
        }
    }
}