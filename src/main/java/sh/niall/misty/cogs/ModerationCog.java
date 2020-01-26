package sh.niall.misty.cogs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import sh.niall.misty.utils.errors.ArgumentError;
import sh.niall.yui.cogs.Cog;
import sh.niall.yui.commands.Context;
import sh.niall.yui.commands.interfaces.Command;
import sh.niall.yui.exceptions.CommandException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModerationCog extends Cog {

    @Command(name = "kick", aliases = {"votekick"})
    public void _commandKick(Context ctx) throws CommandException, InterruptedException {
        if (ctx.getArgs().size() < 2)
            throw new ArgumentError("Please provide a valid target to kick!");

        // Firstly get the member we want to kick
        String targetString = ctx.getArgs().get(1);
        targetString = targetString.replace("<@", "").replace(">", "").replace("!", "");
        if (!targetString.matches("\\d+"))
            throw new ArgumentError("Please provide a valid target to kick!");

        // Get the member
        Member target = ctx.getGuild().getMemberById(Long.parseLong(targetString));
        if (target == null)
            throw new ArgumentError("Please provide a valid target to kick!");

        // Now get the list of online members
        List<Member> onlineMembers = new ArrayList<>();
        for (Member member : ctx.getGuild().getMembers()) {
            if (member.getOnlineStatus().equals(OnlineStatus.OFFLINE) || member.getUser().isBot())
                continue;
            onlineMembers.add(member);
        }

        // Create the embed
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Vote Kick!");
        embedBuilder.setDescription(
                String.format("It's time to decide %s fate. You have 30 seconds to vote.", target.getEffectiveName())
        );
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField("Votes needed:", String.valueOf((onlineMembers.size() + 1) / 3), true);
        Message msg = ctx.send(embedBuilder.build());

        // Add the reaction
        msg.addReaction("U+2705").complete();
        Long msgId = msg.getIdLong();

        // Wait...
        Thread.sleep(30000);

        // Work out the results
        msg = ctx.getChannel().retrieveMessageById(msgId).complete();
        MessageReaction checkReaction = null;
        for (MessageReaction reaction : msg.getReactions()) {
            System.out.println(reaction.getReactionEmote().getEmoji());
            if (reaction.getReactionEmote().getEmoji().equals("✅"))
                checkReaction = reaction;
        }
        if (checkReaction == null)
            throw new CommandException("The check mark emoji is missing!");

        if (checkReaction.getCount() < (onlineMembers.size() + 1) / 3) {
            ctx.send(String.format("%s survives another day...", target.getEffectiveName()));
            return;
        }
        try {
            target.kick().complete();
            ctx.send(String.format("%s was kicked, the jury wins!", target.getEffectiveName()));
        } catch (HierarchyException error) {
            ctx.send("Sadly I can't kick someone with the same rank as me :(");
        }
    }
}
