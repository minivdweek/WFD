package network.fileio.upload;

import network.OwnAddressFinder;
import network.exceptions.BrokenPacketException;
import network.fileio.FileIO;
import network.fileio.Hasher;
import packet.Packet;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static java.lang.Math.toIntExact;
import static java.lang.System.arraycopy;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class FileUploader implements FileIO, Runnable {
    private boolean exit;
    private File file;
    private FileInputStream inputStream;
    private int fileLength;
    private Integer lastBitRead;
    private int lastBitAcked;
    private DatagramSocket socket;
    private int target;
    private int targetPort;
    private byte[] buffer;
    private int ownAddress;
    private ResponsePacketReceiver listener;
    private boolean paused;
    private boolean cancelled;
    private int currentSeqno;
    private PacketUploadBuffer packetBuffer;
    private boolean finished;
    private long packetsSent;
    private long acksReceived;
    private int progress;


    public FileUploader(String input, int target) throws NoSuchFileException {
        exit = false;
        packetBuffer = new PacketUploadBuffer();
        packetsSent = 0;
        acksReceived = 0;
        currentSeqno = 0;
        progress = 0;
        String path = "Joris/testFiles/";
        file = new File(path + input);
        System.out.println(file.getPath());
        System.out.println("you are sending: " + file.getName());
        this.target = target;
        fileLength = toIntExact(file.length());
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            exit = true;
        }
        if (!exit) {
            try {
                this.socket = new DatagramSocket();
            } catch (SocketException e) {
                cancelled = true;
                e.printStackTrace();
            }
            buffer = new byte[512];
            lastBitRead = 0;
            lastBitAcked = 0;
            ownAddress = OwnAddressFinder.getOwnAddress();
            listener = new ResponsePacketReceiver(this, socket);
        } else {
            System.out.println("wrong filename");
        }
    }

    @Override
    public void run() {
        if (!exit) {
            long start = System.currentTimeMillis();
            paused = false;
            cancelled = false;
            finished = false;
            try {
                initialize();
                sendFile();
            } catch (SocketException e) {
                System.out.println("FUL: CANCELLED");
                cancelled = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            long finish = System.currentTimeMillis();
            int timepassed = toIntExact(finish - start);
            System.out.println("FINISHED UPLOAD IN " + (timepassed / 1000) + "." + (timepassed % 1000) + " s");
            System.out.println("number of packets lost: " + (packetsSent - acksReceived));
        }
    }

    private void sendFile() throws IOException {
        while (!(finished || cancelled)) {
            sendNextWindow(10);
            try {
                Thread.sleep(50); //give the other devices a chance , check if neccessary
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (paused) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    private void sendNextWindow(int windowsize) throws IOException {
        updateBuffer(windowsize);
        if (lastBitAcked < fileLength) {
            int temp = (lastBitAcked * 100) / fileLength;
            if (temp > progress) {
                progress = temp;
                System.out.print(".");
            }
            sendWindow();
        } else {
            System.out.print("\n");
            finished = true;
        }
    }

    private void sendWindow() throws IOException {
        Queue<Packet> wndw = packetBuffer.getWindow(10);
        if (!wndw.isEmpty()) {
            for (int i = 0; i < wndw.size(); i++) {
                sendNextFilePart(wndw.poll());
            }
        }
    }

    private void updateBuffer(int window) throws IOException {
        if (packetBuffer.getqueuesize() < window) {
            int bufsize = packetBuffer.getqueuesize();
            int numToAdd = window - bufsize;
            Queue<Packet> packets = new ArrayBlockingQueue<Packet>(numToAdd);
            while (packets.size() < numToAdd) {
                packets.offer(buildNextPacket(getNextFilePart()));
            }
            packetBuffer.fillqueue(packets);
        }
    }


    private Packet buildNextPacket(byte[] data) {
        incrementSeqNo();
        Packet result;
        if (data != null) {
            result = new Packet(FILE, data);
        } else {
            result = new Packet(FILE, FINALPACKET);
            result.setFlags(FIN);
        }
        result.setDst(target);
        result.setSrc(ownAddress);
        result.setPortNo(targetPort);
        result.setSeqNo(currentSeqno);
        return result;
    }

    private void incrementSeqNo() {
        if (currentSeqno < 127) {
            currentSeqno++;
        } else {
            currentSeqno = 1;
        }
    }

    private byte[] getNextFilePart() throws IOException {
        if (fileLength > 0 && lastBitRead < fileLength) {
            int dataLength = inputStream.read(buffer, 0, buffer.length);
            if (dataLength >= 0) {
                lastBitRead += dataLength;
                return Arrays.copyOf(buffer, dataLength);
            }
        }
        inputStream.close();
        return null;
    }

    private void sendNextFilePart(Packet packet) throws IOException{
        if (packet != null) {
            socket.send(packet.toDatagramPacket());
            packetsSent++;
        }
    }

    public void packageReceived(Packet ack) {
        acksReceived++;
        packetBuffer.removeAcked(ack);
    }

    //-----sets up the connection -----//
    private void initialize() {
        boolean succes = false;
        int tries = 0;
        while (!succes) {
            try {
                tries++;
                socket.send(buildInitialiserPacket().toDatagramPacket());
                succes = true;
            } catch (IOException e) {
                if (tries > 2) {
                    e.printStackTrace();
                }
            }
        }
        getResponsetoInitialiser();
    }


    private Packet buildInitialiserPacket() throws IOException {
        byte[] hash = Hasher.getHash(file);
        byte[] filelength = ByteBuffer.allocate(4).putInt(fileLength).array();
        byte[] name = file.getName().getBytes();
        byte namelength = (byte) name.length;
        byte[] data = new byte[name.length + hash.length + 5];
        data[0] = namelength;
        arraycopy(name, 0, data, 1, name.length);
        arraycopy(filelength, 0, data, namelength + 1, filelength.length);
        arraycopy(hash, 0, data, name.length + filelength.length + 1, hash.length);
        Packet result = new Packet(FILE, data);
        result.setDst(target);
        result.setSrc(ownAddress);
        result.setSeqNo(currentSeqno);
        result.setFlags(SYN);
        result.setWindowSize(10);
        return result;
    }

    private void getResponsetoInitialiser() {
        try {
            DatagramPacket response = listener.getNextPacket(new byte[1024]);
            Packet respack = new Packet(response);
            if (respack.getFlags() != FAIL) {
                targetPort = respack.getPortNo();
                (new Thread(listener)).start();
            } else {
                System.out.println(new String(respack.getData()));
            }
        } catch (BrokenPacketException | IOException e) {
            e.printStackTrace();
        }
    }

    public void incrementLastBitAcked(int amount) {
        this.lastBitAcked += amount;
    }
}
