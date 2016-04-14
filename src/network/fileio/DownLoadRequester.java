package network.fileio;

import network.OwnAddressFinder;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by joris.vandijk on 14/04/16.
 */
public class DownLoadRequester implements  FileIO {
    private String filename;
    private int target;

    public DownLoadRequester(String input, int target) {
        this.filename = input;
        this.target = target;
    }

    public void send() {
        sendPacket(buildPacket());

    }

    public Packet buildPacket(){
        Packet packet = new Packet(GET, filename.getBytes());
        packet.setDst(target);
        packet.setSrc(OwnAddressFinder.getOwnAddress());
        packet.setFlags(FILE);
        return packet;

    }

    public void sendPacket(Packet packet) {
        try {
            DatagramSocket sock = new DatagramSocket();
            sock.send(packet.toDatagramPacket());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
