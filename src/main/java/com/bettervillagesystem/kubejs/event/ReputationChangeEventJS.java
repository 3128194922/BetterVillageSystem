package com.bettervillagesystem.kubejs.event;

import com.bettervillagesystem.reputation.ReputationChangeContext;
import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * BetterVillageEvents.reputationChange —— 声望写入前的中心事件。
 * 可 {@code event.cancel()}（不写入），可修改 {@code amount} 与 {@code gossipType}。
 */
public class ReputationChangeEventJS extends ServerEventJS {
    private final ReputationChangeContext ctx;

    public ReputationChangeEventJS(ReputationChangeContext ctx) {
        super(ctx.server);
        this.ctx = ctx;
    }

    /** 触发者；非玩家实体（如袭击村民的僵尸）时为 null。 */
    @Nullable
    public Player getPlayer() {
        return ctx.playerOrNull();
    }

    /** 原始触发实体（可能是玩家或怪物）。 */
    @Nullable
    public Entity getActor() {
        return ctx.actor;
    }

    /** 相关村民；铁傀儡相关事件为 null。 */
    @Nullable
    public Villager getVillager() {
        return ctx.villager;
    }

    /** 变更来源：villager_hurt / villager_killed / zombie_villager_cured / trade / golem_killed / golem_hurt / custom。 */
    public String getChangeType() {
        return ctx.source.id;
    }

    /** 将写入的声望分量 id（major_negative / minor_negative / minor_positive / major_positive / trading）。 */
    public String getGossipType() {
        return ctx.type.id;
    }

    public void setGossipType(String id) {
        for (com.bettervillagesystem.reputation.ReputationType type : com.bettervillagesystem.reputation.ReputationType.VALUES) {
            if (type.id.equals(id) || type.name().equalsIgnoreCase(id)) {
                ctx.type = type;
                return;
            }
        }
        throw new IllegalArgumentException("Unknown reputation type: " + id);
    }

    /** 将写入的分量数值（负分量也传正值）。 */
    public int getAmount() {
        return ctx.amount;
    }

    public void setAmount(int amount) {
        ctx.amount = amount;
    }
}
