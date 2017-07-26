/**
 * Application entry point class.
 *
 * @author Smidgey
 * @since 19th July 2017
 * @version 1.0
 */
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class Application {
  public static void main(String[ ] args) {
    String token = null;

    // Attempt to read our token from file.
    try(FileReader fr = new FileReader("token.txt")) {
      token = new BufferedReader(fr).readLine( );
    } catch(IOException ex) {
      ex.printStackTrace( );
      System.exit(1);
    }

    ZyBot bot = new ZyBot(token);
    bot.run( );
  }
}
