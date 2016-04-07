package network.processor;

import packet.Packet;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class Resender implements Processor {
    private Packet packet;

    public Resender(Packet packet) {
        this.packet = packet;
    }


    @Override
    public void processPacket() {
        //TODO resend the packet using its header
    }
}
