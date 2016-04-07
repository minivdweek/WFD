package network;

import network.processor.Processor;
import network.processor.Resender;
import packet.Packet;

import java.net.DatagramPacket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class IncomingPacketDSTReader {

    public void process(DatagramPacket datagramPacket) {
        processPacket(new Packet(datagramPacket));
    }

    public void processPacket(Packet packet) {
        Processor processor;
        if (isBroadcast(packet) || iAmDestination(packet)) {
            //TODO implement handling of packet meant for this host
            processor = null;
        } else {
            //TODO implement handling of packet not meant for this host
            processor = new Resender(packet);
        }
        processor.processPacket();
    }

    private boolean iAmDestination(Packet packet) {
        int ownDst = 14; //TODO figure out how to figure this out automatically.
        return packet.getDst() == ownDst;
    }

    private boolean isBroadcast(Packet packet) {
        return packet.getDst() == 255;
    }
}
