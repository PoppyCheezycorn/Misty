package sh.niall.misty.cogs;

import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sh.niall.yui.commands.cogs.Cog;
import sh.niall.yui.commands.commands.Context;
import sh.niall.yui.commands.errors.CommandError;
import sh.niall.yui.commands.interfaces.Command;

import java.awt.*;
import java.io.IOException;

public class FunCog extends Cog {

    OkHttpClient client = new OkHttpClient();

    @Command(name = "dog", aliases = {"puppo", "puppos"})
    public void commandDog(Context context) throws IOException, CommandError {
        // We're doing a request, so send a typing message
        context.getChannel().sendTyping().queue();

        // Request a resource
        Response resourceRequest = client.newCall(new Request.Builder().url("https://random.dog/woof").build()).execute();

        // Ensure we got through
        if (resourceRequest.code() != 200) {
            resourceRequest.close();
            throw new CommandError("There were no available dogs to photograph!");
        }

        // Locate the dog!
        String fileName = resourceRequest.body().string();
        String url = "https://random.dog/" + fileName;
        Response dogRequest = client.newCall(new Request.Builder().url(url).build()).execute();

        // Check the photo exists
        if (dogRequest.code() != 200) {
            resourceRequest.close();
            dogRequest.close();
            throw new CommandError("We found you a dog, but they were a little too shy to see you :(");
        }

        // Videos can't be embed, so we have to upload them differently
        if (fileName.endsWith(".mp4") || fileName.endsWith(".webm")) {
            context.getChannel().sendFile(dogRequest.body().byteStream(), fileName).content("Here's a 🐶 Video:").complete();
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Dog Photo!");
            embedBuilder.setDescription("🐶 Woof! 🐶");
            embedBuilder.setColor(Color.cyan);
            embedBuilder.setImage(url);
            context.send(embedBuilder.build());
        }

        // Close the request
        resourceRequest.close();
        dogRequest.close();
    }


}
