package sh.niall.misty;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.LoggerFactory;
import sh.niall.misty.cogs.MusicCog;
import sh.niall.misty.cogs.UtilitiesCog;
import sh.niall.misty.utils.config.Config;
import sh.niall.misty.utils.config.ConfigLoader;
import sh.niall.misty.utils.database.Database;
import sh.niall.yui.commands.Yui;
import sh.niall.yui.commands.prefixes.PrefixManager;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;

public class Misty {

    public static Config config;
    public static Database database;


    public static void main(String[] args) throws LoginException, FileNotFoundException {
        // Initialize globals
        config = ConfigLoader.loadConfig();
        database = new Database();

        // Generate JDA Builder
        JDABuilder builder = new JDABuilder(config.getDiscordToken());
        builder.setAudioSendFactory(new NativeAudioSendFactory());
        builder.setActivity(Activity.watching("Anime"));

        // Setup Yui
        PrefixManager prefixManager = new PrefixManager("?");
        Yui yui = new Yui(builder, prefixManager);
        yui.registerCogs(
                new MusicCog(),
                new UtilitiesCog()
        );

        // Build JDA
        JDA jda = builder.build();
        LoggerFactory.getLogger(Misty.class).info("I'm online and ready to go!");
    }

}
