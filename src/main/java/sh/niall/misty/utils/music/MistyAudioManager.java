package sh.niall.misty.utils.music;

import com.mongodb.client.MongoCollection;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.bson.Document;
import sh.niall.misty.Misty;
import sh.niall.misty.utils.errors.PermissionError;
import sh.niall.misty.utils.errors.VoiceError;
import sh.niall.yui.commands.Context;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MistyAudioManager {
    private AudioPlayerManager audioPlayerManager;
    private HashMap<String, AudioGuild> audioGuilds;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    final static Pattern URL_PATTERN = Pattern.compile("\\s*(https?|attachment)://\\S+\\s*", Pattern.CASE_INSENSITIVE);
    MongoCollection<Document> mongoCollection;

    public MistyAudioManager() {
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        this.audioGuilds = new HashMap<>();
        this.mongoCollection = Misty.database.getCollection("audio");
    }

    /**
     * Retrieves the AudioGuild for the specified Guild. Creates one if it doesn't exist.
     */
    public synchronized AudioGuild getAudioGuild(Guild guild) {
        AudioGuild audioGuild = audioGuilds.get(guild.getId());
        if (audioGuild == null) {
            audioGuild = new AudioGuild(mongoCollection, guild, audioPlayerManager);
            audioGuilds.put(guild.getId(), audioGuild);
        }

        guild.getAudioManager().setSendingHandler(audioGuild.getAudioPlayerSendHandler());
        return audioGuild;
    }

    /**
     * Deletes the AudioGuild Instance for the specified guild
     */
    public synchronized void destoryAudioGuild(Guild guild) {
        AudioGuild audioGuild = audioGuilds.get(guild.getId());
        if (audioGuild == null)
            return;

        audioGuild.stop();
        guild.getAudioManager().closeAudioConnection();
        audioGuilds.remove(guild.getId());
    }

    /**
     * Runs a search query and queues the found song or songs
     */
    public void runQuery(Context context, String query) {
        AudioGuild audioGuild = getAudioGuild(context.getGuild());
        audioPlayerManager.loadItemOrdered(audioGuild, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioGuild.addToQueue(track);
                audioGuild.play();
                context.send("(っ^з^)♪♬ Added `" + track.getInfo().title + "` to the queue!");
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                int added = 0;
                for (AudioTrack track : playlist.getTracks()) {
                    audioGuild.addToQueue(track);
                    added++;
                }
                System.out.println("(っ^з^)♪♬ Added `" + added + "` songs to the queue!");
            }

            @Override
            public void noMatches() {
                context.send("( º﹃º ) No results were found!");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("Load failed");
                exception.printStackTrace();
            }
        });
    }

    /**
     * Gets the current songs artwork
     */
    public String getArtwork(AudioTrack audioTrack) {
        String platform = audioTrack.getSourceManager().getSourceName();
        if (platform.equals("youtube"))
            return "https://i3.ytimg.com/vi/" + audioTrack.getInfo().identifier + "/hqdefault.jpg";

        return null;
    }

    /**
     * Converts a long into minutes and seconds
     */
    public String durationToString(Long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Command methods - These are all methods to help with the Music commands

    /**
     * Returns true if the member is in a voice channel
     */
    public boolean userInVoice(Member member) {
        if (member.getVoiceState() != null)
            return member.getVoiceState().inVoiceChannel();
        return false;
    }

    public boolean userInSameChannel(Guild guild, Member member) {
        // Check if the bot is connected
        if (!guild.getAudioManager().isConnected())
            return false;

        // Check if the member is connected
        if (member.getVoiceState() == null || !member.getVoiceState().inVoiceChannel())
            return false;

        // Check if the member and bot are in the same channel
        return guild.getAudioManager().getConnectedChannel().getId().equals(member.getVoiceState().getChannel().getId());
    }

    public void checkPermissions(Member bot, VoiceChannel channel) throws PermissionError {
        for (Permission perm : new Permission[]{Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD}) {
            if (!bot.hasPermission(channel, perm))
                throw new PermissionError(String.format("I don't have the permission `%s` in the channel `%s`",
                        perm.getName(),
                        channel.getName()
                ));
        }
    }

    /**
     * Joins a voice channel and returns
     */
    public void joinChannel(Guild guild, VoiceChannel channel) throws InterruptedException, VoiceError {
        // Get the Guilds audio manager
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(channel);

        // Wait until the bot is connected
        while (audioManager.isAttemptingToConnect()) // Loop until connected
            Thread.sleep(100);

        // Will fail if the voice channel failed to join
        if (!audioManager.isConnected())
            throw new VoiceError("Failed to connect to the channel!");
    }

    public boolean isValidURL(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    public void checkIfAlone(VoiceChannel channel) {
        executorService.schedule(() -> handleAlone(channel), 5, TimeUnit.SECONDS);
    }

    private void handleAlone(VoiceChannel channel) {
        // First make sure we're still connected
        if (!channel.getGuild().getAudioManager().isConnected())
            return;

        // Check if we're alone
        if (channel.getMembers().size() != 1)
            return;

        destoryAudioGuild(channel.getGuild());
    }

}
