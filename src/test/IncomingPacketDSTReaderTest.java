package test;

import network.IncomingPacketDSTReader;
import network.OwnAddressFinder;
import network.Protocol;
import network.processor.Resender;
import network.processor.TypeReader;
import org.junit.Before;
import org.junit.Test;
import packet.Packet;

import java.net.DatagramPacket;

import static org.junit.Assert.*;

/**
 * Created by joris.vandijk on 15/04/16.
 */
public class IncomingPacketDSTReaderTest {
    private IncomingPacketDSTReader incomingPacketDSTReader;
    private DatagramPacket notForMe;
    private DatagramPacket forMe;
    private Packet fm;
    private Packet nfm;

    @Before
    public void setUp() {
        incomingPacketDSTReader = new IncomingPacketDSTReader();
        Packet temp = new Packet(Protocol.FILE, null);
        temp.setDst(OwnAddressFinder.getOwnAddress());
        assertEquals(temp.getDst(), OwnAddressFinder.getOwnAddress());
        forMe = temp.toDatagramPacket();
        temp.setDst(100);
        assertNotEquals(temp.getDst(), OwnAddressFinder.getOwnAddress());
        notForMe = temp.toDatagramPacket();
    }

    @Test
    public void getProcessor() throws Exception {
        fm = new Packet(Protocol.FILE, null);
        nfm = new Packet(Protocol.FILE, null);
        fm.setDst(OwnAddressFinder.getOwnAddress());
        nfm.setDst(100);

        assertTrue(incomingPacketDSTReader.getProcessor(fm, forMe) instanceof TypeReader);
        assertTrue(incomingPacketDSTReader.getProcessor(nfm, notForMe) instanceof Resender);
    }

    @Test
    public void iAmDestination() throws Exception {
        assertTrue(incomingPacketDSTReader.iAmDestination(new Packet(forMe)));
        assertFalse(incomingPacketDSTReader.iAmDestination(new Packet(notForMe)));
    }

    @Test
    public void isBroadcast() throws Exception {
        assertFalse(incomingPacketDSTReader.isBroadcast(new Packet(forMe)));
        assertFalse(incomingPacketDSTReader.isBroadcast(new Packet(notForMe)));
    }
}