package network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * this class gets the local address, represented by a single int value in the range 0-254
 * Created by joris.vandijk on 08/04/16.
 */
public class OwnAddressFinder {

    public static int getOwnAddress() {
        try {
        Enumeration Net = NetworkInterface.getNetworkInterfaces();
            while (Net.hasMoreElements()) {
                NetworkInterface neta = (NetworkInterface) Net.nextElement();
                Enumeration Inets = neta.getInetAddresses();
                while (Inets.hasMoreElements()) {
                    InetAddress add = (InetAddress) Inets.nextElement();
                    if (add.toString().substring(1,4).equals("172")) {
                        return Integer.parseInt(add.toString().substring(10));
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
