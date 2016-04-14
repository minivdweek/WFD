package network;

/**
 * Created by joris.vandijk on 05/04/16.
 */
public interface Protocol {

    public static final int PORT = 30000; //Pokemon NetBattle
    public static final int BROADCAST_ADDRESS = 255;

    //packet types
    public static final int GET = 0;
    public static final int LIST = 1;
    public static final int FILE = 2;

    public static final byte[] FAILURE = "FAILURE".getBytes();
    public static final byte[] FINALPACKET = "END".getBytes();
    public static final byte[] CANCELLED = "CANCELLED".getBytes();


    //flags
    public static final int SYN = 8;
    public static final int SYNACK = 12;
    public static final int ACK = 4;
    public static final int FIN = 2;
    public static final int ACKFIN = 6;
    public static final int NOFLAGS = 0;
    public static final int FAIL = 1;
}
