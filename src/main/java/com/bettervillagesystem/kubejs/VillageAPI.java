package com.bettervillagesystem.kubejs;

import com.bettervillagesystem.Config;
import com.bettervillagesystem.reputation.GlobalReputationData;
import com.bettervillagesystem.reputation.PlayerReputation;
import com.bettervillagesystem.reputation.ReputationManager;
import com.bettervillagesystem.reputation.ReputationType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * VillageAPI —— 在 KubeJS（server_scripts / startup_scripts）中以
 * {@code VillageAPI.xxx(...)} 直接调用的统一声望操作接口。
 *
 * <pre>
 * VillageAPI.getReputation(event.player)                // 总声望（Σ 分量×权重）
 * VillageAPI.getReputationValue(player, 'trading')      // 单分量原始值
 * VillageAPI.addReputation(player, 'minor_positive', 25)
 * VillageAPI.setReputationValue(player, 'major_negative', 100)
 * VillageAPI.clearReputation(player)
 * VillageAPI.alertIronGolems(player)
 * </pre>
 */
public final class VillageAPI {

    /** 玩家总声望：正数友好、负数敌视，范围约 [-700, +925]。 */
    public static int getReputation(Player player) {
        return ReputationManager.getReputation(player);
    }

    /** 单分量原始值（0 表示无该记忆）。 */
    public static int getReputationValue(Player player, String typeId) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        return ReputationManager.getReputationValue(server, player.getUUID(), parseType(typeId));
    }

    /** 全部分量：{major_negative, minor_negative, minor_positive, major_positive, trading} → 值。 */
    public static Map<String, Integer> getReputationDetail(Player player) {
        MinecraftServer server = player.getServer();
        Map<String, Integer> detail = new LinkedHashMap<>();
        if (server == null) {
            return detail;
        }
        PlayerReputation rep = GlobalReputationData.get(server).peek(player.getUUID());
        for (ReputationType type : ReputationType.VALUES) {
            detail.put(type.id, rep == null ? 0 : rep.get(type));
        }
        return detail;
    }

    /**
     * 添加声望（同走 reputationChange 可取消事件，changeType = 'custom'）。
     *
     * @return true 表示实际写入
     */
    public static boolean addReputation(Player player, String typeId, int amount) {
        MinecraftServer server = player.getServer();
        return server != null
                && ReputationManager.apiAdd(server, player.getUUID(), parseType(typeId), amount);
    }

    /** 直接覆写分量值（绕过事件，仍受 ±max 钳制）。 */
    public static void setReputationValue(Player player, String typeId, int value) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            ReputationManager.apiSet(server, player.getUUID(), parseType(typeId), value);
        }
    }

    /** 清空玩家的全部声望。 */
    public static void clearReputation(Player player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            ReputationManager.apiClear(server, player.getUUID());
        }
    }

    /** 立即使附近（非玩家建造的）铁傀儡对该玩家愤怒。 */
    public static void alertIronGolems(Player player) {
        if (player.level() instanceof ServerLevel level) {
            ReputationManager.alertGolems(level, player);
        }
    }

    /** 当前铁傀儡敌视阈值（总声望 ≤ 该值时敌视）。 */
    public static int getGolemAggroThreshold() {
        return Config.GOLEM_AGGRO_THRESHOLD.get();
    }

    public static boolean isGolemAggroEnabled() {
        return Config.GOLEM_AGGRO_ENABLED.get();
    }

    /** 离线玩家总声望（playerId 为 UUID 字符串）。 */
    public static int getOfflineReputation(MinecraftServer server, String playerId) {
        PlayerReputation rep = GlobalReputationData.get(server).peek(UUID.fromString(playerId));
        return rep == null ? 0 : rep.weightedTotal();
    }

    public static ReputationType parseType(String id) {
        for (ReputationType type : ReputationType.VALUES) {
            if (type.id.equals(id) || type.name().equalsIgnoreCase(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown reputation type: " + id
                + " (valid: major_negative, minor_negative, minor_positive, major_positive, trading)");
    }

    private VillageAPI() {
    }
}
