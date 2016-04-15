package network.processor;

import network.OwnAddressFinder;
import packet.Packet;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by joris.vandijk on 11/04/16.
 */
public class LSProcessor implements Processor {
    private Packet packet;
    private int targetPort;

    public LSProcessor(Packet packet, int targetPort) {
        this.packet = packet;
        this.targetPort = targetPort;
    }

    @Override
    public void processPacket() {
        Packet response = new Packet(LIST, getFileNames().getBytes());
        response.setFlags(ACK);
        response.setPortNo(targetPort);
        response.setDst(packet.getSrc());
        response.setSrc(OwnAddressFinder.getOwnAddress());
        sendResponse(response);
    }


    public String getFileNames() {
        File[] files = (new File("Joris/testFiles/")).listFiles();
        String response = getName();
        if (files != null && files.length > 0) {
            for (File file : files) {
                response += (file.getName() + " \n ");
            }
        } else {
            response+=("No files present");
        }
        return response;
    }

    public String getName() {
        int ownAdress = OwnAddressFinder.getOwnAddress();
        if (ownAdress == 4) {
            return "PI4, reachable via target 4";
        } else if (ownAdress == 14) {
            return "Joris' Laptop, reachable via target 14";
        } else {
            return "Extenision " + ownAdress + "\n";
        }
    }

    public void sendResponse(Packet packet) {
        DatagramPacket res = packet.toDatagramPacket();
        try {
            DatagramSocket sock = new DatagramSocket();
            sock.send(res);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
