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

import static java.lang.Math.toIntExact;
import static java.lang.System.arraycopy;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class FileUploader implements FileIO, Runnable {
    private String filename;
    private File file;
    private FileInputStream inputStream;
    private int fileLength;
    private Integer lastBitSent;
    private int maxseqno;
    private DatagramSocket socket;
    private int target; //the target Pi
    private int targetPort;
    private byte[] buffer;
    private int ownAddress;
    private ResponseListener listener;
    private boolean paused;
    private boolean cancelled;
    private int lastSeqAcked;

    public FileUploader(String input, int target) {
        file = new File(input);
        this.filename = file.getName() + "%%%";
        System.out.println(file.getName());
        System.out.println(file.exists());
        this.target = target;
        fileLength = toIntExact(file.length());
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        buffer = new byte[512];
        lastBitSent = 0;
        ownAddress = OwnAddressFinder.getOwnAddress();
        listener = new ResponseListener(this, socket);
        maxseqno = fileLength / buffer.length;
    }

    @Override
    public void run() {
        paused = false;
        cancelled = false;
        //TODO implement something to handle a failure to send (Catch IOException)
        try {
            //initialize
            initialize();
            //now send the file and close
            sendFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile() throws IOException{
        do {
            sendNextWindow(10);
            while (paused) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (!cancelled);
    }

    private void sendNextWindow(int window) throws IOException {
        int lastAck = lastSeqAcked;
        int[] seqsent = new int[window];
        for (int m = 0; m < window; m++) {
            seqsent[m] = m+lastAck;
        }
        listener.setUnAckedSeqNos(seqsent);
        Packet next;
        for (int n = 0; n < window; n++) {
            next = getNextPacket(getNextFilePart());
            sendNextFilePart(next , lastAck+n);
            System.out.println(getProgress());
            if (next == null) {
                break;
            }
        }
    }



    private Packet getNextPacket(byte[] data) {
        if (data != null) {
            Packet result = new Packet(FILE, data);
            result.setDst(target);
            result.setSrc(ownAddress);
            result.setPortNo(targetPort);
            return result;
        } else {
            return null;
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

    private void sendNextFilePart(Packet packet, int seqno) throws IOException{
        if (packet != null) {
            packet.setSeqNo(seqno);
            socket.send(packet.toDatagramPacket());
        } else {
            sendClosingPacket(seqno);
        }
    }

    private void sendClosingPacket(int seqno) throws  IOException {
        Packet lastp = buildFinalPacket();
        lastp.setSeqNo(seqno);
        socket.send(lastp.toDatagramPacket());
    }

    private Packet buildFinalPacket() {
        Packet result = new Packet(FILE, FINALPACKET);
        result.setDst(target);
        result.setSrc(ownAddress);
        result.setFlags(FIN);
        result.setPortNo(targetPort);
        return result;
    }

    private int getProgress() {
        return (lastSeqAcked * 100)/maxseqno;
    } //TODO needs to be last bit acked

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void cancel() {
        cancelled = true;
    }

    public void updateLastSeqAcked(int ack) {
        this.lastSeqAcked = ack;
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
        byte[] name = filename.getBytes();
        byte namelength = (byte) name.length;
        byte[] data = new byte[name.length + hash.length + 5];
        data[0] = namelength;
        arraycopy(name, 0, data, 1, name.length);
        arraycopy(filelength, 0, data, namelength + 1, filelength.length);
        arraycopy(hash, 0, data, name.length + filelength.length + 1, hash.length);
        Packet result = new Packet(FILE, data);
        result.setDst(target);
        result.setSrc(ownAddress);
        result.setSeqNo(1);
        return result;
    }

    private void getResponsetoInitialiser() { //TODO needs to be able to recover from BrokenPacketException
        try {
            DatagramPacket response = listener.getNextPacket(new byte[1024]);
            try {
                socket.receive(response);
                targetPort = (new Packet(response)).getPortNo();
            } catch (BrokenPacketException e) { //must drop packet and listen again
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
