package network.fileio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by joris.vandijk on 12/04/16.
 */
public class Hasher {
    public static byte[] getHash(File file){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(Paths.get(file.toURI())));
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return new byte[1];
    }
}
