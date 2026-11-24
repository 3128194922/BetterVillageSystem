package com.bettervillagesystem.reputation;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 一次声望变更请求的完整上下文。
 * 由原版行为（攻击/击杀/治愈/交易）或 JS API 发起，
 * 经 KubeJS 事件（可取消、可改值）后写入全局统一声望。
 */
public class ReputationChangeContext {
    public enum Source {
        /** 村民被打（原版 VILLAGER_HURT，默认 minor_negative +25）。 */
        VILLAGER_HURT("villager_hurt"),
        /** 村民被杀（原版 VILLAGER_KILLED，默认 major_negative +25）。 */
        VILLAGER_KILLED("villager_killed"),
        /** 僵尸村民被治愈（原版 ZOMBIE_VILLAGER_CURED，默认 major_positive +20 与 minor_positive +25）。 */
        ZOMBIE_VILLAGER_CURED("zombie_villager_cured"),
        /** 与村民交易（原版 TRADE，默认 trading +2）。 */
        TRADE("trade"),
        /** 玩家击杀铁傀儡（原版注册了 golem_killed 但从未触发，本模组补全，默认 major_negative +25）。 */
        GOLEM_KILLED("golem_killed"),
        /** 玩家攻击铁傀儡（配置默认关闭，默认 minor_negative +10）。 */
        GOLEM_HURT("golem_hurt"),
        /** JS API 或其他模组主动添加。 */
        CUSTOM("custom");

        public final String id;

        Source(String id) {
            this.id = id;
        }
    }

    public final MinecraftServer server;
    /** 触发声望变化的实体（可能是玩家，也可能是袭击村民的怪物）。 */
    @Nullable
    public final Entity actor;
    /** 与该次变化相关的村民（见证者/被交易者/被治愈者），铁傀儡事件为 null。 */
    @Nullable
    public final Villager villager;
    public final Source source;
    /** 变更写入的声望分量（JS 可修改）。 */
    public ReputationType type;
    /** 变更量，正值提升分量值（负分量如 major_negative 也用正值写入）（JS 可修改）。 */
    public int amount;
    public boolean canceled;

    public ReputationChangeContext(MinecraftServer server, @Nullable Entity actor, @Nullable Villager villager,
                                   Source source, ReputationType type, int amount) {
        this.server = server;
        this.actor = actor;
        this.villager = villager;
        this.source = source;
        this.type = type;
        this.amount = amount;
    }

    /** actor 是玩家时返回之，否则 null（怪物袭击村民时不产生声望变化）。 */
    @Nullable
    public Player playerOrNull() {
        return actor instanceof Player p ? p : null;
    }

    @Nullable
    public UUID playerUuidOrNull() {
        return actor instanceof Player p ? p.getUUID() : null;
    }
}
