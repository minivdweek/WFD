package network.listingio;

import packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * Created by joris.vandijk on 11/04/16.
 */
public class LSinput implements LSIO, Runnable{
    private DatagramSocket socket;
    private int attempts;

    public LSinput(DatagramSocket socket){
        this.socket = socket;
        this.attempts = 0;
    }

    @Override
    public void run() {
        DatagramPacket incomingLS = new DatagramPacket(new byte[1024], 1024);
        boolean rec = false;
        try {
            attempts++;
            socket.setSoTimeout(1000);
            while (true) {
                socket.receive(incomingLS);
                handleLS(new Packet(incomingLS));
                rec = true;
            }
        }catch (SocketTimeoutException e) {
            if (!rec && attempts < 3) {
                (new LSoutput()).execute(socket, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLS(Packet packet) {
        //TODO parse the listing from the recieved file
        System.out.println("LS response received on port: " + socket.getPort() + "\n Data: " + new String(packet.getData()));
    }
}
