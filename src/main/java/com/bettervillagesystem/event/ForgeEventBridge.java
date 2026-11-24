package com.bettervillagesystem.event;

import com.bettervillagesystem.BetterVillageSystem;
import com.bettervillagesystem.bridge.BetterVillageHooks;
import com.bettervillagesystem.reputation.ReputationManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * Forge 事件 → KubeJS 事件桥：
 * - 交易（Forge 原生 TradeWithVillagerEvent，含流浪商人）；
 * - 村民受伤/被杀、铁傀儡受伤/被杀（LivingHurt/LivingDeath 过滤）；
 * - 服务器 tick 驱动每日声望衰减。
 */
@Mod.EventBusSubscriber(modid = BetterVillageSystem.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEventBridge {

    @SubscribeEvent
    public static void onTrade(TradeWithVillagerEvent event) {
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (bridge == null || !(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        AbstractVillager trader = event.getAbstractVillager();
        bridge.trade(level, player, trader, event.getMerchantOffer());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (event.getEntity().level().isClientSide
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        DamageSource source = event.getSource();
        Player killer = playerOrNull(source.getEntity());
        if (event.getEntity() instanceof IronGolem golem) {
            if (bridge != null) {
                bridge.golemKilled(level, golem, source, killer);
            }
            ReputationManager.onGolemKilledByPlayer(level, golem, killer);
        } else if (event.getEntity() instanceof Villager villager && bridge != null) {
            bridge.villagerKilled(level, villager, source, killer);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (event.getEntity().level().isClientSide
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        DamageSource source = event.getSource();
        Player attacker = playerOrNull(source.getEntity());
        if (event.getEntity() instanceof Villager villager) {
            if (bridge != null) {
                bridge.villagerHurt(level, villager, source, event.getAmount());
            }
        } else if (event.getEntity() instanceof IronGolem golem) {
            if (bridge != null) {
                bridge.golemHurt(level, golem, source, attacker, event.getAmount());
            }
            ReputationManager.onGolemHurtByPlayer(level, golem, attacker, event.getAmount());
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server != null && server.isRunning()) {
            ReputationManager.serverTick(server);
        }
    }

    @Nullable
    private static Player playerOrNull(@Nullable Entity entity) {
        return entity instanceof Player p ? p : null;
    }

    private ForgeEventBridge() {
    }
}
