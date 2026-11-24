package com.bettervillagesystem.reputation;

/**
 * 统一声望的五个分量，语义与原版 GossipType 一一对应：
 * 总声望 = Σ(分量值 × weight)，与原版村民对话（gossip）计算方式相同。
 */
public enum ReputationType {
    /** 重大负面：杀村民/杀铁傀儡，weight -5，上限 100，每天衰减 10。 */
    MAJOR_NEGATIVE("major_negative", -5, 100, 10),
    /** 轻微负面：攻击村民等，weight -1，上限 200，每天衰减 20。 */
    MINOR_NEGATIVE("minor_negative", -1, 200, 20),
    /** 轻微正面：日常善行，weight 1，上限 200，每天衰减 1。 */
    MINOR_POSITIVE("minor_positive", 1, 200, 1),
    /** 重大正面：治愈僵尸村民，weight 5，上限 100，不衰减。 */
    MAJOR_POSITIVE("major_positive", 5, 100, 0),
    /** 交易：每次交易积累，weight 1，上限 25，每天衰减 2。 */
    TRADING("trading", 1, 25, 2);

    public static final ReputationType[] VALUES = values();

    public final String id;
    public final int weight;
    public final int max;
    public final int decayPerDay;

    ReputationType(String id, int weight, int max, int decayPerDay) {
        this.id = id;
        this.weight = weight;
        this.max = max;
        this.decayPerDay = decayPerDay;
    }

    /** 原版语义：低于 |2| 的分量视为无记忆并移除。 */
    public static final int DISCARD_THRESHOLD = 2;

    @Override
    public String toString() {
        return id;
    }
}
