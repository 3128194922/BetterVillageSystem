package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;

/**
 * BetterVillageEvents.raidStart / raidVictory / raidDefeat。
 */
public class RaidEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final Raid raid;

    public RaidEventJS(ServerLevel level, Raid raid) {
        super(level.getServer());
        this.level = level;
        this.raid = raid;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Raid getRaid() {
        return raid;
    }

    public int getRaidId() {
        return raid.getId();
    }

    /** 袭击中心（村庄位置附近）。 */
    public BlockPos getCenter() {
        return raid.getCenter();
    }

    public int getBadOmenLevel() {
        return raid.getBadOmenLevel();
    }
}
