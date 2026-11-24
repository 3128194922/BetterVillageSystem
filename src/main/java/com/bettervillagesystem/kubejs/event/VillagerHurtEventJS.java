package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * BetterVillageEvents.villagerHurt —— 村民受到伤害（信息事件）。
 */
public class VillagerHurtEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final Villager villager;
    private final DamageSource source;
    private final float amount;

    public VillagerHurtEventJS(ServerLevel level, Villager villager, DamageSource source, float amount) {
        super(level.getServer());
        this.level = level;
        this.villager = villager;
        this.source = source;
        this.amount = amount;
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
    public Player getAttacker() {
        return source.getEntity() instanceof Player p ? p : null;
    }

    public float getAmount() {
        return amount;
    }
}
