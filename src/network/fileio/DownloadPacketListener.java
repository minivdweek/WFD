package network.fileio;

import network.exceptions.BrokenPacketException;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 13/04/16.
 */
public class DownloadPacketListener implements FileIO, Runnable {
    private FileDownloadManager downloadManager;
    private PacketDownloadBuffer packetBuffer;
    private DatagramSocket socket;
    private boolean paused;
    private boolean cancelled;
    private boolean finished;
    private int buffersize;

    public DownloadPacketListener(FileDownloadManager manager, DatagramSocket socket) {
        this.downloadManager = manager;
        this.socket = socket;
        buffersize = 1024;
        paused = false;
        cancelled = false;
        finished = false;
        packetBuffer = new PacketDownloadBuffer();
    }


    @Override
    public void run() {
        byte[] buffer = new byte[buffersize];
        while (!(paused || finished)) {
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            Packet packet;
            try {
                socket.receive(datagramPacket);
                packet = new Packet(datagramPacket);
                downloadManager.sendAck(packet);
                if (packet.getFlags() != FIN) {
                    packetBuffer.add(packet.getSeqNo(), packet);
                } else {
                    //TODO what to do if END is received
                }
                //TODO check which packets can be appended to the file.
            } catch (BrokenPacketException | IOException e) {
                continue;
            }

        }
    }
}
