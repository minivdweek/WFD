package network;

/**
 * Created by joris.vandijk on 05/04/16.
 */
public interface Protocol {

    public static final int PORT = 30000; //Pokemon NetBattle





    public static final int BROADCAST_ADDRESS = 255;

    //packet types
    public static final int PING = 0;
    public static final int LIST = 1;
    public static final int FILE = 2;
}
