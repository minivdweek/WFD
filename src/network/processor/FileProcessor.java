package network.processor;

import network.fileio.FileDownloader;
import packet.Packet;

/**
 * provides an intermediary between the Network class and the FileDownloader Class
 * Created by joris.vandijk on 12/04/16.
 */
public class FileProcessor implements Processor {
    private Packet packet;
    private int sourcePort;

    public FileProcessor(Packet packet, int sourcePort) {
        this.packet = packet;
        this.sourcePort = sourcePort;
    }

    @Override
    public void processPacket() {
        (new Thread(new FileDownloader(packet, sourcePort))).start();
    }
}
