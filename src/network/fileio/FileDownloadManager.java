package network.fileio;

import network.OwnAddressFinder;
import network.exceptions.BrokenPacketException;
import packet.Packet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static java.lang.System.arraycopy;

/**
 * Created by joris.vandijk on 12/04/16.
 */
public class FileDownloadManager implements FileIO {
    private Packet setupPacket;
    private File file;
    private String filename;
    private FileOutputStream outputStream;
    private Integer fileLength;
    private DatagramSocket socket;
    private int targetPort;
    private int ownAddress;
    private byte[] hash;
    private int lastPacketHandled;
    private DownloadPacketListener listener;
    private String path;
    private Queue<Integer> ackBuffer;


    public FileDownloadManager(Packet packet, int sourcePort) {
        ackBuffer = new ArrayBlockingQueue<Integer>(20);
        this.targetPort = sourcePort;
        this.setupPacket = packet;
        path = "Joris/testFiles/";
    }

    public void setup() {
        lastPacketHandled = setupPacket.getSeqNo();
        byte[] rawdata = setupPacket.getData();
        byte[] nameinpack = new byte[rawdata[0]];
        arraycopy(rawdata, 1, nameinpack, 0, nameinpack.length);
        filename = (new String(nameinpack));
        file = new File(path + filename);
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
        arraycopy(rawdata, (rawdata[0] + 1), filelengthinpack, 0, filelengthinpack.length);
        fileLength = ByteBuffer.wrap(filelengthinpack).getInt();
        System.out.println("FDM: FILELENGTH = " + fileLength);
        hash = new byte[rawdata.length - (filename.getBytes().length + 5)];
        arraycopy(rawdata, filename.getBytes().length + 5, hash, 0, hash.length);
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
        listener = new DownloadPacketListener(this, socket, outputStream, fileLength);
        (new Thread(listener)).start();
        sendAck(setupPacket);
    }

    public boolean fileIsIntact() {
        return Arrays.equals(Hasher.getHash(file), hash);
    }


    public void sendAck(Packet packet) {
        if (ackBuffer.contains(packet.getSeqNo())) {
            //double packet
        } else {
            try {
                if (packet.getFlags() != FIN) {
                    packet.setFlags(ACK);
                }
                packet.setAckNo(packet.getSeqNo());
                packet.setDst(packet.getSrc());
                packet.setSrc(ownAddress);
                packet.setPortNo(targetPort);
                socket.send(packet.toDatagramPacket());
                if (ackBuffer.size() >= 20) {
                    ackBuffer.poll();
                }
                ackBuffer.offer(packet.getAckNo());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
