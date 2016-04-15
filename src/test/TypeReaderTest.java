package test;

import network.Protocol;
import network.processor.FileProcessor;
import network.processor.FileSender;
import network.processor.LSProcessor;
import network.processor.TypeReader;
import org.junit.Before;
import org.junit.Test;
import packet.Packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * Created by joris.vandijk on 15/04/16.
 */
public class TypeReaderTest {
    private DatagramPacket sourcepack;
    private Packet file;
    private Packet list;
    private Packet get;
    private Packet empty;

    @Before
    public void setUp() throws Exception {
        file = new Packet(Protocol.FILE, null);
        list = new Packet(Protocol.LIST, null);
        get = new Packet(Protocol.GET, null);
        empty = new Packet(8, null);
        sourcepack = new DatagramPacket(new byte[10], 10, InetAddress.getLocalHost(), 18010);
    }

    @Test
    public void getProcessor() throws Exception {
        assertTrue((new TypeReader(file, sourcepack)).getProcessor() instanceof FileProcessor);
        assertTrue((new TypeReader(list, sourcepack)).getProcessor() instanceof LSProcessor);
        assertTrue((new TypeReader(get, sourcepack)).getProcessor() instanceof FileSender);
        assertNull((new TypeReader(empty, sourcepack)).getProcessor());
    }
}