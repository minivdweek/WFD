package network.fileio;

import network.OwnAddressFinder;
import network.exceptions.BrokenPacketException;
import packet.Packet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.lang.System.arraycopy;

/**
 * Created by joris.vandijk on 12/04/16.
 */
public class FileDownloader implements FileIO, Runnable {
    private File file;
    private FileOutputStream outputStream;
    private Integer fileLength;
    private DatagramSocket socket;
    private byte[] buffer;
    private int ownAddress;
    private byte[] hash;
    private int lastPacketHandled;
    //TODO create an array of (Datagram)Packets, to deal with out-of order arrival of packets

    public FileDownloader(Packet packet) {
        lastPacketHandled = packet.getSeqNo();
        byte[] rawdata = packet.getData();
        byte[] nameinpack = new byte[rawdata[0]];
        arraycopy(rawdata, 1, nameinpack, 0, nameinpack.length);
        String filename = (new String(nameinpack));
        file = new File("Joris/", filename);
        byte[] filelengthinpack = new byte[4];
        arraycopy(rawdata, nameinpack.length + 1, filelengthinpack, 0, 4);
        fileLength = ByteBuffer.wrap(filelengthinpack).getInt();
        hash = new byte[packet.getData().length - (filename.getBytes().length + 6)];
        arraycopy(packet.getData(), filename.getBytes().length + 6, hash, 0, hash.length);
        try {
            outputStream = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        ownAddress = OwnAddressFinder.getOwnAddress();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while (file.length() < fileLength || !fileIsIntact()) {
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(datagramPacket);
                Packet packet = new Packet(datagramPacket);
                writeToFile(getFilePartfromPacket(packet));
            } catch (BrokenPacketException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeToFile(byte[] data) throws IOException {
        if (data != null) {
            outputStream.write(data);
        }
    }

    public byte[] getFilePartfromPacket(Packet packet) {
        return packet.getData();
    }

    public boolean fileIsIntact() {
        return Arrays.equals(Hasher.getHash(file), hash);
    }

    public boolean hasNextFilePart(Packet packet) {
        return packet.getSeqNo() == lastPacketHandled + 1;
    }

    public void addToReceivedList(int seqno) {

    }

    public void sendAck(Packet packet) {

    }







}
