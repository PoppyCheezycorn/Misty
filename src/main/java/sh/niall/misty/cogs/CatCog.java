package sh.niall.misty.cogs;

import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import sh.niall.yui.cogs.Cog;
import sh.niall.yui.commands.Context;
import sh.niall.yui.commands.interfaces.Command;
import sh.niall.yui.exceptions.CommandException;

import java.awt.*;
import java.io.IOException;

public class FunCog extends Cog {

    OkHttpClient client = new OkHttpClient();

    @Command(name = "cat", aliases = {"kitty", "kitties"})
    public void commandCat(Context context) throws IOException, CommandException {
        // We're doing a request, so send a typing message
        context.getChannel().sendTyping().queue();

        // Request a resource
        Response resourceRequest = client.newCall(new Request.Builder().url("http://aws.random.cat/meow").build()).execute();

        // Ensure we got through
        if (resourceRequest.code() != 200) {
            resourceRequest.close();
            throw new CommandException("There were no available cats to photograph!");
        }

        // Locate the cat!
        String fileName = resourceRequest.body().string();
        String url = "https://random.cat/" + fileName;
        Response dogRequest = client.newCall(new Request.Builder().url(url).build()).execute();

        // Check the photo exists
        if (catRequest.code() != 200) {
            resourceRequest.close();
            catRequest.close();
            throw new CommandException("We found you a cat, but they were a little too shy to see you :(");
        }

        // Videos can't be embed, so we have to upload them differently
        if (fileName.endsWith(".mp4") || fileName.endsWith(".webm")) {
            context.getChannel().sendFile(catRequest.body().byteStream(), fileName).content("Here's a 🐱 Video:").complete();
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Cat Photo!");
            embedBuilder.setDescription("🐱 Meow! 🐱");
            embedBuilder.setColor(Color.cyan);
            embedBuilder.setImage(url);
            context.send(embedBuilder.build());
        }

        // Close the request
        resourceRequest.close();
        catRequest.close();
    }


}
