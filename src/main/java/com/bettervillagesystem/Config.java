package com.bettervillagesystem;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 通用配置（COMMON）。声望数值本身走 KubeJS 事件（可逐次修改），
 * 这里只放开关与阈值类的全局参数。
 */
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue GOLEM_AGGRO_ENABLED;
    public static final ForgeConfigSpec.IntValue GOLEM_AGGRO_THRESHOLD;
    public static final ForgeConfigSpec.IntValue GOLEM_AGGRO_RANGE;
    public static final ForgeConfigSpec.BooleanValue ALERT_GOLEMS_ON_DROP;

    public static final ForgeConfigSpec.BooleanValue GOLEM_KILL_PENALTY_ENABLED;
    public static final ForgeConfigSpec.IntValue GOLEM_KILL_REP_AMOUNT;
    public static final ForgeConfigSpec.BooleanValue GOLEM_HURT_PENALTY_ENABLED;
    public static final ForgeConfigSpec.IntValue GOLEM_HURT_REP_AMOUNT;

    public static final ForgeConfigSpec.BooleanValue DECAY_ENABLED;

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("iron_golem");
        GOLEM_AGGRO_ENABLED = BUILDER.comment(
                "When true, naturally spawned iron golems become hostile to players whose",
                "unified reputation is at or below golemAggroThreshold (vanilla golems only",
                "anger at players who hit them).")
                .define("golemAggroEnabled", true);
        GOLEM_AGGRO_THRESHOLD = BUILDER.comment(
                "Reputation total at which iron golems turn hostile. One murdered villager",
                "with a witness is about -125, so -100 catches single-murder players.")
                .defineInRange("golemAggroThreshold", -100, Integer.MIN_VALUE, -1);
        GOLEM_AGGRO_RANGE = BUILDER.comment("Horizontal range in blocks for golems to notice a low-reputation player.")
                .defineInRange("golemAggroRange", 16, 4, 64);
        ALERT_GOLEMS_ON_DROP = BUILDER.comment(
                "When reputation drops to the threshold, immediately anger nearby golems",
                "instead of waiting for the periodic scan.")
                .define("alertGolemsOnReputationDrop", true);
        BUILDER.pop();

        BUILDER.push("golem_penalty");
        GOLEM_KILL_PENALTY_ENABLED = BUILDER.comment(
                "Apply a reputation penalty when a player kills an iron golem.",
                "Vanilla registers this reputation type (golem_killed) but never fires it;",
                "this mod restores the intended behavior.")
                .define("golemKillPenaltyEnabled", true);
        GOLEM_KILL_REP_AMOUNT = BUILDER.comment(
                "Gossip value applied on golem kill (major_negative, weight -5,",
                "so 25 value = -125 reputation, same as murdering a villager).")
                .defineInRange("golemKillRepAmount", 25, 1, 100);
        GOLEM_HURT_PENALTY_ENABLED = BUILDER.comment(
                "Apply a smaller reputation penalty each time a player damages an iron golem.",
                "Off by default to stay close to vanilla.")
                .define("golemHurtPenaltyEnabled", false);
        GOLEM_HURT_REP_AMOUNT = BUILDER.comment(
                "Gossip value applied on golem hurt (minor_negative, weight -1,",
                "so 10 value = -10 reputation).")
                .defineInRange("golemHurtRepAmount", 10, 1, 200);
        BUILDER.pop();

        BUILDER.push("decay");
        DECAY_ENABLED = BUILDER.comment(
                "Mirror the vanilla daily gossip decay on the unified global reputation:",
                "major_negative -10/day value, minor_negative -20, minor_positive -1,",
                "major_positive 0, trading -2. Values shrinking below |2| are discarded.")
                .define("decayEnabled", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private Config() {
    }
}
