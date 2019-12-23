package sh.niall.misty.utils.music;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.Document;
import sh.niall.misty.Misty;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioGuild extends AudioEventAdapter {

    // JDA and Database
    private final String guildID;
    private String textChannelID;
    private final MongoCollection<Document> mongoCollection;

    // Audio
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    private final AudioPlayerSendHandler audioPlayerSendHandler;

    // Config
    private int volume = 100;

    public AudioGuild(MongoCollection<Document> mongoCollection, Guild guild, AudioPlayerManager audioPlayerManager) {
        // JDA and Database
        this.guildID = guild.getId();
        this.mongoCollection = mongoCollection;

        // Audio
        this.player = audioPlayerManager.createPlayer();
        this.queue = new LinkedBlockingQueue<>();
        this.player.addListener(this);
        this.audioPlayerSendHandler = new AudioPlayerSendHandler(this.player);

        // Guild Config
        loadConfig();
    }

    public AudioPlayerSendHandler getAudioPlayerSendHandler() {
        return audioPlayerSendHandler;
    }

    public void addToQueue(AudioTrack track) {
        queue.offer(track);
    }

    public void play() {
        if (this.player.isPaused())
            this.player.setPaused(false);

        if (this.player.getPlayingTrack() == null && this.queue.size() > 0) {
            player.setVolume(this.volume);
            player.playTrack(queue.poll());
        }
    }

    public void pause() {
        this.player.setPaused(true);
    }

    public void resume() {
        this.player.setPaused(false);
    }

    public void stop() {
        this.player.stopTrack();
        this.player.destroy();
        this.queue.clear();
    }

    public void skip() {
        this.player.stopTrack();
        play();
    }

    public void clear() {
        this.player.stopTrack();
        this.queue.clear();
    }

    public AudioTrack getNowPlaying() {
        return this.player.getPlayingTrack();
    }

    public int getVolume() {
        return this.volume;
    }

    public void setVolume(int volume) {
        this.player.setVolume(volume);
        updateDocument(new Document("$set", new Document("volume", volume)));
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext)
            play();
    }

    public String getTextChannelID() {
        return textChannelID;
    }

    public void setTextChannelID(String textChannelID) {
        this.textChannelID = textChannelID;
    }

    private void loadConfig() {
        Document document = getDocument();
        this.volume = document.getInteger("volume");
    }

    /**
     * Gets the document for the guild, creates one if it doesn't exist.
     */
    private Document getDocument() {
        Document document = this.mongoCollection.find(Filters.eq("_id", this.guildID)).first();
        if (document != null)
            return document;

        document = new Document();
        document.append("_id", this.guildID);
        document.append("volume", this.volume);
        this.mongoCollection.insertOne(document);
        return document;
    }

    private void updateDocument(Document document) {
        mongoCollection.updateOne(Filters.eq("_id", this.guildID), document);
    }
}
