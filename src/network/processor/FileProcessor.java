package network.processor;

import packet.Packet;

/**
 * Created by joris.vandijk on 12/04/16.
 */
public class FileProcessor implements Processor {
    private Packet packet;

    public FileProcessor(Packet packet) {
        this.packet = packet;
    }

    @Override
    public void processPacket() {
        (new Thread(new FileDownloader(packet))).start();
    }
}
