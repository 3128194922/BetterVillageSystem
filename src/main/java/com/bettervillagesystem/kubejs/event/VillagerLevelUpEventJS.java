package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;

/**
 * BetterVillageEvents.villagerLevelUp —— 村民职业等级提升。
 */
public class VillagerLevelUpEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final Villager villager;
    private final int newLevel;

    public VillagerLevelUpEventJS(ServerLevel level, Villager villager, int newLevel) {
        super(level.getServer());
        this.level = level;
        this.villager = villager;
        this.newLevel = newLevel;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Villager getVillager() {
        return villager;
    }

    /** 1=新手 2=学徒 3=老手 4=专家 5=大师。 */
    public int getNewLevel() {
        return newLevel;
    }
}
