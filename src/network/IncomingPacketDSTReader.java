package network;

import network.exceptions.BrokenPacketException;
import network.processor.Processor;
import network.processor.Resender;
import network.processor.TypeReader;
import packet.Packet;

import java.net.DatagramPacket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class IncomingPacketDSTReader {

    public void process(DatagramPacket datagramPacket) throws BrokenPacketException {
        Packet packet = new Packet(datagramPacket);
        getProcessor(packet, datagramPacket).processPacket();
    }

    public Processor getProcessor(Packet packet, DatagramPacket datagramPacket) {
        Processor processor;
        if (isBroadcast(packet) || iAmDestination(packet)) {
            //handling of packet meant for this host
            processor = new TypeReader(packet, datagramPacket);
        } else {
            //handling of packet not meant for this host
            processor = new Resender(packet);
        }
        return processor;
    }

    private boolean iAmDestination(Packet packet) {
        return packet.getDst() == OwnAddressFinder.getOwnAddress();
    }

    private boolean isBroadcast(Packet packet) {
        return packet.getDst() == 255;
    }
}