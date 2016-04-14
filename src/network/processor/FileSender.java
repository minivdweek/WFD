package network.processor;

import packet.Packet;
import tui.PUTCommand;

/**
 * Created by joris.vandijk on 14/04/16.
 */
public class FileSender implements Processor {
    private Packet packet;

    public FileSender(Packet packet) {
        this.packet = packet;
    }

    @Override
    public void processPacket() {
        (new PUTCommand(new String(packet.getData()), packet.getSrc())).execute();
    }
}
