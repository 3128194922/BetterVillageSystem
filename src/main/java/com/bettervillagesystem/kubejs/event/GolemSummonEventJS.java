package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;

/**
 * BetterVillageEvents.golemSummon —— 村庄村民即将召唤铁傀儡（可取消）。
 */
public class GolemSummonEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final Villager villager;

    public GolemSummonEventJS(ServerLevel level, Villager villager) {
        super(level.getServer());
        this.level = level;
        this.villager = villager;
    }

    public ServerLevel getLevel() {
        return level;
    }

    /** 发起召唤的村民（村庄里满足条件的 5 名村民之一）。 */
    public Villager getVillager() {
        return villager;
    }

    public BlockPos getPos() {
        return villager.blockPosition();
    }
}
