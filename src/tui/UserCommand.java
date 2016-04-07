package tui;

import network.Protocol;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public interface UserCommand extends Protocol {
    public void execute();
}
