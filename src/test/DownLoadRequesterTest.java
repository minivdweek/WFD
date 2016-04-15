package test;

import network.OwnAddressFinder;
import network.Protocol;
import network.fileio.DownLoadRequester;
import org.junit.Before;
import org.junit.Test;
import packet.Packet;

import static org.junit.Assert.*;

/**
 * Created by joris.vandijk on 15/04/16.
 */
public class DownLoadRequesterTest {
    private DownLoadRequester downLoadRequester;
    private Packet test;

    @Before
    public void setUp() throws Exception {
        downLoadRequester = new DownLoadRequester("itunes", 14);
        test = new Packet(Protocol.GET, "itunes".getBytes());
        test.setDst(14);
        test.setSrc(OwnAddressFinder.getOwnAddress());
        test.setFlags(Protocol.FILE);
    }

    @Test
    public void buildPacket() throws Exception {
        assertArrayEquals(test.toBytes(), downLoadRequester.buildPacket().toBytes());
    }
}