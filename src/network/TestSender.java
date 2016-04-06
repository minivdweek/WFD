package network;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by joris.vandijk on 05/04/16.
 */
public class TestSender {
    private static int bufferSize = 1024;

    public static void main(String args[]) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(Protocol.PORT);
        InetAddress ip = InetAddress.getByName(Protocol.IP_ADDRESS_PI4);
        File file = new File("network/itunes.txt");
        try (FileInputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[bufferSize];
            int n = 0;
            int offset = 0;
            while (n < (int) file.length() && n != -1) {
                n += input.read(buffer, offset, bufferSize);
                DatagramPacket datagramPacket = new DatagramPacket(buffer, offset, bufferSize, ip, Protocol.PORT);
                datagramSocket.send(datagramPacket);

            }

        }
        datagramSocket.close();

    }

}
