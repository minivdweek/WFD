package tui;

/**
 * Created by joris.vandijk on 07/04/16.
 */
public class PUTCommand implements UserCommand {
    private String filename;

    public  PUTCommand(String input) {
        this.filename = input;
    }

    @Override
    public void execute() {
        //TODO handle the execution, hand of responsibility to do so to another class?
    }
}
