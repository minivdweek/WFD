package network.fileio;

import network.OwnAddressFinder;
import packet.Packet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import static java.lang.System.arraycopy;

/**
 * Created by joris.vandijk on 12/04/16.
 */
public class FileDownloader implements FileIO, Runnable {
    private File file;
    private FileOutputStream outputStream;
    private Integer fileLength;
    private int lastBitReceived;
    private DatagramSocket socket;
    private byte[] buffer;
    private int ownAddress;
    private byte[] hash;

    public FileDownloader(Packet packet) {
        byte[] rawdata = packet.getData();
        byte[] nameinpack = new byte[rawdata[0]];
        arraycopy(rawdata, 1, nameinpack, 0, nameinpack.length);
        String filename = (new String(nameinpack));
        file = new File("src/network/", filename);
        byte[] filelengthinpack = new byte[4];
        arraycopy(rawdata, nameinpack.length + 1, filelengthinpack, 0, 4);
        fileLength = ByteBuffer.wrap(filelengthinpack).getInt();
        hash = new byte[packet.getData().length - (filename.getBytes().length + 6)];
        arraycopy(packet.getData(), filename.getBytes().length + 6, hash, 0, hash.length);
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
        lastBitReceived = 0;
        ownAddress = OwnAddressFinder.getOwnAddress();
    }

    @Override
    public void run() {

    }

    public void writeToFile(byte[] data) {
        try {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();//TODO handle error while writing to file
        }
    }


}
