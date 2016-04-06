package network;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by joris.vandijk on 05/04/16.
 */
public class TestReciever {
    public static void main(String args[]) throws Exception {
        DatagramSocket datagramme = new DatagramSocket(Protocol.PORT);

        //dit ontvangt een packet incl header, met een maximale lengte van 1024 bytes
        byte buff[] = new byte[1024];
        File file = new File("network/itunes2.txt");
        try (FileWriter output = new FileWriter(file)) {
            while (true) {
                DatagramPacket paquet_d = new DatagramPacket(buff, buff.length);

                //hier schrijf je paquet_d dus pas vol met bytes uit het ontvangen packet
                //"receive into" dus
                datagramme.receive(paquet_d);

                output.write(new String(paquet_d.getData()));
            }
        }
    }
}
