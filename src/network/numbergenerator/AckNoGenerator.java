package network.numbergenerator;

/**
 * Created by joris.vandijk on 11/04/16.
 */
public class AckNoGenerator {
    private static int currentAckNo;

    public AckNoGenerator() {
        resetAckNo();
    }

    public static void resetAckNo() {
        currentAckNo = 0;
    }

    public static int getNextAckNo() {
        currentAckNo++;
        return currentAckNo;
    }
}
