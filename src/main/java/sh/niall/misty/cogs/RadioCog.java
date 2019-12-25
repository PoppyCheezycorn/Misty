package sh.niall.misty.cogs;

import sh.niall.misty.utils.errors.VoiceError;
import sh.niall.misty.utils.music.MistyAudioManager;
import sh.niall.yui.commands.cogs.Cog;
import sh.niall.yui.commands.commands.Context;
import sh.niall.yui.commands.errors.ArgumentError;
import sh.niall.yui.commands.errors.CommandError;
import sh.niall.yui.commands.interfaces.Group;
import sh.niall.yui.commands.interfaces.GroupCommand;

public class RadioCog extends Cog {

    MistyAudioManager audioManager;

    public RadioCog(MistyAudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Group(name = "radio")
    public void groupRadio(Context context) throws ArgumentError {
        throw new ArgumentError("Please specify which radio you want to listen to!");
    }

    @GroupCommand(group = "radio", name = "lofi")
    public void commandLofi(Context context) throws CommandError, InterruptedException, VoiceError {
        playRadio(context, "https://www.youtube.com/watch?v=hHW1oY26kxQ");
    }

    @GroupCommand(group = "radio", name = "swing", aliases = "electroswing")
    public void commandSwing(Context context) throws CommandError, InterruptedException, VoiceError {
        playRadio(context, "https://www.youtube.com/watch?v=t3p8bmei7Dc");
    }

    @GroupCommand(group = "radio", name = "chip")
    public void commandChip(Context context) throws CommandError, InterruptedException, VoiceError {
        playRadio(context, "https://www.youtube.com/watch?v=HVyJuu8s0vQ");
    }

    @GroupCommand(group = "radio", name = "funky")
    public void commandFunky(Context context) throws CommandError, InterruptedException, VoiceError {
        playRadio(context, "https://www.youtube.com/watch?v=MJbeVA7F6ng");
    }

    public void playRadio(Context context, String url) throws CommandError, VoiceError, InterruptedException {
        // Check to see if the user is in a voice channel
        if (!audioManager.userInVoice(context.getAuthor()))
            throw new CommandError("I can't play the radio because you're not in a voice channel!");

        // Make sure the bot is in a channel
        if (!context.getGuild().getAudioManager().isConnected())
            audioManager.joinChannel(context.getGuild(), context.getAuthor().getVoiceState().getChannel());

        // See if the user is in the same channel as the bot
        if (!audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            throw new CommandError("I can't play the radio because you're not in the same voice channel as me.");

        // Update the text channel
        audioManager.getAudioGuild(context.getGuild()).setTextChannelID(context.getChannel().getId());

        // Clear!!
        audioManager.getAudioGuild(context.getGuild()).clear();

        // Play the radio
        audioManager.runQuery(context, url);
    }
}
