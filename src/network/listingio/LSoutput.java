package network.listingio;

import network.OwnAddressFinder;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * LSoutput sends an ls command to all other devices in range.
 * Created by joris.vandijk on 08/04/16.
 */
public class LSoutput implements LSIO {
    private DatagramSocket socket;

    public LSoutput() {
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a DatagramPacket over an arbitrarily chosen free socket,
     * then start a listener on that socket to catch the response(s).
     */
    public void execute(DatagramSocket sock, boolean isResend) {
        Packet lspacket = new Packet(LIST, null);
        lspacket.setDst(BROADCAST_ADDRESS);
        lspacket.setSrc(OwnAddressFinder.getOwnAddress());
        try {
            sock = new DatagramSocket();
            sock.send(lspacket.toDatagramPacket());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!isResend) {
            (new Thread(new LSinput(sock))).start();
        }
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
