package com.bettervillagesystem.bridge;

import com.bettervillagesystem.reputation.ReputationChangeContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * 核心 ↔ KubeJS 桥接。核心代码只依赖本接口；
 * KubeJS 存在时由 {@code BetterVillageKubeJSPlugin} 在初始化时注入实现，
 * 不存在时保持 null —— 所有方法调用点都必须判空，此时回退为纯原版+统一声望行为。
 */
public final class BetterVillageHooks {
    public interface Bridge {
        /** @return true 表示取消（不写入声望）。 */
        boolean reputationChange(ReputationChangeContext ctx);

        /** 村庄即将召唤铁傀儡。@return true 表示取消召唤。 */
        boolean golemSummon(ServerLevel level, Villager villager);

        /** 村民向英雄玩家送礼。@return true 表示取消送礼。 */
        boolean villagerGift(ServerLevel level, Villager villager, Player hero);

        void trade(ServerLevel level, Player player, AbstractVillager trader, MerchantOffer offer);
        void villagerHurt(ServerLevel level, Villager villager, DamageSource source, float amount);
        void villagerKilled(ServerLevel level, Villager villager, DamageSource source, @Nullable Player killer);
        void zombieVillagerCured(ServerLevel level, Villager villager, @Nullable Player player);
        void golemKilled(ServerLevel level, IronGolem golem, DamageSource source, @Nullable Player killer);
        void golemHurt(ServerLevel level, IronGolem golem, DamageSource source, @Nullable Player attacker, float amount);

        void raidStart(ServerLevel level, Raid raid);
        void raidVictory(ServerLevel level, Raid raid);
        void raidDefeat(ServerLevel level, Raid raid);

        void villagerLevelUp(ServerLevel level, Villager villager, int newLevel);
        void villagerRestock(ServerLevel level, Villager villager);

        /** 每日声望衰减，changes 为 类型id→变化量。 */
        void reputationDecay(MinecraftServer server, UUID playerId, Map<String, Integer> changes);
    }

    public static volatile Bridge bridge = null;

    private BetterVillageHooks() {
    }
}
