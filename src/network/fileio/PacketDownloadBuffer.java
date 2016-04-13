package network.fileio;

import packet.Packet;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by joris.vandijk on 13/04/16.
 */
public class PacketDownloadBuffer {
    private ConcurrentHashMap<Integer, Packet> buffer;
    private int seqnoAtEndofFile;

    public PacketDownloadBuffer() {
        this.buffer = new ConcurrentHashMap<>();
    }

    public void add(int seqno, Packet packet) {
        buffer.put(seqno, packet);
    }

    public Packet getNextPacketinBuffer(int lastaddedtofile) {
        for (int i : buffer.keySet()) {
            if (i == lastaddedtofile +1) {
                return buffer.get(i);
            }
        }
        return null;
    }

    public void remove(int key) {
        buffer.remove(key);
    }

    public boolean bufferEmpty() {
        return buffer.isEmpty();
    }



}
