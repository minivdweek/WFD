package network.numbergenerator;

/**
 * Created by joris.vandijk on 11/04/16.
 */
public class SeqNoGenerator {
    private static int currentSeqNo;

    public SeqNoGenerator() {
        resetSeqNo();
    }

    public static void resetSeqNo() {
        currentSeqNo = 0;
    }

    public static int getNextSeqNo() {
        currentSeqNo++;
        return currentSeqNo;
    }
}
