package net.vpg.bot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookCluster;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.vpg.bot.commands.general.InviteCommand;
import net.vpg.bot.core.Bot;
import net.vpg.bot.core.BotBuilder;
import net.vpg.bot.core.ClassFilter;
import net.vpg.bot.event.CommandReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Driver {
    public static final Map<String, String> webhookUrls = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Bot bot = BotBuilder.createDefault("ye", System.getenv("TOKEN"))
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .setPrefix(".")
            .setFilter(ClassFilter.getDefault().disable(InviteCommand.class))
            //.putProperties(DataObject.fromJson(Driver.class.getResourceAsStream("properties.json")))
            //.setDatabase(new Database(System.getenv("DB_URL"), "BotData"))
            .build();
    }

    public static void broadcast(CommandReceivedEvent e, String content) {
        Guild guild = e.getGuild();
        List<BaseGuildMessageChannel> channels = guild.getChannels()
            .stream()
            .filter(c -> c.getType().isMessage())
            .map(BaseGuildMessageChannel.class::cast)
            .collect(Collectors.toList());
        List<RestAction<List<Webhook>>> actions = channels.stream()
            .filter(c -> !webhookUrls.containsKey(c.getId()))
            .map(BaseGuildMessageChannel::retrieveWebhooks)
            .collect(Collectors.toList());
        Runnable action = () -> {
            try (WebhookCluster cluster = new WebhookCluster(webhookUrls.size())) {
                cluster.addWebhooks(webhookUrls.values()
                    .stream()
                    .map(WebhookClient::withUrl)
                    .collect(Collectors.toList()));
                cluster.broadcast(content).forEach(c -> c.thenAcceptAsync(m ->
                    e.getJDA().getChannelById(BaseGuildMessageChannel.class, m.getChannelId()).deleteMessageById(m.getId()).queue()
                ));
            }
        };
        if (actions.isEmpty()) {
            action.run();
        } else {
            RestAction.allOf(actions).queue(webhooks -> {
                Map<String, String> webhookMap = webhooks.stream()
                    .flatMap(List::stream)
                    .filter(w -> w.getToken() != null)
                    .collect(Collectors.toMap(w -> w.getChannel().getId(), Webhook::getUrl));
                webhookMap.putAll(webhookUrls);
                if (webhookMap.size() < channels.size()) {
                    RestAction.allOf(channels.stream()
                        .filter(c -> !webhookMap.containsKey(c.getId()))
                        .map(c -> c.createWebhook("Ezio"))
                        .collect(Collectors.toList()))
                        .complete()
                        .forEach(w -> webhookMap.put(w.getChannel().getId(), w.getUrl()));
                }
                webhookUrls.putAll(webhookMap);
                action.run();
            });
        }
    }
}
