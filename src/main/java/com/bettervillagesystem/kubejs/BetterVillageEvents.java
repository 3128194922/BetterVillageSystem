package com.bettervillagesystem.kubejs;

import com.bettervillagesystem.kubejs.event.GolemHurtEventJS;
import com.bettervillagesystem.kubejs.event.GolemKilledEventJS;
import com.bettervillagesystem.kubejs.event.GolemSummonEventJS;
import com.bettervillagesystem.kubejs.event.RaidEventJS;
import com.bettervillagesystem.kubejs.event.ReputationChangeEventJS;
import com.bettervillagesystem.kubejs.event.ReputationDecayEventJS;
import com.bettervillagesystem.kubejs.event.TradeEventJS;
import com.bettervillagesystem.kubejs.event.VillagerGiftEventJS;
import com.bettervillagesystem.kubejs.event.VillagerHurtEventJS;
import com.bettervillagesystem.kubejs.event.VillagerKilledEventJS;
import com.bettervillagesystem.kubejs.event.VillagerLevelUpEventJS;
import com.bettervillagesystem.kubejs.event.VillagerRestockEventJS;
import com.bettervillagesystem.kubejs.event.ZombieVillagerCuredEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/**
 * BetterVillageSystem 的 KubeJS 事件组。
 * 在 server_scripts 中监听，例如：
 * <pre>
 * BetterVillageEvents.reputationChange(event => {
 *     if (event.changeType == 'villager_killed') event.cancel()
 * })
 * </pre>
 */
public interface BetterVillageEvents {
    EventGroup GROUP = EventGroup.of("BetterVillageEvents");

    /** 玩家与村民/流浪商人完成一笔交易（信息事件）。 */
    EventHandler TRADE = GROUP.server("trade", () -> TradeEventJS.class);

    /**
     * 任何声望变更写入前（可取消、可修改分量与数值）。
     * changeType: villager_hurt / villager_killed / zombie_villager_cured /
     * trade / golem_killed / golem_hurt / custom
     */
    EventHandler REPUTATION_CHANGE = GROUP.server("reputationChange", () -> ReputationChangeEventJS.class).hasResult();

    /** 村庄村民即将召唤铁傀儡（可取消）。 */
    EventHandler GOLEM_SUMMON = GROUP.server("golemSummon", () -> GolemSummonEventJS.class).hasResult();

    /** 村民向“村庄英雄”送礼（可取消）。 */
    EventHandler VILLAGER_GIFT = GROUP.server("villagerGift", () -> VillagerGiftEventJS.class).hasResult();

    /** 村民受到伤害（信息事件；取消伤害请改用 KubeJS 自带 EntityEvents.hurt）。 */
    EventHandler VILLAGER_HURT = GROUP.server("villagerHurt", () -> VillagerHurtEventJS.class);

    /** 村民被杀（信息事件；声望惩罚走 reputationChange）。 */
    EventHandler VILLAGER_KILLED = GROUP.server("villagerKilled", () -> VillagerKilledEventJS.class);

    /** 僵尸村民被治愈（信息事件）。 */
    EventHandler ZOMBIE_VILLAGER_CURED = GROUP.server("zombieVillagerCured", () -> ZombieVillagerCuredEventJS.class);

    /** 铁傀儡被杀（信息事件 + 声望惩罚）。 */
    EventHandler GOLEM_KILLED = GROUP.server("golemKilled", () -> GolemKilledEventJS.class);

    /** 铁傀儡受伤（信息事件，惩罚默认关闭）。 */
    EventHandler GOLEM_HURT = GROUP.server("golemHurt", () -> GolemHurtEventJS.class);

    /** 袭击开始。 */
    EventHandler RAID_START = GROUP.server("raidStart", () -> RaidEventJS.class);

    /** 袭击胜利（英雄效果发放时刻）。 */
    EventHandler RAID_VICTORY = GROUP.server("raidVictory", () -> RaidEventJS.class);

    /** 袭击失败（村庄被毁）。 */
    EventHandler RAID_DEFEAT = GROUP.server("raidDefeat", () -> RaidEventJS.class);

    /** 村民职业等级提升。 */
    EventHandler VILLAGER_LEVEL_UP = GROUP.server("villagerLevelUp", () -> VillagerLevelUpEventJS.class);

    /** 村民补货。 */
    EventHandler VILLAGER_RESTOCK = GROUP.server("villagerRestock", () -> VillagerRestockEventJS.class);

    /** 全局声望每日衰减（changes: 分量id → 变化量）。 */
    EventHandler REPUTATION_DECAY = GROUP.server("reputationDecay", () -> ReputationDecayEventJS.class);
}
