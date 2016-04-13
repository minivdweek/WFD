package network.fileio;

import packet.Packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static network.Protocol.FIN;

/**
 * Created by joris.vandijk on 13/04/16.
 */
public class PacketUploadBuffer {
    private ConcurrentHashMap<Integer, Packet> buffer;

    public PacketUploadBuffer() {
        buffer = new ConcurrentHashMap<>();
    }

    public void add(int seqno, Packet packet) {
        buffer.put(seqno, packet);
    }


    public void fillBuffer(Packet[] packets) {
        for (Packet p : packets) {
            if (p!=null) {
                add(p.getSeqNo(), p);
            }
        }
    }

    public void remove(int key) {
        buffer.remove(key);
    }


    public ConcurrentHashMap<Integer, Packet> getWindow() {
        ConcurrentHashMap<Integer, Packet> temp = new ConcurrentHashMap<>();
        for (int p : buffer.keySet()) {
            temp.put(p, buffer.get(p));
        }

        ConcurrentHashMap<Integer, Packet> result = new ConcurrentHashMap<>();
        for (int i : temp.keySet()) {
            result.put(i, temp.get(i));
            temp.remove(i);
        }
        return result;
    }

    public int getBufferSize() {
        return buffer.size();
    }

}
