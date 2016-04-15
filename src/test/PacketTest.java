package test;

import network.Protocol;
import org.junit.Before;
import org.junit.Test;
import packet.Packet;

import java.net.DatagramPacket;

import static org.junit.Assert.*;

/**
 * Created by joris.vandijk on 15/04/16.
 */
public class PacketTest {
    private Packet packet;
    private DatagramPacket datagramPacket;
    private byte[] testData;

    @Before
    public void setUp() throws Exception {
        testData = ("Dit is de testdata").getBytes();
        packet = new Packet(Protocol.LIST, testData);
        assertEquals(Protocol.LIST, packet.getType());
        assertEquals(testData, packet.getData());
        assertEquals(Protocol.PORT, packet.getPortNo());
        packet.setSrc(14);
        packet.setDst(14);
        packet.setSeqNo(0);
        packet.setAckNo(0);
        packet.setFlags(0);
        packet.setWindowSize(1);
        packet.setDataLength(testData.length);
        packet.buildHeader();
        datagramPacket = packet.toDatagramPacket();
        assertTrue(datagramPacket instanceof DatagramPacket);
        assertArrayEquals(packet.toBytes(), datagramPacket.getData());
    }


    @Test
    public void toDatagramPacket() throws Exception {
        DatagramPacket test = packet.toDatagramPacket();
        assertEquals(Protocol.PORT, test.getPort());
        assertEquals(packet.dstToIP(), test.getAddress());
    }

    @Test
    public void createCheckSum() throws Exception {
        Packet test = new Packet(Protocol.FILE, testData);
        assertArrayEquals(test.createCheckSum(testData), test.createCheckSum(test.getData()));
    }

    @Test
    public void concatenateHeaderData() throws Exception {
        byte[] test = new byte[4];
        byte[] expected = new byte[8];
        Packet p = new Packet(Protocol.FILE, test);
        assertArrayEquals(expected, p.concatenateHeaderData(test, test));
    }
}