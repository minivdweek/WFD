package tui;

/**
 * Created by joris.vandijk on 11/04/16.
 */
public class UNKNOWNCommand implements UserCommand {
    @Override
    public void execute() {
        System.out.println("Command not recognized.");
    }
}
