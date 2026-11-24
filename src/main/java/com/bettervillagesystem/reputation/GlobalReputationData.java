package com.bettervillagesystem.reputation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 全局统一声望的世界级存储（SavedData，随存档保存）。
 * 存储于主世界，任何维度访问的都是同一份数据 ——
 * 玩家在 A 村庄的行为会自动影响 B 村庄，实现“低声望去哪都低声望”。
 */
public class GlobalReputationData extends SavedData {
    private static final String DATA_NAME = "bettervillage_reputation";

    private final Map<UUID, PlayerReputation> byPlayer = new HashMap<>();

    public static GlobalReputationData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                GlobalReputationData::loadStatic, GlobalReputationData::new, DATA_NAME);
    }

    public PlayerReputation getOrCreate(UUID playerId) {
        return byPlayer.computeIfAbsent(playerId, id -> new PlayerReputation());
    }

    /** 若该玩家所有分量均为空则移除条目，避免存档膨胀。 */
    public void cleanup(UUID playerId) {
        PlayerReputation rep = byPlayer.get(playerId);
        if (rep != null && rep.isEmpty() && rep.lastDecayGameTime == 0L) {
            byPlayer.remove(playerId);
        }
    }

    public PlayerReputation peek(UUID playerId) {
        return byPlayer.get(playerId);
    }

    public Map<UUID, PlayerReputation> all() {
        return byPlayer;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PlayerReputation> e : byPlayer.entrySet()) {
            CompoundTag entry = e.getValue().save();
            entry.putUUID("Player", e.getKey());
            list.add(entry);
        }
        tag.put("Players", list);
        return tag;
    }

    private static GlobalReputationData loadStatic(CompoundTag tag) {
        GlobalReputationData data = new GlobalReputationData();
        ListTag list = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("Player")) {
                data.byPlayer.put(entry.getUUID("Player"), PlayerReputation.load(entry));
            }
        }
        return data;
    }
}
