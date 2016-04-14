package network.fileio;

import network.exceptions.BrokenPacketException;
import packet.Packet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static java.lang.Math.toIntExact;

/**
 * Created by joris.vandijk on 13/04/16.
 */
public class DownloadPacketListener implements FileIO, Runnable {
    private FileDownloadManager downloadManager;
    private PacketDownloadBuffer packetBuffer;
    private FileOutputStream outputStream;
    private DatagramSocket socket;
    private boolean finished;
    private int buffersize;
    private int lastWrittentoFile;
    private int lastBitWritten;
    private int fileLength;
    private int[] ackbuffer;

    public DownloadPacketListener(FileDownloadManager manager, DatagramSocket socket, FileOutputStream outputStream, int fileLength) {
        this.downloadManager = manager;
        this.socket = socket;
        this.outputStream = outputStream;
        this.fileLength = fileLength;
        buffersize = 1024;
        finished = false;
        lastWrittentoFile = 0;
        lastBitWritten = 0;
        packetBuffer = new PacketDownloadBuffer();
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        byte[] buffer = new byte[buffersize];
        do {
            if (lastBitWritten < fileLength) {
                writeBufftoFile();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffersize);
                Packet packet;
                try {
                    socket.receive(datagramPacket);
                    packet = new Packet(datagramPacket);
                } catch (BrokenPacketException | IOException e) {
                    System.out.println("DPL: error receiving packet");
                    continue;
                }
                if (packet.getFlags() != FIN) {
                    packetBuffer.add(packet.getSeqNo(), packet);
                } else {
                    System.out.println("DPL: FINAL PACKET RECEIVED");
                }
            } else {
                finished = true;
                if (!socket.isClosed()) {
                    socket.close();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } while (!finished);
        if (downloadManager.fileIsIntact()) {
            System.out.println("The file is intact.");
        }
        long finish = System.currentTimeMillis();
        int timepassed = toIntExact(finish - start);
        System.out.println("FINISHED DOWNLOAD IN " + (timepassed / 1000) + "." + (timepassed % 1000) + " s");
    }

    public void writeBufftoFile() {
        Packet nextpack;
        do {
            nextpack = packetBuffer.getNextPacketinBuffer(lastWrittentoFile);
            try {
                writeToFile(nextpack);
                if (nextpack != null) {
                    lastWrittentoFile = nextpack.getSeqNo();
                    packetBuffer.remove(nextpack.getSeqNo());
                    downloadManager.sendAck(nextpack);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (nextpack != null);
    }

    public void writeToFile(Packet packet) throws IOException {
        byte[] data = getFilePartfromPacket(packet);
        if (data != null) {
            lastBitWritten += data.length;
            outputStream.write(data);
        }
    }

    public byte[] getFilePartfromPacket(Packet packet) {
        if (packet != null) {
            return packet.getData();
        } else {
            return null;
        }
    }
}
