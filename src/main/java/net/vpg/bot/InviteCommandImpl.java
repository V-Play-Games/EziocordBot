package net.vpg.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.vpg.bot.commands.general.InviteCommand;
import net.vpg.bot.core.Bot;
import net.vpg.bot.event.CommandReceivedEvent;

public class InviteCommandImpl extends InviteCommand {
    public InviteCommandImpl(Bot bot) {
        super(bot);
    }

    @Override
    protected MessageEmbed getEmbed(CommandReceivedEvent e) {
        JDA jda = e.getJDA();
        return new EmbedBuilder()
            .setTitle(jda.getSelfUser().getName())
            .setThumbnail(jda.getSelfUser().getAvatarUrl())
            .setDescription(String.format("(Click here to invite)[%s+application.commands]", jda.getInviteUrl()))
            .build();
    }
}
