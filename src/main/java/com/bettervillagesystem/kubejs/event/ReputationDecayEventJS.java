package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * BetterVillageEvents.reputationDecay —— 全局声望每日衰减。
 */
public class ReputationDecayEventJS extends ServerEventJS {
    private final UUID playerId;
    private final Map<String, Integer> changes;

    public ReputationDecayEventJS(MinecraftServer server, UUID playerId, Map<String, Integer> changes) {
        super(server);
        this.playerId = playerId;
        this.changes = changes;
    }

    public String getPlayerId() {
        return playerId.toString();
    }

    /** 玩家在线时返回其对象，离线为 null。 */
    @Nullable
    public ServerPlayer getPlayer() {
        return getServer().getPlayerList().getPlayer(playerId);
    }

    /** 分量id → 本次衰减变化量（负数表示减少）。 */
    public Map<String, Integer> getChanges() {
        return changes;
    }
}
