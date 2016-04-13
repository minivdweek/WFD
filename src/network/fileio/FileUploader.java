package network.fileio;

import network.OwnAddressFinder;
import network.exceptions.BrokenPacketException;
import packet.Packet;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.toIntExact;
import static java.lang.System.arraycopy;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class FileUploader implements FileIO, Runnable {
    private File file;
    private FileInputStream inputStream;
    private int fileLength;
    private Integer lastBitSent;
    private DatagramSocket socket;
    private int target;
    private int targetPort;
    private byte[] buffer;
    private int ownAddress;
    private ResponseListener listener;
    private boolean paused;
    private boolean cancelled;
    private int currentSeqno;
    private PacketUploadBuffer packetBuffer;
    private ConcurrentHashMap<Integer, Integer> ackbuffer;
    private boolean finished;


    public FileUploader(String input, int target) {
        packetBuffer = new PacketUploadBuffer();
        ackbuffer = new ConcurrentHashMap<>();
        currentSeqno = 0;
        file = new File(input);
        System.out.println("you are sending: " + file.getName());
        this.target = target;
        fileLength = toIntExact(file.length());
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            this.socket = new DatagramSocket();
            socket.setSoTimeout(60000);
        } catch (SocketException e) {
            cancelled = true;
            e.printStackTrace();
        }
        buffer = new byte[512];
        lastBitSent = 0;
        ownAddress = OwnAddressFinder.getOwnAddress();
        listener = new ResponseListener(this, socket);
    }

    @Override
    public void run() {
        paused = false;
        cancelled = false;
        finished = false;
        try {
            //initialize
            initialize();
            //now send the file and close when completed
            sendFile();
        } catch (SocketException e) {
            cancelled = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile() throws IOException {
        do {
            sendNextWindow(10);
            while (paused) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (!(cancelled || finished));
    }

    private void sendNextWindow(int windowsize) throws IOException {
        updateBuffer(windowsize);
        sendWindow();
        System.out.println("FileUploader: progress is: " + getProgress());
    }

    private void sendWindow() throws IOException {
        if (packetBuffer.getBufferSize() > 0) {
            ConcurrentHashMap<Integer, Packet> wndw = packetBuffer.getWindow();
            for (int s : wndw.keySet()) {
//                System.out.println("FileUploader: sending packet no: " + s);
                sendNextFilePart(wndw.get(s));
                wndw.remove(s);
            }
        }
    }

    private void updateBuffer(int window) throws IOException {
        for (int a : ackbuffer.keySet()) {
            packetBuffer.remove(a);
            ackbuffer.remove(a);
        }
        if (packetBuffer.getBufferSize() < window) {
            Packet[] packets = new Packet[window];
            for (int i = 0; i < window; i++) {
                Packet temp = buildNextPacket(getNextFilePart());
                packets[i] = temp;
                if (temp.getFlags() == FIN) {
                    finished = true;
                    break;
                }
            }
            packetBuffer.fillBuffer(packets);
        }
    }


    private Packet buildNextPacket(byte[] data) {
        incrementSeqNo();
        Packet result;
        if (data != null) {
            result = new Packet(FILE, data);
        } else {
            System.out.println("FILEUPLOADER: adding final packet");
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
        if (fileLength > 0 && lastBitSent < fileLength) {
            int dataLength = inputStream.read(buffer, 0, buffer.length);
            if (dataLength >= 0) {
                lastBitSent += dataLength;
                return Arrays.copyOf(buffer, dataLength);
            }
        }
        return null;
    }

    private void sendNextFilePart(Packet packet) throws IOException{
        if (packet != null) {
            socket.send(packet.toDatagramPacket());
        }
    }

    public void packageReceived(int ack) {
        ackbuffer.put(ack, ack);
    }

    private int getProgress() {
        return (lastBitSent * 100) / fileLength;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void cancel() {
        cancelled = true;
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
                    e.printStackTrace(); //TODO handle failure to set up connection, perhaps just a system.out
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
        result.setSeqNo(++currentSeqno);
        result.setFlags(SYN);
        result.setWindowSize(10);
        return result;
    }

    private void getResponsetoInitialiser() { //TODO needs to be able to recover from BrokenPacketException
        try {
            DatagramPacket response = listener.getNextPacket(new byte[1024]);
            Packet respack = new Packet(response);
            targetPort = respack.getPortNo();
            (new Thread(listener)).start();
        } catch (BrokenPacketException | IOException e) { //must drop packet and listen again, perhaps communicate failure to user
            e.printStackTrace();
        }
    }
}
