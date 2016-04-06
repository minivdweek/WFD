package network;

import packet.Packet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by joris.vandijk on 06/04/16.
 */
public class FileReceiver {
    private File file;
    private Map<Integer, DatagramPacket> buffer;
    private FileWriter writer;

    public FileReceiver(File file, int windowsize) {
        this.file = file;
        this.buffer = new HashMap<Integer, DatagramPacket>();
        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            System.out.println("Error setting up writer");
            e.printStackTrace();
        }
    }

    public void receive(Packet packet) {
//        byte[] rawdata = packet;
    }

    public void writeToFile(byte[] data) {
        try {
            writer.write(new String(data));
        } catch (IOException e) {
            System.out.println("Error writing to file");
            e.printStackTrace();
        }
    }

    public void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            System.out.println("Error closing writer");
            e.printStackTrace();
        }
    }


    public File getFile() {
        return file;
    }

}
