package sh.niall.misty.utils.presets;

import net.dv8tion.jda.api.EmbedBuilder;
import sh.niall.misty.Misty;
import sh.niall.yui.commands.commands.Context;
import sh.niall.yui.commands.errors.YuiError;

import java.awt.*;
import java.time.LocalDateTime;

public class ErrorPreset {

    /**
     * Is called whenever there is an error
     * @param context The context of the error
     * @param error The actual exception
     */
    public static void onError(Context context, Exception error) {
        EmbedBuilder embedBuilder = generateBaseEmbed();
        embedBuilder.setAuthor(context.getAuthor().getEffectiveName(), null, context.getUser().getEffectiveAvatarUrl());


        if (error instanceof YuiError) {
            embedBuilder.addField("Information:", error.getMessage(), false);
            context.send(embedBuilder.build());
            return;
        }

        embedBuilder.addField("Bot Error", "I don't know how to handle this error! Please inform my developer!", false);
        context.send(embedBuilder.build());
    }

    private static EmbedBuilder generateBaseEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Error!");
        embedBuilder.setDescription("I ran into an error processing your request");
        embedBuilder.setColor(Color.RED);
        embedBuilder.setThumbnail(Misty.config.getDiscordErrorImage());
        embedBuilder.setTimestamp(LocalDateTime.now());
        return embedBuilder;
    }

}
