package tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class Commander implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                String input = readUserInput();
                //TODO doe iets met de input
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String readUserInput() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        return input.readLine();
    }

}
