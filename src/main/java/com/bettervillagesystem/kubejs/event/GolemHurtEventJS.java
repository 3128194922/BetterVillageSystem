package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * BetterVillageEvents.golemHurt —— 铁傀儡受伤（信息事件）。
 */
public class GolemHurtEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final IronGolem golem;
    private final DamageSource source;
    private final float amount;

    public GolemHurtEventJS(ServerLevel level, IronGolem golem, DamageSource source, float amount) {
        super(level.getServer());
        this.level = level;
        this.golem = golem;
        this.source = source;
        this.amount = amount;
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
    public Player getAttacker() {
        return source.getEntity() instanceof Player p ? p : null;
    }

    public float getAmount() {
        return amount;
    }
}
