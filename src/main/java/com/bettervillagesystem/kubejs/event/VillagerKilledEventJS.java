package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * BetterVillageEvents.villagerKilled —— 村民被杀（信息事件）。
 */
public class VillagerKilledEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final Villager villager;
    private final DamageSource source;
    @Nullable
    private final Player killer;

    public VillagerKilledEventJS(ServerLevel level, Villager villager, DamageSource source, @Nullable Player killer) {
        super(level.getServer());
        this.level = level;
        this.villager = villager;
        this.source = source;
        this.killer = killer;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Villager getVillager() {
        return villager;
    }

    public DamageSource getSource() {
        return source;
    }

    @Nullable
    public Player getKiller() {
        return killer;
    }
}
