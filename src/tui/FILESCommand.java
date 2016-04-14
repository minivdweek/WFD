package tui;

import java.io.File;

/**
 * Created by joris.vandijk on 14/04/16.
 */
public class FILESCommand implements UserCommand {


    @Override
    public void execute() {
        listFilesonOwnDevice();
    }

    public void listFilesonOwnDevice() {
        File[] files = (new File("Joris/testFiles/")).listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                System.out.println(file.getName());
            }
        } else {
            System.out.println("No files present");
        }
    }
}
