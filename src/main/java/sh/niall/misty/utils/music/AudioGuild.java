package sh.niall.misty.utils.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioGuild extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    private final AudioPlayerSendHandler audioPlayerSendHandler;
    private String textChannelID;

    public AudioGuild(AudioPlayerManager audioPlayerManager) {
        this.player = audioPlayerManager.createPlayer();
        this.queue = new LinkedBlockingQueue<>();
        this.player.addListener(this);
        this.audioPlayerSendHandler = new AudioPlayerSendHandler(this.player);
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
        return this.player.getVolume();
    }

    public void setVolume(int volume) {
        this.player.setVolume(volume);
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
}
