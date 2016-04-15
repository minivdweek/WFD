package test;
import org.junit.runners.Suite;
import org.junit.runner.RunWith;

/**
 * Created by joris.vandijk on 15/04/16.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CommanderTest.class,
        DownLoadRequesterTest.class,
        PacketTest.class, 
        TypeReaderTest.class})
public class Tests {
}
