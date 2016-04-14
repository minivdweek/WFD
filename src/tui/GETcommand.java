package tui;

import network.fileio.DownLoadRequester;

import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class GETcommand implements UserCommand {
    private String input;

    public GETcommand(String input) {
        this.input = input;
    }


    @Override
    public void execute() {
        String file = input.trim().split(" ")[0].trim();
        int target = Integer.parseInt(input.trim().split(" ")[1].trim());
        (new DownLoadRequester(file, target)).send();
    }
}
