package network.fileio;

import packet.Packet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by joris.vandijk on 12/04/16.
 */
public class FileDownloader implements FileIO, Runnable {
    private File file;
    private FileOutputStream outputStream;
    private int fileLength;
    private int lastBitReceived;
    private DatagramSocket socket;
    private byte[] buffer;
    private int ownAddress;

    public FileDownloader(Packet packet) {
        String filename = (new String(packet.getData())).trim().split(" ")[0].trim();
        file = new File("src/network/", filename);
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

    }
}
