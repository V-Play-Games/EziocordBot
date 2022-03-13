package net.vpg.bot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import net.vpg.bot.commands.BotCommandImpl;
import net.vpg.bot.commands.manager.ManagerCommand;
import net.vpg.bot.core.Bot;
import net.vpg.bot.event.CommandReceivedEvent;
import net.vpg.bot.event.SlashCommandReceivedEvent;
import net.vpg.bot.event.TextCommandReceivedEvent;

import java.util.stream.Collectors;

public class SetupCommand extends BotCommandImpl implements ManagerCommand {
    public SetupCommand(Bot bot) {
        super(bot, "setup", "s.e.t.u.p.");
        setMinArgs(2);
        setMaxArgs(2);
        addOption(OptionType.ROLE, "participant_role", "Participant role", true);
        addOption(OptionType.ROLE, "alive_role", "Alive role", true);
    }

    public void execute(CommandReceivedEvent e, Role participant, Role alive) {
        GuildMessageChannel channel = (GuildMessageChannel) e.getChannel();
        BattleSetup setup = BattleSetup.createNew(channel.getId(), participant.getId(), alive.getId());
        channel.getPermissionContainer().putPermissionOverride(alive).grant(Permission.MESSAGE_SEND).queue();
        e.getGuild().loadMembers().onSuccess(members -> RestAction.allOf(members.stream()
            .filter(member -> member.getRoles().contains(participant))
            .peek(member -> setup.getAlivePeople().add(member.getId()))
            .map(member -> e.getGuild().addRoleToMember(member, alive))
            .collect(Collectors.toList()))
            .queue(x -> {
                e.send("The battle royale is starting NOW. Type " + bot.getPrefix() + "kill @user to kill them!").queue();
                e.getChannel().sendMessage("**People who are alive**\n" + setup.getAlivePeople()
                    .stream()
                    .map(id -> "<@" + id + ">")
                    .collect(Collectors.joining("\n"))).queue(message -> {
                        message.pin().queue();
                        setup.setAliveMessageId(message.getId());
                });
            })
        );
    }

    @Override
    public void onTextCommandRun(TextCommandReceivedEvent e) {
        Guild guild = e.getGuild();
        Role participant = guild.getRoleById(e.getArg(0));
        Role alive = guild.getRoleById(e.getArg(1));
        if (participant == null || alive == null) {
            e.send("One of the roles don't exist").queue();
            return;
        }
        execute(e, participant, alive);
    }

    @Override
    public void onSlashCommandRun(SlashCommandReceivedEvent e) {
        execute(e,
            e.getOption("participant_role", OptionMapping::getAsRole, null),
            e.getOption("alive_role", OptionMapping::getAsRole, null));
    }
}
