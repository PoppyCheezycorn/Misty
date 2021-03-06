package sh.niall.misty;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.LoggerFactory;
import sh.niall.misty.cogs.*;
import sh.niall.misty.utils.config.Config;
import sh.niall.misty.utils.config.ConfigLoader;
import sh.niall.misty.utils.database.Database;
import sh.niall.misty.utils.errors.MistyErrorHandler;
import sh.niall.misty.utils.music.MistyAudioManager;
import sh.niall.yui.Yui;
import sh.niall.yui.exceptions.PrefixException;
import sh.niall.yui.prefix.PrefixManager;


import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;

public class Misty {

    public static Config config;
    public static Database database;


    public static void main(String[] args) throws LoginException, FileNotFoundException, PrefixException {
        // Initialize globals
        config = ConfigLoader.loadConfig();
        database = new Database();

        // Generate JDA Builder
        JDABuilder builder = new JDABuilder(config.getDiscordToken());
        builder.setAudioSendFactory(new NativeAudioSendFactory());
        builder.setActivity(Activity.watching("Anime"));

        // Create the audio manager
        MistyAudioManager audioManager = new MistyAudioManager();

        // Setup Yui
        PrefixManager prefixManager = new PrefixManager(config.getDiscordPrefixes());
        Yui yui = new Yui(builder, prefixManager, new MistyErrorHandler());
        yui.addCogs(
                new HelpCog(),
                new FunCog(),
                new MusicCog(audioManager),
                new UtilitiesCog(),
                new ModerationCog()
        );

        // Build JDA
        builder.build();
        LoggerFactory.getLogger(Misty.class).info("I'm online and ready to go!");
    }

}
