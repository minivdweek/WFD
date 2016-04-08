package tui;

import network.Protocol;

import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public interface UserCommand extends Protocol {
    public void execute();
    public void setSocket(DatagramSocket socket);
}
