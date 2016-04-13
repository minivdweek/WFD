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
    private boolean paused;
    private boolean finished;
    private int buffersize;

    public ResponseListener(FileUploader uploader, DatagramSocket socket) {
        buffersize = 1024;
        this.socket = socket;
        this.uploader = uploader;
        paused = false;
        finished = false;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[buffersize];
        while (!paused && !finished) {
            try {
                DatagramPacket nextPack = getNextPacket(buffer);
                try {
                    int recAckno = getAckNo(nextPack);
                    ackReceived(recAckno);
                } catch (BrokenPacketException e) {
                    System.out.println("ResponseListener: broken packet");
                    continue;
                }
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

    public void ackReceived(int ack) {
        uploader.packageReceived(ack);
    }

    //TODO unAckedSeqNos is being reinitialised each time, even if an ack for some of the packets has been received
    //not ok, a package can be received and handled (added to the new file), but stay unAcked...

}
