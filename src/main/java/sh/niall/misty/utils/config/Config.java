package sh.niall.misty.utils.config;

public class Config {

    String discordErrorImage;
    String discordToken;

    String mongoURL;

    public String getDiscordErrorImage() { return discordErrorImage; }

    public String getDiscordToken() {
        return discordToken;
    }

    public String getMongoURL() { return mongoURL; }
}
