package net.vpg.bot;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BattleSetup {
    private static final Map<String, BattleSetup> setups = new HashMap<>();
    private final String channelId;
    private final String participantId;
    private final String aliveId;
    private final List<String> alivePeople = new ArrayList<>();
    private final Map<String, AtomicInteger> kills = new HashMap<>();
    private String aliveMessageId;

    public BattleSetup(String channelId, String participantId, String aliveId) {
        this.channelId = channelId;
        this.participantId = participantId;
        this.aliveId = aliveId;
    }

    public static BattleSetup get(String id) {
        return setups.get(id);
    }

    public static BattleSetup createNew(String channelId, String participantId, String aliveId) {
        BattleSetup setup = new BattleSetup(channelId, participantId, aliveId);
        setups.put(channelId, setup);
        return setup;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public String getAliveId() {
        return aliveId;
    }

    public Map<String, AtomicInteger> getKills() {
        return kills;
    }

    public List<String> getAlivePeople() {
        return alivePeople;
    }

    public String getAliveMessageId() {
        return aliveMessageId;
    }

    public void setAliveMessageId(String aliveMessageId) {
        this.aliveMessageId = aliveMessageId;
    }
}
