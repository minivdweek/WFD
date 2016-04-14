package network.fileio;

import packet.Packet;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by joris.vandijk on 13/04/16.
 */
public class PacketUploadBuffer {
    private Queue<Packet> queue;

    public PacketUploadBuffer() {
        queue = new ArrayBlockingQueue<>(20);
    }

    public void add(Packet packet) {
        queue.offer(packet);
    }
    public int getqueuesize() {
        return queue.size();
    }

    public void fillqueue(Packet[] packets) {
        for (int i = 0; i <  packets.length; i++) {
            if (packets[i]!=null) {
                add(packets[i]);
            }
        }
    }

    public void removeAcked(Packet packet) {
        for (Packet p : queue) {
            if (p.getSeqNo() == packet.getAckNo()) {
//                System.out.println("REMOVING PACKET FROMBUFFER");
                queue.remove(p);
            }
        }
    }

    public Queue<Packet> getWindow(int size) {
        Packet[] result = new Packet[queue.size()];
        queue.toArray(result);
        Queue<Packet> res = new ArrayBlockingQueue<Packet>(size);
        for (int i = 0; (i < size && i < result.length); i++) {
            if (result[i] != null ) {
                res.offer(result[i]);
            }
        }
        return res;
    }
}
