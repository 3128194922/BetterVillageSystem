package com.bettervillagesystem.reputation;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

/**
 * 单个玩家的统一声望：五个分量值 + 上次衰减记录。
 * 加合/钳制规则与原版 GossipContainer 一致。
 */
public class PlayerReputation {
    private final EnumMap<ReputationType, Integer> values = new EnumMap<>(ReputationType.class);
    /** 上次执行每日衰减的游戏刻，用于支持长期离线玩家的补衰减。 */
    public long lastDecayGameTime;

    /** 累加声望分量并按原版规则钳制，返回衰减后是否仍保留该分量。 */
    public boolean add(ReputationType type, int amount) {
        int merged = values.getOrDefault(type, 0) + amount;
        if (merged > type.max) {
            merged = Math.max(type.max, values.getOrDefault(type, 0));
        }
        if (Math.abs(merged) < ReputationType.DISCARD_THRESHOLD) {
            values.remove(type);
            return false;
        }
        values.put(type, merged);
        return true;
    }

    /** 覆写某个分量的值（API 用），仍受上限钳制。 */
    public void setValue(ReputationType type, int value) {
        int clamped = Math.abs(value) > type.max ? Integer.signum(value) * type.max : value;
        if (Math.abs(clamped) < ReputationType.DISCARD_THRESHOLD) {
            values.remove(type);
        } else {
            values.put(type, clamped);
        }
    }

    public void remove(ReputationType type) {
        values.remove(type);
    }

    public int get(ReputationType type) {
        return values.getOrDefault(type, 0);
    }

    /** 总声望 = Σ(值 × 权重)，范围与原版一致（约 -700 ~ +925）。 */
    public int weightedTotal() {
        int total = 0;
        for (Map.Entry<ReputationType, Integer> e : values.entrySet()) {
            total += e.getValue() * e.getKey().weight;
        }
        return total;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    /** 执行一天的衰减，返回 类型→变化量（仅包含实际变化项）。 */
    public Map<ReputationType, Integer> decayOneDay() {
        EnumMap<ReputationType, Integer> changes = new EnumMap<>(ReputationType.class);
        for (ReputationType type : ReputationType.VALUES) {
            if (type.decayPerDay == 0) {
                continue;
            }
            int old = get(type);
            if (old == 0) {
                continue;
            }
            int reduced = old > 0 ? old - type.decayPerDay : old + type.decayPerDay;
            if (Math.abs(reduced) < ReputationType.DISCARD_THRESHOLD) {
                values.remove(type);
                changes.put(type, -old);
            } else {
                values.put(type, reduced);
                changes.put(type, reduced - old);
            }
        }
        return changes;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("LastDecay", lastDecayGameTime);
        for (Map.Entry<ReputationType, Integer> e : values.entrySet()) {
            tag.putInt(e.getKey().id, e.getValue());
        }
        return tag;
    }

    public static PlayerReputation load(CompoundTag tag) {
        PlayerReputation rep = new PlayerReputation();
        rep.lastDecayGameTime = tag.getLong("LastDecay");
        for (ReputationType type : ReputationType.VALUES) {
            if (tag.contains(type.id)) {
                rep.values.put(type, tag.getInt(type.id));
            }
        }
        return rep;
    }
}
