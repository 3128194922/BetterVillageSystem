package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

/**
 * BetterVillageEvents.villagerGift —— 村民向“村庄英雄”送礼（可取消）。
 */
public class VillagerGiftEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final Villager villager;
    private final Player hero;

    public VillagerGiftEventJS(ServerLevel level, Villager villager, Player hero) {
        super(level.getServer());
        this.level = level;
        this.villager = villager;
        this.hero = hero;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Villager getVillager() {
        return villager;
    }

    public Player getHero() {
        return hero;
    }
}
