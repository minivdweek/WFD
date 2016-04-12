package tui;

import network.listingio.LSoutput;

/**
 * Does nothing but provides an intermediary for the commands to the network part of the program
 * Created by joris.vandijk on 07/04/16.
 */
public class LSCommand implements UserCommand {

    @Override
    public void execute() {
        LSoutput output = new LSoutput();
        output.execute(output.getSocket(), false);
    }
}
