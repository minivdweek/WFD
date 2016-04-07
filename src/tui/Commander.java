package tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This is the Class describing the commands received from the user.
 * Created by joris.vandijk on 07/04/16.
 */
public class Commander implements Runnable {

    @Override
    public void run() {
        boolean quit = false;
        while (!quit) {
            try {
                String in = readUserInput();
                if (in.equalsIgnoreCase("q")) {
                    quit = true;
                }
                handleInput(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String readUserInput() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        return input.readLine();
    }

    private void handleInput(String input) {
        UserCommand command = getCommand(input);
        if (command != null) {
            command.execute();
        }
    }

    private UserCommand getCommand(String input) {
        if (input.length() > 1) {
            String firstWord = input.trim().split(" ")[0].trim();
            if (firstWord.equalsIgnoreCase("get")) {
                return new GETcommand(input.substring(firstWord.length()));
            } else if (firstWord.equalsIgnoreCase("ls")) {
                return new LSCommand();
            } else if (firstWord.equalsIgnoreCase("put")) {
                return new PUTCommand(input.substring(firstWord.length()));
            } else if (firstWord.equalsIgnoreCase("devices")) {
                return new DEVICESCommand();
            }
        }
        return null;
    }

}
