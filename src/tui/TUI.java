package tui;
//TODO implement
/**
 * Created by joris.vandijk on 12/04/16.
 */
public class TUI {
    private String window;
    public TUI() {

    }

    public String clearDown(int no) {
        String cleardown = "";
        for (int c = 0; c < no; c++) {
            cleardown += "\n";
        }
        return cleardown;
    }

    public String uploadDisp() {
        String result = "Uploads: \n";
        //put uploads here
        result += "-----------------------------\n";
        return result;
    }

    public String downloadDisp() {
        String result = "Downloads: \n";
        //put downloads here
        result += "-----------------------------\n";
        return result;
    }

    public String prompt() {
        return "What do you want to do?";
    }
}
