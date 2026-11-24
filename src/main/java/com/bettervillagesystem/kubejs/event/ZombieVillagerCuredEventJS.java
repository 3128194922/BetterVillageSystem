package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * BetterVillageEvents.zombieVillagerCured —— 僵尸村民被治愈（信息事件）。
 */
public class ZombieVillagerCuredEventJS extends ServerEventJS {
    private final ServerLevel level;
    private final Villager villager;
    @Nullable
    private final Player player;

    public ZombieVillagerCuredEventJS(ServerLevel level, Villager villager, @Nullable Player player) {
        super(level.getServer());
        this.level = level;
        this.villager = villager;
        this.player = player;
    }

    public ServerLevel getLevel() {
        return level;
    }

    /** 治愈后重生的村民。 */
    public Villager getVillager() {
        return villager;
    }

    /** 完成治愈的玩家。 */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    public BlockPos getPos() {
        return villager.blockPosition();
    }
}
