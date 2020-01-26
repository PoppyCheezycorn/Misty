package sh.niall.misty.utils.errors;

import net.dv8tion.jda.api.EmbedBuilder;
import sh.niall.misty.Misty;
import sh.niall.yui.cogs.ErrorHandler;
import sh.niall.yui.commands.Context;
import sh.niall.yui.exceptions.YuiException;

import java.awt.*;
import java.time.LocalDateTime;

public class MistyErrorHandler extends ErrorHandler {

    @Override
    public void onError(Context ctx, Throwable error) {
        EmbedBuilder embedBuilder = generateBaseEmbed();
        embedBuilder.setAuthor(ctx.getAuthor().getEffectiveName(), null, ctx.getUser().getEffectiveAvatarUrl());


        if (error instanceof YuiException) {
            embedBuilder.addField("Information:", error.getMessage(), false);
            ctx.send(embedBuilder.build());
            return;
        }

        embedBuilder.addField("Bot Error", "I don't know how to handle this error! Please inform my developer!", false);
        ctx.send(embedBuilder.build());
        error.printStackTrace();
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
