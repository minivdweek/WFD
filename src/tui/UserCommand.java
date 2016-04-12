package tui;

import network.Protocol;

import java.net.DatagramSocket;

/**
 * Commands are parsed from input given by the Commander class, and have an execute method
 * Created by joris.vandijk on 07/04/16.
 */
public interface UserCommand extends Protocol {
    /**
     * Executes the command.
     */
    public void execute();
}
