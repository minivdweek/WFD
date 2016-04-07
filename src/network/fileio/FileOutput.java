package network.fileio;

import packet.Packet;

import java.io.File;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class FileOutput implements FileIO, Runnable {
    private File file;
    private int progress;


    public FileOutput(String input) {
        progress = 0;
        file = new File(input);
    }


    public Packet getInitialiserPacket() {

        return null;
    }

    public Packet partOfFile(byte[] data) {

        return null;
    }

    public Packet getFinalPacket() {

        return null;
    }

    @Override
    public void run() {

    }

    public void sendInitialiser() {

    }
}
