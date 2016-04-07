package network.processor;

import network.Protocol;
import packet.Packet;

import java.net.DatagramPacket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public interface Processor extends Protocol {
    //public void process(DatagramPacket datagramPacket);
    public void processPacket();
}
