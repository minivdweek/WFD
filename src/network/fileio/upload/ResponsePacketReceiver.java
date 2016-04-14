package network.fileio.upload;

import network.exceptions.BrokenPacketException;
import network.fileio.FileIO;
import network.fileio.upload.FileUploader;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
/**
 * ResponsePacketReceiver for a FileUploader
 * Created by joris.vandijk on 12/04/16.
 */
public class ResponsePacketReceiver implements FileIO, Runnable {
    private FileUploader uploader;
    private DatagramSocket socket;
    private boolean paused;
    private boolean finished;
    private int buffersize;

    public ResponsePacketReceiver(FileUploader uploader, DatagramSocket socket) {
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
                    System.out.println("ResponsePacketReceiver: broken packet");
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
//        System.out.println("ResponsePacketReceiver: ack received");
        return datagramPacket;
    }

    public Packet getAck(DatagramPacket dpacket) throws BrokenPacketException {
        return new Packet(dpacket);
    }

    public void ackReceived(Packet ack) {
        uploader.packageReceived(ack);
    }

}
