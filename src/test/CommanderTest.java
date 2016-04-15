package test;

import org.junit.Before;
import org.junit.Test;
import tui.*;

import static org.junit.Assert.*;

/**
 * Created by joris.vandijk on 15/04/16.
 */
public class CommanderTest {
    private Commander commander;

    @Before
    public void setUp() throws Exception {
        commander = new Commander();
    }

    @Test
    public void testGetCommand() {
        assertTrue(commander.getCommand("ls") instanceof LSCommand);
        assertTrue(commander.getCommand("put blaat") instanceof PUTCommand);
        assertTrue(commander.getCommand("devices") instanceof DEVICESCommand);
        assertTrue(commander.getCommand("files") instanceof FILESCommand);
        assertTrue(commander.getCommand("get blaat") instanceof GETcommand);
        assertTrue(commander.getCommand("asjbnj") instanceof UNKNOWNCommand);
    }



}