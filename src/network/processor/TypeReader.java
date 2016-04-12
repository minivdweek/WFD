package network.processor;

import packet.Packet;

/**
 * Created by joris.vandijk on 11/04/16.
 */
public class TypeReader implements Processor {
    private Packet packet;

    public TypeReader(Packet packet) {
        this.packet = packet;
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
            result = new FileProcessor(packet);
        } else {
            return null;
            //result = new PingProcessor(packet); //TODO create PINGPROCESSOR
        }
        return result;
    }

}
