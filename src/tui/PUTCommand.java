package tui;

import network.fileio.FileUploader;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class PUTCommand implements UserCommand {
    private String filename;
    private int target;

    public PUTCommand(String input) {
        this.filename = input;
        this.target = BROADCAST_ADDRESS;
    }

    public PUTCommand(String input, int target) {
        this.filename = input;
        this.target = target;
    }

    @Override
    public void execute() {
        //TODO check available devices, ask user?
        //user does not specify dest --> do ls call
        //check available devices, if more than one, user must specify destination
        //
        sendFile();
    }

    private void sendFile(){
        (new Thread(new FileUploader(filename, target))).start();
    }
}
