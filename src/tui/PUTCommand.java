package tui;

import network.fileio.FileOutput;

import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class PUTCommand implements UserCommand {
    private String filename;
    private int target;

    public PUTCommand(String input) {
        this.filename = input;
        this.target = 4; //TODO set bc adress in protocol
    }

    public PUTCommand(String input, int target) {
        this.filename = input;
        this.target = target;
    }

    @Override
    public void execute() {
        //TODO check available devices, ask user?
        sendFile();
    }

    //TODO not necessary!!!!
    @Override
    public void setSocket(DatagramSocket socket) {
    }

    private void sendFile(){
        (new Thread(new FileOutput(filename, target))).start();
    }
}
