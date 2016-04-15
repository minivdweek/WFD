package test;

import network.fileio.upload.PacketUploadBuffer;
import org.junit.Before;
import org.junit.Test;
import packet.Packet;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.*;

/**
 * Created by joris.vandijk on 15/04/16.
 */
public class PacketUploadBufferTest {
    private PacketUploadBuffer packetUploadBuffer;
    private Queue<Packet> testbuf;

    @Before
    public void setUp() throws Exception {
        packetUploadBuffer = new PacketUploadBuffer();
        testbuf = new ArrayBlockingQueue<Packet>(10);
        Packet filler = new Packet(0, null);
        for (int i = 0; i<10;i++) {
            filler.setSeqNo(i);
            filler.setAckNo(i);
            testbuf.offer(filler);
            System.out.print(filler.getSeqNo());
        }
        packetUploadBuffer.fillqueue(testbuf);
        assertTrue(packetUploadBuffer.getqueuesize() > 0);
        assertTrue(testbuf.size() == 0);
    }

    @Test
    public void getWindow() throws Exception {
        Queue<Packet> res = packetUploadBuffer.getWindow(4);
        assertEquals(4, res.size());
        assertEquals(10, packetUploadBuffer.getqueuesize());
    }

    @Test
    public void removeAcked() throws Exception {
        assertEquals(10, packetUploadBuffer.getqueuesize());
        Packet rem = new Packet(0, null);
        rem.setAckNo(5);
        packetUploadBuffer.removeAcked(rem);
        assertEquals(9, packetUploadBuffer.getqueuesize());
    }
}