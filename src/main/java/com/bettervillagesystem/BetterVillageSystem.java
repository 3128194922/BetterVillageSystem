package com.bettervillagesystem;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * BetterVillageSystem
 *
 * <p>将原版按村民独立存储的声望（gossip）完全接管为全局、按玩家存储的统一声望系统，
 * 并把原版村庄的全部机制（交易、攻击/击杀村民、治愈僵尸村民、村民召唤铁傀儡、
 * 玩家击杀铁傀儡、袭击开始/胜利/失败、村民送礼、村民升级、补货、声望衰减）
 * 回传为 KubeJS 事件，同时提供 VillageAPI 供脚本主动操作。</p>
 *
 * <p>KubeJS 为可选软依赖：未安装 KubeJS 时，统一声望与铁傀儡联动仍生效，
 * 只是事件与脚本 API 不可用。KubeJS 桥接类通过 kubejs.plugins.txt 懒加载。</p>
 */
@Mod(BetterVillageSystem.MODID)
public class BetterVillageSystem {
    public static final String MODID = "bettervillagesystem";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterVillageSystem() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.register(com.bettervillagesystem.event.ForgeEventBridge.class);
        bus.register(com.bettervillagesystem.event.ServerLifecycleBridge.class);
        LOGGER.info("[BetterVillageSystem] unified village reputation system loaded");
    }
}
