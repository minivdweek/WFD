package network.fileio;

import network.Protocol;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * ResponseListener for a FileUploader
 * Created by joris.vandijk on 12/04/16.
 */
public class ResponseListener implements Protocol, Runnable {
    private DatagramSocket socket;
    private int[] last20seqnos;
    private int[] last20acknos;
    private boolean paused;
    private boolean finished;
    private int buffersize;

    public ResponseListener(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[buffersize];
        while (!paused && !finished) {
            try {
                DatagramPacket nextPack = getNextPacket(buffer);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public DatagramPacket getNextPacket(byte[] buffer) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(datagramPacket);
        return datagramPacket;
    }

    public int getAckNo(DatagramPacket dpacket) {
        Packet packet = new Packet(dpacket);
        return packet.getAckNo();
    }

    public void addSeqNoToSentList(int seqno) {

    }

}
