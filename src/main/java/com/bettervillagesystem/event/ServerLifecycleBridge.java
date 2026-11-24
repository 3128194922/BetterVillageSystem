package com.bettervillagesystem.event;

import com.bettervillagesystem.BetterVillageSystem;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 服务器生命周期日志。
 */
@Mod.EventBusSubscriber(modid = BetterVillageSystem.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerLifecycleBridge {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        BetterVillageSystem.LOGGER.info(
                "[BetterVillageSystem] reputation is unified per player across all villages; KubeJS events: {}",
                com.bettervillagesystem.bridge.BetterVillageHooks.bridge != null ? "ENABLED" : "disabled (KubeJS absent)");
    }

    private ServerLifecycleBridge() {
    }
}
