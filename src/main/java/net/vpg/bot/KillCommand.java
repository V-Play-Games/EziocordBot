package net.vpg.bot;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.vpg.bot.commands.BotCommandImpl;
import net.vpg.bot.core.Bot;
import net.vpg.bot.event.CommandReceivedEvent;
import net.vpg.bot.event.SlashCommandReceivedEvent;
import net.vpg.bot.event.TextCommandReceivedEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class KillCommand extends BotCommandImpl {
    public KillCommand(Bot bot) {
        super(bot, "kill", "kill 'em");
        addOption(OptionType.USER, "target", "Who you wanna kill", true);
    }

    @Override
    public void onTextCommandRun(TextCommandReceivedEvent e) {
        List<Member> members = e.getMessage().getMentionedMembers();
        if (members.isEmpty()) {
            e.send("You have to ping someone to kill them").queue();
            return;
        }
        if (members.size() > 1) {
            e.send("You cannot kill 2 people at once, I'll ignore the others").queue();
        }
        execute(e, members.get(0));
    }

    @Override
    public void onSlashCommandRun(SlashCommandReceivedEvent e) {
        e.send("Can't escape slowmode :)").queue();
    }

    public void execute(CommandReceivedEvent e, Member target) {
        BattleSetup setup = BattleSetup.get(e.getChannel().getId());
        if (setup == null) {
            return;
        }
        if (target.getRoles().stream().noneMatch(role -> role.getId().equals(setup.getParticipantId()))) {
            e.send("That user is not a participant").queue();
            return;
        }
        Role aliveRole = e.getGuild().getRoleById(setup.getAliveId());
        List<String> alivePeople = setup.getAlivePeople();
        if (!alivePeople.contains(e.getMember().getId())) {
            e.send("You are not alive in the battle royale").queue();
            return;
        }
        if (!alivePeople.contains(target.getId())) {
            e.send("That user is not alive in the battle royale").queue();
            return;
        }
        e.getGuild().removeRoleFromMember(target, aliveRole).queue();
        alivePeople.remove(target.getId());
        int kills = setup.getKills().computeIfAbsent(e.getUser().getId(), x -> new AtomicInteger()).incrementAndGet();
        e.send(e.getUser().getAsMention() + " successfully assassinated " + target.getAsMention() + "\nTotal Kills: " + kills).queue();
        e.getChannel().editMessageById(setup.getAliveMessageId(), "**People who are alive**\n" + alivePeople.stream()
            .map(id -> "<@" + id + ">")
            .collect(Collectors.joining("\n"))).queue();

        if (alivePeople.size() == 1) {
            ((GuildChannel) e.getChannel()).getPermissionContainer().putPermissionOverride(aliveRole).reset().queue();
            String winner = alivePeople.get(0);
            e.getGuild().removeRoleFromMember(winner, aliveRole).queue();
            e.getChannel().deleteMessageById(setup.getAliveMessageId()).queue();
            e.getChannel().sendMessage("<@" + winner + "> is the winner!").queue();
            e.getChannel().sendMessage("**KILL TABLE**\n" + setup.getKills()
                .entrySet()
                .stream()
                .sorted(Comparator.<Map.Entry<String, AtomicInteger>>comparingInt(entry -> entry.getValue().get()).reversed())
                .map(entry -> "<@" + entry.getKey() + "> - " + entry.getValue())
                .collect(Collectors.joining("\n"))).queue();
        }
    }
}