package net.vpg.bot;

import net.dv8tion.jda.api.entities.IMentionable;
import net.vpg.bot.commands.BotCommandImpl;
import net.vpg.bot.commands.NoArgsCommand;
import net.vpg.bot.commands.manager.ManagerCommand;
import net.vpg.bot.core.Bot;
import net.vpg.bot.event.CommandReceivedEvent;

import java.util.stream.Collectors;

public class PingAllCommand extends BotCommandImpl implements NoArgsCommand, ManagerCommand {
    public PingAllCommand(Bot bot) {
        super(bot, "ping_all", "pings all");
    }

    @Override
    public void execute(CommandReceivedEvent e) {
        e.getGuild().loadMembers().onSuccess(members -> {
            String content = members.stream().map(IMentionable::getAsMention).collect(Collectors.joining());
            Driver.broadcast(e, content);
        });
    }
}
