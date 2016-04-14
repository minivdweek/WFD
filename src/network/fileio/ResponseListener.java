package network.fileio;

import network.exceptions.BrokenPacketException;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
        while (!paused && !finished) {
            try {
                DatagramPacket nextPack = getNextPacket(new byte[buffersize]);
                try {
                    Packet recAckno = getAck(nextPack);
                    ackReceived(recAckno);
                    uploader.incrementLastBitAcked(recAckno.getData().length);
                } catch (BrokenPacketException e) {
                    System.out.println("ResponseListener: broken packet");
                    continue;
                }
            } catch (IOException e) {
                finished = true;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                socket.close();
            }
        }
    }

    public DatagramPacket getNextPacket(byte[] buffer) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(datagramPacket);
//        System.out.println("ResponseListener: ack received");
        return datagramPacket;
    }

    public Packet getAck(DatagramPacket dpacket) throws BrokenPacketException {
        return new Packet(dpacket);
    }

    public void ackReceived(Packet ack) {
        uploader.packageReceived(ack);
    }

}
