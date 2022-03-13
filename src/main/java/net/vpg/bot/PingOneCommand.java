package net.vpg.bot;

import net.dv8tion.jda.api.entities.IMentionable;
import net.vpg.bot.commands.BotCommandImpl;
import net.vpg.bot.commands.manager.ManagerCommand;
import net.vpg.bot.core.Bot;
import net.vpg.bot.event.SlashCommandReceivedEvent;
import net.vpg.bot.event.TextCommandReceivedEvent;

import java.util.stream.Collectors;

public class PingOneCommand extends BotCommandImpl implements ManagerCommand {
    public PingOneCommand(Bot bot) {
        super(bot, "ping_one", "p");
    }

    @Override
    public void onTextCommandRun(TextCommandReceivedEvent e) {
        String content = e.getMessage().getMentionedUsers().stream().map(IMentionable::getAsMention).collect(Collectors.joining());
        Driver.broadcast(e, content);
    }

    @Override
    public void onSlashCommandRun(SlashCommandReceivedEvent e) {
        e.send("nope.").queue();
    }
}