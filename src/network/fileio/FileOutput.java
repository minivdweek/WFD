package network.fileio;

import network.OwnAddressFinder;
import packet.Packet;

import java.io.*;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class FileOutput implements FileIO, Runnable {
    private File file;
    private FileInputStream inputStream;
    //private int progress; //keep track of how many packets have been sent
    private int fileLength;
    private int lastBitSent;
    private DatagramSocket socket;
    private int target; //the target Pi
    private byte[] buffer;
    private int ownAddress;


    public FileOutput(String input, int target) {
        //progress = 0;
        file = new File(input);
        System.out.println(file.getName());
        System.out.println(file.exists());
        this.target = target;
        fileLength = (int) file.length();
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
        buffer = new byte[256];
        lastBitSent = 0;
        ownAddress = OwnAddressFinder.getOwnAddress();
    }

    @Override
    public void run() { //TODO hier komt alles in
        //TODO implement something to handle a failure to send (Catch IOException)
        try {
            sendInitialiser();
            Packet next;
            do {
                next = getNextPacket(getNextFilePart());
                sendNextFilePart(next);
                //TODO and update progress
                System.out.println(getProgress());
            } while (next != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Packet getInitialiserPacket() throws IOException{
        Packet result = new Packet(FILE, getHash(file));
        result.setDst(target);
        result.setSrc(ownAddress);
        return result;
    }

    private Packet getNextPacket(byte[] data) {
        if (data != null) {
            Packet result = new Packet(FILE, data);
            result.setDst(target);
            result.setSrc(ownAddress);
            return result;
        } else {
            return null;
        }
    }

    private Packet getFinalPacket() {
        return null;
    }

    private byte[] getNextFilePart() throws IOException {
        if (fileLength > 0 && lastBitSent < fileLength) {
            int dataLength = inputStream.read(buffer, 0, buffer.length);
            if (dataLength >= 0) {
                lastBitSent += dataLength;
                return Arrays.copyOf(buffer, dataLength); //TODO testen of dit ndig is
            }
        }
        return null;
    }

    private void sendInitialiser() throws IOException {
        socket.send(getInitialiserPacket().toDatagramPacket());
    }

    private void sendNextFilePart(Packet packet) throws IOException{
        if (packet!=null) {
            socket.send(packet.toDatagramPacket());
        } else {
            sendClosingPacket();
        }
    }

    private void sendClosingPacket() throws  IOException {
        socket.send(getFinalPacket().toDatagramPacket());
    }

    private byte[] getHash(File file) throws IOException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(Paths.get(file.toURI())));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getProgress() {
        return (lastBitSent*100)/fileLength;
    }
}
