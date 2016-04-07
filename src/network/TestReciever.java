package network;


import packet.Packet;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 05/04/16.
 */
public class TestReciever {
    static DatagramSocket datagramSocket;

    public static void main(String args[]) throws Exception {
        datagramSocket = new DatagramSocket(Protocol.PORT);

        String choice = null;
        try {
            do {
                System.out.println("Do you want to get the testfile?");
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                choice = input.readLine();
                input.close();
            } while (choice == null || (!choice.equalsIgnoreCase("y") &&
                    !choice.equalsIgnoreCase("n")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (choice.equalsIgnoreCase("y")) {
            requestfile("doet dit het ook?", 4);
            Thread.sleep(100);
            //getfile("network/itunes.txt");
        } else if (choice.equalsIgnoreCase("n")) {
            System.out.println("OK doei");
        }

    }


    public static void getfile(String ftg) throws IOException {

        //dit ontvangt een packet incl header, met een maximale lengte van 1024 bytes
        byte buff[] = new byte[1024];
        File file = new File(ftg);
        try (FileWriter output = new FileWriter(file)) {
            while (true) {
                DatagramPacket paquet_d = new DatagramPacket(buff, buff.length);

                //hier schrijf je paquet_d dus pas vol met bytes uit het ontvangen packet
                //"receive into" dus
                datagramSocket.receive(paquet_d);

                output.write(new String(paquet_d.getData()));
            }
        }
    }

    public static void requestfile(String str, int hostNo) throws  IOException {
        Packet requestpacket = new Packet(3, str.getBytes());
        requestpacket.setDst(hostNo);
        requestpacket.setFileID(77);
        requestpacket.setSrc(14); //find out your own ID!!!!!!!!!
        requestpacket.setSeqNo(0);
        DatagramPacket request = requestpacket.toDatagramPacket();
        datagramSocket.send(request);
    }
}
