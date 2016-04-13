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
import java.util.HashMap;

import static java.lang.System.arraycopy;

/**
 * Created by joris.vandijk on 12/04/16.
 */
public class FileDownloadManager implements FileIO, Runnable {
    private File file;
    private FileOutputStream outputStream;
    private Integer fileLength;
    private DatagramSocket socket;
    private int targetPort;
    private int windowSize;
    private int ownAddress;
    private byte[] hash;
    private int lastPacketHandled;
    private DownloadPacketListener listener;


    public FileDownloadManager(Packet packet, int sourcePort) {
        this.targetPort = sourcePort;
        setup(packet);
    }

    public void setup(Packet packet) {
        windowSize = packet.getWindowSize();
        lastPacketHandled = packet.getSeqNo();
        byte[] rawdata = packet.getData();
        byte[] nameinpack = new byte[rawdata[0]];
        arraycopy(rawdata, 1, nameinpack, 0, nameinpack.length);
        String filename = (new String(nameinpack));
        file = new File("src/network/files/new" + filename);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            System.out.println("New file created at: " + file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] filelengthinpack = new byte[4];
        arraycopy(rawdata, nameinpack.length + 4, filelengthinpack, 0, filelengthinpack.length);
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
        listener = new DownloadPacketListener(this, socket);
        (new Thread(listener)).start();
        sendAck(packet);
    }

    @Override
    public void run() {
        //TODO add writer? if so, does this class need to be runnable? perhaps observer-pattern? <- seems best
    }

    public void writeToFile(Packet packet) throws IOException {
        byte[] data = getFilePartfromPacket(packet);
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


    public void sendAck(Packet packet) {
        try {
            packet.setFlags(ACK);
            packet.setAckNo(packet.getSeqNo());
            packet.setDst(packet.getSrc());
            packet.setSrc(ownAddress);
            packet.setPortNo(targetPort);
            socket.send(packet.toDatagramPacket());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }







}
