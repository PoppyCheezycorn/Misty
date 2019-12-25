package sh.niall.misty.cogs;

import sh.niall.misty.utils.presets.ErrorPreset;
import sh.niall.yui.commands.cogs.Cog;
import sh.niall.yui.commands.commands.Context;
import sh.niall.yui.commands.errors.ArgumentError;
import sh.niall.yui.commands.interfaces.Command;

public class HelpCog extends Cog {

    @Command(name = "help")
    public void commandHelp(Context context) throws ArgumentError {
        if (context.getArgs().size() > 1) {
            switch (context.getArgs().get(1).toLowerCase()) {
                case "fun":
                    context.send(helpFun());
                    break;

                case "music":
                    context.send(helpMusic());
                    break;

                case "radio":
                    context.send(helpRadio());
                    break;

                case "utils":
                    context.send(helpUtils());
                    break;

                default:
                    throw new ArgumentError("Help for " + context.getArgs().get(1).toLowerCase() + "doesn't exist");

            }
        } else {
            context.send(
                    helpFun()
                            + helpMusic()
                            + helpRadio()
                            + helpUtils()
            );
        }
    }

    public String helpFun() {
        return "**Fun Commands:**"
                + "```"
                + "dog -- Show a picture or video of a dog."
                + "```\n";
    }

    public String helpMusic() {
        return "**Music Commands:**"
                + "```"
                + "connect -- Summon Misty to your channel\n"
                + "play [url] -- Adds a song to the queue\n"
                + "pause -- You'll never guess what this does.\n"
                + "resume -- Resumes song playback.\n"
                + "stop -- Stops the music and disconnects Misty.\n"
                + "skip -- Skips the current song to the next song in the queue.\n"
                + "clear -- Clears the queue and stops the current song.\n"
                + "volume -- Shows the current volume.\n"
                + "volume [0-100] -- Change the current volume.\n"
                + "nowplaying -- Shows the current playing song.\n"
                + "```\n";
    }

    public String helpRadio() {
        return "**Radio Commands:**"
                + "```"
                + "radio lofi -- Starts the lofi radio.\n"
                + "radio swing -- Starts the electro swing radio.\n"
                + "radio chip -- Starts the chiptune radio.\n"
                + "radio funky -- Starts the funky radio.\n"
                + "```\n";
    }

    public String helpUtils() {
        return "**Utility Commands:**"
                + "```"
                + "avatar -- Get your current avatar.\n"
                + "avatar [ID or @] -- Get the specified users avatar.\n"
                + "screenshare -- Gets the link for the screenshare page.\n"
                + "```\n";
    }

    @Override
    public void onError(Context context, Exception error) {
        ErrorPreset.onError(context, error);
    }
}
