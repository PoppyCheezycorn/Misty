package sh.niall.misty.cogs;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import org.apache.commons.lang3.StringUtils;
import sh.niall.misty.utils.errors.ArgumentError;
import sh.niall.misty.utils.errors.PermissionError;
import sh.niall.misty.utils.errors.VoiceError;
import sh.niall.misty.utils.music.AudioGuild;
import sh.niall.misty.utils.music.MistyAudioManager;
import sh.niall.yui.cogs.Cog;
import sh.niall.yui.commands.Context;
import sh.niall.yui.commands.interfaces.Command;
import sh.niall.yui.exceptions.CommandException;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class MusicCog extends Cog {

    MistyAudioManager audioManager;

    public MusicCog(MistyAudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Connects Misty to the invokers voice channel
     */
    @Command(name = "connect", aliases = {"summon", "join", "move"})
    public void commandConnect(Context context) throws CommandException, InterruptedException, VoiceError, PermissionError {
        // Firstly check to see if the user is in a voice channel
        if (!audioManager.userInVoice(context.getAuthor()))
            throw new CommandException("I can't join a voice channel because you're not in one.");

        // Check if the bot is in the same channel as the user
        if (audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            return;

        // Check if the bot has permissions in the specified channel
        audioManager.checkPermissions(context.getMe(), context.getAuthor().getVoiceState().getChannel());

        // Move to the channel
        audioManager.joinChannel(context.getGuild(), context.getAuthor().getVoiceState().getChannel());

        // Update the text channel
        audioManager.getAudioGuild(context.getGuild()).setTextChannelID(context.getChannel().getId());

        // Send a message (Deletes after 5 seconds)
        context.send(String.format("Connected to: `%s`", context.getAuthor().getVoiceState().getChannel()))
                .delete().completeAfter(5, TimeUnit.SECONDS);
    }

    /**
     * Queues a song (URL) and tells the player to play.
     */
    @Command(name = "play", aliases = {"p", "queue"})
    public void commandPlay(Context context) throws CommandException, ArgumentError, InterruptedException, VoiceError {
        // First check they provided an argument
        if (context.getArgs().size() == 1)
            throw new ArgumentError("Please provide a song URL to play.");

        // Next check their argument is a valid URL
        String songURL = context.getArgs().get(1);
        if (!audioManager.isValidURL(songURL))
            throw new ArgumentError("Please provide a valid song URL to play.");

        // Check to see if the user is in a voice channel
        if (!audioManager.userInVoice(context.getAuthor()))
            throw new CommandException("I can't play your song because you're not in a voice channel!");

        // Make sure the bot is in a channel
        if (!context.getGuild().getAudioManager().isConnected())
            audioManager.joinChannel(context.getGuild(), context.getAuthor().getVoiceState().getChannel());

        // See if the user is in the same channel as the bot
        if (!audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            throw new CommandException("I can't play your song because you're not in the same voice channel as me.");

        // Update the text channel
        audioManager.getAudioGuild(context.getGuild()).setTextChannelID(context.getChannel().getId());

        // Queue the song
        audioManager.runQuery(context, songURL);
    }

    /**
     * Pauses the playback of music
     */
    @Command(name = "pause")
    public void commandPause(Context context) throws CommandException {
        // First make sure the bot is connected
        if (!context.getGuild().getAudioManager().isConnected())
            throw new CommandException("I can't pause because I'm not in a voice channel!");

        // Next check the user is in the same channel
        if (!audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            throw new CommandException("I can't pause because you're not in the same voice channel as me.");

        // Make sure the bot is already playing
        if (audioManager.getAudioGuild(context.getGuild()).player.isPaused())
            throw new CommandException("I'm already paused!");

        // Pause the music
        audioManager.getAudioGuild(context.getGuild()).pause();

        // Update the text channel
        audioManager.getAudioGuild(context.getGuild()).setTextChannelID(context.getChannel().getId());

        // Send a message
        context.send("⏸ Paused");
    }

    /**
     * Resumes the playback of music
     */
    @Command(name = "resume")
    public void commandResume(Context context) throws CommandException {
        // First make sure the bot is connected
        if (!context.getGuild().getAudioManager().isConnected())
            throw new CommandException("I can't resume because I'm not in a voice channel!");

        // Next check the user is in the same channel
        if (!audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            throw new CommandException("I can't resume because you're not in the same voice channel as me.");

        // Make sure the bot is paused
        if (!audioManager.getAudioGuild(context.getGuild()).player.isPaused())
            throw new CommandException("I'm not currently paused.");

        // Resume the music
        audioManager.getAudioGuild(context.getGuild()).resume();

        // Update the text channel
        audioManager.getAudioGuild(context.getGuild()).setTextChannelID(context.getChannel().getId());

        // Send a message
        context.send("▶ Resuming");
    }

    /**
     * Stops playback of the current song and disconnects the bot
     */
    @Command(name = "stop", aliases = {"leave"})
    public void commandStop(Context context) throws CommandException {
        // First make sure the bot is connected
        if (!context.getGuild().getAudioManager().isConnected())
            throw new CommandException("I'm not currently in a voice channel!");

        // Make the Bot leave
        audioManager.destoryAudioGuild(context.getGuild());

        // Send a message
        context.send("\\(^o^)/ Okay, see you later!").delete().completeAfter(5, TimeUnit.SECONDS);
    }

    /**
     * Skips the current song
     */
    @Command(name = "skip", aliases = {"s"})
    public void commandSkip(Context context) throws CommandException {
        // First make sure the bot is connected
        if (!context.getGuild().getAudioManager().isConnected())
            throw new CommandException("I can't skip a song because I'm not currently in a voice channel!");

        // Check to see if the user is in a voice channel
        if (!audioManager.userInVoice(context.getAuthor()))
            throw new CommandException("I can't play your song because you're not in a voice channel!");

        // Make sure the user is in the same channel
        if (!audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            throw new CommandException("I can't play your song because you're not in the same voice channel as me.");

        // Make sure the bot is playing
        if (audioManager.getAudioGuild(context.getGuild()).player.isPaused())
            throw new CommandException("I can't skip because I'm currently paused!");

        // Skip the song
        audioManager.getAudioGuild(context.getGuild()).skip();

        // Send a message
        context.send("Skipping ヽ(｀Д´)⊃━☆ﾟ. * ･ ｡ﾟ,");
    }

    /**
     * Clears the queue and stops the current songs playback
     */
    @Command(name = "clear")
    public void commandClear(Context context) throws CommandException {
        // First make sure the bot is connected
        if (!context.getGuild().getAudioManager().isConnected())
            throw new CommandException("I can't clear the queue because I'm not currently in a voice channel!");

        // Next make sure the user is in a voice channel
        if (!audioManager.userInVoice(context.getAuthor()))
            throw new CommandException("You can't clear the queue because you're not in a voice channel!");

        // Make sure the user and bot are in the same channel
        if (!audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            throw new CommandException("I can't clear the queue because you're not in the same voice channel as me.");

        // Clear!!
        audioManager.getAudioGuild(context.getGuild()).clear();

        // Update the text channel
        audioManager.getAudioGuild(context.getGuild()).setTextChannelID(context.getChannel().getId());

        // Send a message
        context.send("┻━┻ ︵ヽ(`Д´)ﾉ︵ ┻━┻ Queue Cleared");
    }

    /**
     * Gets or Sets the volume
     */
    @Command(name = "volume", aliases = {"v"})
    public void commandVolume(Context context) throws CommandException, ArgumentError {
        // First make sure the bot is connected
        if (!context.getGuild().getAudioManager().isConnected())
            throw new CommandException("I can't display the volume because I'm not currently in a voice channel!");

        // Next make sure the user is in a voice channel
        if (!audioManager.userInVoice(context.getAuthor()))
            throw new CommandException("You can't request the volume because you're not in a voice channel!");

        // Make sure the user and bot are in the same channel
        if (!audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            throw new CommandException("I can't request the volume because you're not in the same voice channel as me.");

        // Get the audio guild
        AudioGuild audioGuild = audioManager.getAudioGuild(context.getGuild());

        // Detect if no argument was provided - send the current volume if so
        if (context.getArgs().size() == 1) {
            context.send("\uD83C\uDFA7 The current volume is " + audioGuild.getVolume() + "%");
            return;
        }

        // Detect if the first argument is a int
        if (!context.getArgs().get(1).matches("-?(0|[1-9]\\d*)"))
            throw new ArgumentError("Please specify a valid number between 0-100 to change the volume to");

        // Make sure it's a number between 0-100
        int volume = Integer.parseInt(context.getArgs().get(1));
        if (volume < 0 || 100 < volume)
            throw new ArgumentError("Please specify a valid number between 0-100 to change the volume to");

        // Change the volume
        audioGuild.setVolume(volume);

        // Send a message
        context.send("\uD83C\uDFA7 The volume has been set to " + volume + "%");
    }

    /**
     * Gets the current playing song
     */
    @Command(name = "nowplaying", aliases = {"np"})
    public void commandNowPlaying(Context context) throws CommandException {
        // First make sure the bot is connected
        if (!context.getGuild().getAudioManager().isConnected())
            throw new CommandException("I can't display what I'm playing because I'm not currently in a voice channel!");

        // Make sure the bot is playing
        AudioTrack audioTrack = audioManager.getAudioGuild(context.getGuild()).getNowPlaying();
        if (audioTrack == null)
            throw new CommandException("I'm currently not playing anything!");

        // Send the embed
        String duration = String.format(
                "%s/%s",
                audioManager.durationToString(audioTrack.getPosition()),
                audioManager.durationToString(audioTrack.getDuration())
        );
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Now Playing!", audioTrack.getInfo().uri);
        embedBuilder.setDescription("From " + StringUtils.capitalize(audioTrack.getSourceManager().getSourceName()));
        embedBuilder.setImage(audioManager.getArtwork(audioTrack));
        embedBuilder.setColor(Color.PINK);
        embedBuilder.setAuthor(context.getAuthor().getEffectiveName(), null, context.getUser().getEffectiveAvatarUrl());
        embedBuilder.addField("Title:", audioTrack.getInfo().title, true);
        embedBuilder.addField("Duration:", duration, false);
        embedBuilder.addField("Volume:", audioManager.getAudioGuild(context.getGuild()).getVolume() + "%", true);
        context.send(embedBuilder.build());
    }

    /**
     * Enables/Disables Looping
     */
    @Command(name = "loop")
    public void commandLooping(Context context) throws CommandException {
        // First make sure the bot is connected
        if (!context.getGuild().getAudioManager().isConnected())
            throw new CommandException("I can't change edit the player because I'm not currently in a voice channel!");

        // Next make sure the user is in a voice channel
        if (!audioManager.userInVoice(context.getAuthor()))
            throw new CommandException("You can't request looping because you're not in a voice channel!");

        // Make sure the user and bot are in the same channel
        if (!audioManager.userInSameChannel(context.getGuild(), context.getAuthor()))
            throw new CommandException("I can't edit the looping status because you're not in the same voice channel as me.");

        // Get the audio guild
        AudioGuild audioGuild = audioManager.getAudioGuild(context.getGuild());

        // Set the looping
        audioGuild.setLoop(!audioGuild.isLooping());

        // Send the message
        if (audioGuild.isLooping())
            context.send("\uD83D\uDD01 Looping!");
        else
            context.send("❌ No longer looping!");
    }

    /**
     * Called when a member is moved to a new voice channel
     * @param event
     */
    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        // Check if the bot is the person that was moved - join that channel
        if (event.getMember().getId().equals(event.getGuild().getSelfMember().getId())) {
            try {
                // Join the channel (This will wait until done)
                audioManager.joinChannel(event.getGuild(), event.getChannelJoined());
            } catch (InterruptedException | VoiceError e) {
                TextChannel textChannel = event.getGuild().getTextChannelById(audioManager.getAudioGuild(event.getGuild()).getTextChannelID());
                textChannel.sendMessage("There was an error connecting to this voice channel").queue();
            }
        }

        // Check the bot isn't the only one in the channel
        if (event.getChannelJoined().getMembers().size() == 1)
            audioManager.checkIfAlone(event.getChannelJoined());
    }

    /**
     * Called when a member leaves a voice channel
     */
    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        // First check the bot is connected
        if (!event.getGuild().getAudioManager().isConnected())
            return;

        // Check the bot is in that voice channel
        if (!event.getChannelLeft().getId().equals(event.getGuild().getAudioManager().getConnectedChannel().getId()))
            return;

        // Check if the bot is the person that left
        if (event.getMember().getId().equals(event.getGuild().getSelfMember().getId())) {
            audioManager.destoryAudioGuild(event.getGuild());
            return;
        }

        // Check the bot isn't the only one in the channel
        if (event.getChannelLeft().getMembers().size() == 1)
            audioManager.checkIfAlone(event.getChannelLeft());
    }
}
