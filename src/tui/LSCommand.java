package tui;

import network.OwnAddressFinder;
import packet.Packet;

import java.io.IOException;
import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class LSCommand implements UserCommand {
    private DatagramSocket socket;
    private int target;

    public LSCommand() {
        target = 4;
    }

    @Override
    public void execute() {
        //TODO hand of responsibility to another class?
        Packet lspacket = new Packet(LIST, null);
        lspacket.setDst(target);
        lspacket.setSrc(OwnAddressFinder.getOwnAddress());
        try {
            //socket.setBroadcast(true);
            socket.send(lspacket.toDatagramPacket());
           // socket.setBroadcast(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }
}
