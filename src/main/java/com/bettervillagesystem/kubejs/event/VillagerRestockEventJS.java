package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;

/**
 * BetterVillageEvents.villagerRestock —— 村民补货。
 */
public class VillagerRestockEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final Villager villager;

    public VillagerRestockEventJS(ServerLevel level, Villager villager) {
        super(level.getServer());
        this.level = level;
        this.villager = villager;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Villager getVillager() {
        return villager;
    }
}
