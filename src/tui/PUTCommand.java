package tui;

import network.fileio.FileUploader;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class PUTCommand implements UserCommand {
    private String filename;
    private int target;

    public PUTCommand(String input) {
        this(input, BROADCAST_ADDRESS);
        if (input.split(" ").length > 1) {
            try {
                this.filename = input.trim().split(" ")[0];
                this.target = Integer.parseInt(input.split(" ")[1].trim());
            } catch (IllegalArgumentException e) {
                this.target = BROADCAST_ADDRESS;
            }
        }
    }

    public PUTCommand(String input, int target) {
        this.filename = input;
        this.target = target;
    }

    @Override
    public void execute() {
        if (target != BROADCAST_ADDRESS) {
            sendFile();
        } else {
            System.out.println("Please specify destiation device.");
            (new DEVICESCommand()).execute();
        }
    }

    private void sendFile(){
        (new Thread(new FileUploader(filename, target))).start();
    }
}
