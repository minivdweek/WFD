package network.fileio.download;

import network.exceptions.BrokenPacketException;
import network.fileio.FileIO;
import packet.Packet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static java.lang.Math.toIntExact;

/**
 * Created by joris.vandijk on 13/04/16.
 */
public class DownloadPacketReceiver implements FileIO, Runnable {
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

    public DownloadPacketReceiver(FileDownloadManager manager, DatagramSocket socket, FileOutputStream outputStream, int fileLength) {
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
        while (!finished) {
            writeBufftoFile();
            System.out.println(fileLength - lastBitWritten);
            if (lastBitWritten < fileLength) {
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
                }
            } else {
                finished = true;
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!socket.isClosed()) {
            socket.close();
        }
        if (downloadManager.fileIsIntact()) {
            System.out.println("The file is intact.");
        } else {
            System.out.println("The file is not intact");
        }
        long finish = System.currentTimeMillis();
        int timepassed = toIntExact(finish - start);
        System.out.println("FINISHED DOWNLOAD IN " + (timepassed / 1000) + "." + (timepassed % 1000) + " s, with " + fileLength/(timepassed) + " kB/s.");
    }

    public void writeBufftoFile() {
        Packet nextpack = packetBuffer.getNextPacketinBuffer(lastWrittentoFile);
        while (nextpack != null ) {
            try {
                writeToFile(nextpack);
                lastWrittentoFile = nextpack.getSeqNo();
                packetBuffer.remove(nextpack.getSeqNo());
                downloadManager.sendAck(nextpack);
            } catch (IOException e) {
                e.printStackTrace();
            }
            nextpack = packetBuffer.getNextPacketinBuffer(lastWrittentoFile);
        }
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
