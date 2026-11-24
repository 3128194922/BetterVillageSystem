package com.bettervillagesystem.kubejs;

import com.bettervillagesystem.bridge.BetterVillageHooks;
import com.bettervillagesystem.reputation.ReputationChangeContext;
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
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * KubeJS 插件入口（通过 kubejs.plugins.txt 懒加载，KubeJS 不在时永不加载）。
 * 注入 {@link BetterVillageHooks.Bridge}，把核心钩子转换为 KubeJS 事件。
 */
public class BetterVillageKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        BetterVillageHooks.bridge = new BridgeImpl();
    }

    @Override
    public void registerEvents() {
        BetterVillageEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("VillageAPI", VillageAPI.class);
    }

    private static class BridgeImpl implements BetterVillageHooks.Bridge {

        @Override
        public boolean reputationChange(ReputationChangeContext ctx) {
            if (!BetterVillageEvents.REPUTATION_CHANGE.hasListeners()) {
                return false;
            }
            return BetterVillageEvents.REPUTATION_CHANGE
                    .post(ScriptType.SERVER, new ReputationChangeEventJS(ctx))
                    .interruptFalse();
        }

        @Override
        public boolean golemSummon(ServerLevel level, Villager villager) {
            if (!BetterVillageEvents.GOLEM_SUMMON.hasListeners()) {
                return false;
            }
            return BetterVillageEvents.GOLEM_SUMMON
                    .post(ScriptType.SERVER, new GolemSummonEventJS(level, villager))
                    .interruptFalse();
        }

        @Override
        public boolean villagerGift(ServerLevel level, Villager villager, Player hero) {
            if (!BetterVillageEvents.VILLAGER_GIFT.hasListeners()) {
                return false;
            }
            return BetterVillageEvents.VILLAGER_GIFT
                    .post(ScriptType.SERVER, new VillagerGiftEventJS(level, villager, hero))
                    .interruptFalse();
        }

        @Override
        public void trade(ServerLevel level, Player player, AbstractVillager trader, MerchantOffer offer) {
            if (BetterVillageEvents.TRADE.hasListeners()) {
                BetterVillageEvents.TRADE.post(ScriptType.SERVER,
                        new TradeEventJS((net.minecraft.server.level.ServerPlayer) player, trader, offer));
            }
        }

        @Override
        public void villagerHurt(ServerLevel level, Villager villager, DamageSource source, float amount) {
            if (BetterVillageEvents.VILLAGER_HURT.hasListeners()) {
                BetterVillageEvents.VILLAGER_HURT.post(ScriptType.SERVER,
                        new VillagerHurtEventJS(level, villager, source, amount));
            }
        }

        @Override
        public void villagerKilled(ServerLevel level, Villager villager, DamageSource source, @Nullable Player killer) {
            if (BetterVillageEvents.VILLAGER_KILLED.hasListeners()) {
                BetterVillageEvents.VILLAGER_KILLED.post(ScriptType.SERVER,
                        new VillagerKilledEventJS(level, villager, source, killer));
            }
        }

        @Override
        public void zombieVillagerCured(ServerLevel level, Villager villager, @Nullable Player player) {
            if (BetterVillageEvents.ZOMBIE_VILLAGER_CURED.hasListeners()) {
                BetterVillageEvents.ZOMBIE_VILLAGER_CURED.post(ScriptType.SERVER,
                        new ZombieVillagerCuredEventJS(level, villager, player));
            }
        }

        @Override
        public void golemKilled(ServerLevel level, IronGolem golem, DamageSource source, @Nullable Player killer) {
            if (BetterVillageEvents.GOLEM_KILLED.hasListeners()) {
                BetterVillageEvents.GOLEM_KILLED.post(ScriptType.SERVER,
                        new GolemKilledEventJS(level, golem, source, killer));
            }
        }

        @Override
        public void golemHurt(ServerLevel level, IronGolem golem, DamageSource source, @Nullable Player attacker, float amount) {
            if (BetterVillageEvents.GOLEM_HURT.hasListeners()) {
                BetterVillageEvents.GOLEM_HURT.post(ScriptType.SERVER,
                        new GolemHurtEventJS(level, golem, source, amount));
            }
        }

        @Override
        public void raidStart(ServerLevel level, Raid raid) {
            if (BetterVillageEvents.RAID_START.hasListeners()) {
                BetterVillageEvents.RAID_START.post(ScriptType.SERVER, new RaidEventJS(level, raid));
            }
        }

        @Override
        public void raidVictory(ServerLevel level, Raid raid) {
            if (BetterVillageEvents.RAID_VICTORY.hasListeners()) {
                BetterVillageEvents.RAID_VICTORY.post(ScriptType.SERVER, new RaidEventJS(level, raid));
            }
        }

        @Override
        public void raidDefeat(ServerLevel level, Raid raid) {
            if (BetterVillageEvents.RAID_DEFEAT.hasListeners()) {
                BetterVillageEvents.RAID_DEFEAT.post(ScriptType.SERVER, new RaidEventJS(level, raid));
            }
        }

        @Override
        public void villagerLevelUp(ServerLevel level, Villager villager, int newLevel) {
            if (BetterVillageEvents.VILLAGER_LEVEL_UP.hasListeners()) {
                BetterVillageEvents.VILLAGER_LEVEL_UP.post(ScriptType.SERVER,
                        new VillagerLevelUpEventJS(level, villager, newLevel));
            }
        }

        @Override
        public void villagerRestock(ServerLevel level, Villager villager) {
            if (BetterVillageEvents.VILLAGER_RESTOCK.hasListeners()) {
                BetterVillageEvents.VILLAGER_RESTOCK.post(ScriptType.SERVER,
                        new VillagerRestockEventJS(level, villager));
            }
        }

        @Override
        public void reputationDecay(MinecraftServer server, UUID playerId, Map<String, Integer> changes) {
            if (BetterVillageEvents.REPUTATION_DECAY.hasListeners()) {
                BetterVillageEvents.REPUTATION_DECAY.post(ScriptType.SERVER,
                        new ReputationDecayEventJS(server, playerId, changes));
            }
        }
    }
}
