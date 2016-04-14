package network.processor;

import packet.Packet;

/**
 * Created by joris.vandijk on 11/04/16.
 */
public class LSProcessor implements Processor {
    private Packet packet;

    public LSProcessor(Packet packet) {
        this.packet = packet;
    }

    @Override
    public void processPacket() {
        //TODO implement
    }
}
