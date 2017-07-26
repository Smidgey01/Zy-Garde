import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.ClientBuilder;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ZyBot implements Runnable {
    /**
     * The thing that handles our connection to discord.
     */
    private final IDiscordClient client;

    //Where the bad words are
    private ArrayList<Pattern> badWords;

    /**
     * Creates a new instance of our bot with the given token as
     * the login token.
     *
     * @param token the Discord token to log in with.
     */
    public ZyBot(String token) {
        client = new ClientBuilder()
                .setDaemon(false)
                .withToken(token)
                .build();
        badWords = new ArrayList<>();
        // The file will be called "swears.txt" and have each word
        // to block on a separate line.
        try (FileReader in = new FileReader("swears.txt")) {
            // Lets us read an entire line, rather than bytes
            BufferedReader reader = new BufferedReader(in);

            // Not the nicest way to do this, but the proper way
            // will look confusing for now. While(true) will loop
            // forever until we manually return or break out of it.
            while (true) {
                String next = reader.readLine();
                // If we are at the end of the file, then next is "null".
                // In this case, we will break out of the loop!
                if (next == null)
                    break;
                else
                    badWords.add(Pattern.compile(next, Pattern.CASE_INSENSITIVE));
            }
        }
// If an error reading the file occurs, e.g. it is corrupt, in use or
// the file doesn't exist, just print the error and continue.
        catch (IOException ex) {
            System.err.println("Error occurred reading swearwords: " +
                    ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Attempts to log in, returns true if we logged in without a problem.
     * or false if we failed.
     */
    private boolean login() {
        try {
            System.out.println("Logging in");
            client.login();
            System.out.println("*Hacker Voice* I'm in...");
            return true;
        } catch (Throwable ex) {
            System.err.println(ex.getClass() + " occurred with message: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * The run( ) method. Logs us in to discord and
     * starts listening for the events we want.
     */
    @Override
    public void run() {
        if (login()) {
            client.getDispatcher().registerListener(this);
        }
    }

    /**
     * This prints any incoming messages to the console.
     */
    @EventSubscriber
    public void printMessage(MessageReceivedEvent message) {
        System.out.println("Message from "
                + message.getAuthor()
                + " saying "
                + message.getMessage().getContent());
    }

    /**
     * This greets people on every channel as soon as we go online.
     */
    @EventSubscriber
    public void greet(ReadyEvent ready) {
        for (IChannel channel : client.getChannels()) {
            channel.sendMessage("Sombra Online");
        }
    }

    //Detects messages and responds unless it's a self message

    public @EventSubscriber void detectMessage(MessageReceivedEvent mre) {
        IUser messageOwner = mre.getAuthor();
        IUser botUser = client.getOurUser();
        if(botUser.equals(messageOwner)) {
            // Stop executing the function
            return;
        }

        final String originalText = mre.getMessage( ).getContent( );

        String fixedText = originalText;
        for(Pattern p : badWords) {
            fixedText = p.matcher(fixedText).replaceAll("冒涜");
        }

        if(!originalText.equals(fixedText)) {
            IMessage msg = mre.getMessage( );
            msg.delete( );
            msg.reply(
                    String.format("\n%s\n`Corrected that for ya! ~ZyBot!`", fixedText)
            );
        }
    }
}