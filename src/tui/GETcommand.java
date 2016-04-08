package tui;

import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class GETcommand implements UserCommand {
    private String filename;

    public GETcommand(String input) {
        this.filename = input;
    }


    @Override
    public void execute() {
        //TODO get the file, hand of responsibility to do so to downloader class?
    }

    @Override
    public void setSocket(DatagramSocket socket) {

    }
}
