package network.fileio;

import network.exceptions.BrokenPacketException;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
//TODO implement Pause, resume and cancel commands from downloading host
/**
 * ResponseListener for a FileUploader
 * Created by joris.vandijk on 12/04/16.
 */
public class ResponseListener implements FileIO, Runnable {
    private FileUploader uploader;
    private DatagramSocket socket;
    private int[] unAckedSeqNos;
    private boolean paused;
    private boolean finished;
    private int buffersize;

    public ResponseListener(FileUploader uploader, DatagramSocket socket) {
        this.socket = socket;
        this.uploader = uploader;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[buffersize];
        while (!paused && !finished) {
            try {
                DatagramPacket nextPack = getNextPacket(buffer);
                try {
                    ackReceived(getAckNo(nextPack));
                } catch (BrokenPacketException e) {
                    continue;
                }
                uploader.updateLastSeqAcked(getLowestUnackedSeqno());
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

    public int getAckNo(DatagramPacket dpacket) throws BrokenPacketException {
        Packet packet = new Packet(dpacket);
        return packet.getAckNo();
    }

    public void setUnAckedSeqNos(int[] seqNos) {
        this.unAckedSeqNos = seqNos;
    }

    public int getLowestUnackedSeqno() {
        int min = 0;
        for (int i : unAckedSeqNos) {
            if (i > 0) {
                min = i;
                break;
            }
        }
        for (int i : unAckedSeqNos) {
            if (i < min) {
                min = i;
            }
        }
        return min;
    }

    public void ackReceived(int ack) {
        for (int a = 0; a < unAckedSeqNos.length; a++) {
            if (unAckedSeqNos[a] == ack - 1) {
                unAckedSeqNos[a] = 0;
                break;
            }
        }
    }

}
