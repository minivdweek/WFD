package network.processor;

import packet.Packet;

import java.net.DatagramPacket;

/**
 * Created by joris.vandijk on 11/04/16.
 */
public class TypeReader implements Processor {
    private Packet packet;
    private int sourcePort;

    public TypeReader(Packet packet, DatagramPacket datagramPacket) {
        this.packet = packet;
        this.sourcePort = datagramPacket.getPort();

    }

    @Override
    public void processPacket() {
        (getProcessor()).processPacket();
    }

    public Processor getProcessor() {
        int type = packet.getType();
        Processor result;
        if (type == LIST) {
            result = new LSProcessor(packet);
        } else if (type == FILE) {
            result = new FileProcessor(packet, sourcePort);
        } else {
            return null;
            //result = new PingProcessor(packet); //TODO create PINGPROCESSOR
        }
        return result;
    }

}
