package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * BetterVillageEvents.golemKilled —— 铁傀儡被杀（信息事件 + 可配置声望惩罚）。
 */
public class GolemKilledEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final IronGolem golem;
    private final DamageSource source;
    @Nullable
    private final Player killer;

    public GolemKilledEventJS(ServerLevel level, IronGolem golem, DamageSource source, @Nullable Player killer) {
        super(level.getServer());
        this.level = level;
        this.golem = golem;
        this.source = source;
        this.killer = killer;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public IronGolem getGolem() {
        return golem;
    }

    public DamageSource getSource() {
        return source;
    }

    @Nullable
    public Player getKiller() {
        return killer;
    }
}
