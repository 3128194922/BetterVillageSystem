package com.bettervillagesystem.reputation;

import com.bettervillagesystem.Config;
import com.bettervillagesystem.bridge.BetterVillageHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 统一声望的核心管理器：
 * - 读取：{@link #getReputation(Player)}（原版 Villager#getPlayerReputation 被 Mixin 重定向到这里）
 * - 写入：原版声望事件被 Mixin 拦截后经 {@link #requestChange} 走
 *   KubeJS 事件（可取消/改值）→ 全局存储 → 联动铁傀儡。
 */
public final class ReputationManager {
    /** 每日衰减扫描周期（600 tick 扫一次，按 24000 tick 为一天结算）。 */
    private static final int DECAY_SWEEP_INTERVAL = 600;
    private static final long VANILLA_DECAY_PERIOD = 24000L;

    /** 见证者去重：key = 源+行为者，value = 上次应用的游戏刻。 */
    private static final Map<String, Long> WITNESS_DEDUPE = new HashMap<>();

    private ReputationManager() {
    }

    // ------------------------------------------------------------------
    // 读取
    // ------------------------------------------------------------------

    /** 玩家当前总声望（Σ 分量×权重），所有村庄共用这一份。 */
    public static int getReputation(Player player) {
        if (player.level().isClientSide) {
            return 0;
        }
        PlayerReputation rep = GlobalReputationData.get(player.getServer()).peek(player.getUUID());
        return rep == null ? 0 : rep.weightedTotal();
    }

    public static int getReputationValue(MinecraftServer server, UUID playerId, ReputationType type) {
        PlayerReputation rep = GlobalReputationData.get(server).peek(playerId);
        return rep == null ? 0 : rep.get(type);
    }

    public static Map<UUID, PlayerReputation> allReputations(MinecraftServer server) {
        return GlobalReputationData.get(server).all();
    }

    // ------------------------------------------------------------------
    // 原版声望事件入口（Mixin 调用）
    // ------------------------------------------------------------------

    /**
     * 原版 {@code Villager#onReputationEventFrom} 的替代逻辑。
     * 完全接管：不再写入村民自己的 gossip 容器，统一写入全局声望。
     */
    public static void handleVanillaReputationEvent(Villager villager, ReputationEventType eventType, Entity actor) {
        MinecraftServer server = villager.getServer();
        if (server == null || villager.level().isClientSide) {
            return;
        }
        if (eventType == ReputationEventType.VILLAGER_HURT) {
            requestChange(server, actor, villager, ReputationChangeContext.Source.VILLAGER_HURT,
                    ReputationType.MINOR_NEGATIVE, 25, false);
        } else if (eventType == ReputationEventType.VILLAGER_KILLED) {
            // 多个见证村民都会分发一次同刻事件，去重后只应用一份。
            requestChange(server, actor, villager, ReputationChangeContext.Source.VILLAGER_KILLED,
                    ReputationType.MAJOR_NEGATIVE, 25, true);
        } else if (eventType == ReputationEventType.ZOMBIE_VILLAGER_CURED) {
            BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
            if (bridge != null && villager.level() instanceof ServerLevel level) {
                bridge.zombieVillagerCured(level, villager, ctxPlayer(actor));
            }
            requestChange(server, actor, villager, ReputationChangeContext.Source.ZOMBIE_VILLAGER_CURED,
                    ReputationType.MAJOR_POSITIVE, 20, false);
            requestChange(server, actor, villager, ReputationChangeContext.Source.ZOMBIE_VILLAGER_CURED,
                    ReputationType.MINOR_POSITIVE, 25, false);
        } else if (eventType == ReputationEventType.TRADE) {
            requestChange(server, actor, villager, ReputationChangeContext.Source.TRADE,
                    ReputationType.TRADING, 2, false);
        } else if (eventType == ReputationEventType.GOLEM_KILLED) {
            requestChange(server, actor, villager, ReputationChangeContext.Source.GOLEM_KILLED,
                    ReputationType.MAJOR_NEGATIVE, 25, false);
        }
    }

    /**
     * 声望变更主流程：KubeJS 事件（可取消/修改）→ 仅玩家行为写入全局存储 →
     * 触发铁傀儡警觉。非玩家行为者（如袭击村民的僵尸）只回传事件，不写入存储。
     *
     * @return true 表示最终写入了声望。
     */
    public static boolean requestChange(MinecraftServer server, @Nullable Entity actor, @Nullable Villager villager,
                                        ReputationChangeContext.Source source, ReputationType defaultType,
                                        int defaultAmount, boolean dedupeWitnesses) {
        if (dedupeWitnesses) {
            long now = server.overworld().getGameTime();
            String key = source.id + ":" + (actor == null ? "?" : actor.getStringUUID());
            Long last = WITNESS_DEDUPE.put(key, now);
            if (last != null && last == now) {
                return false;
            }
            if (WITNESS_DEDUPE.size() > 256) {
                WITNESS_DEDUPE.clear();
            }
        }

        ReputationChangeContext ctx = new ReputationChangeContext(server, actor, villager, source, defaultType, defaultAmount);
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (bridge != null && bridge.reputationChange(ctx)) {
            return false;
        }

        UUID playerUuid = ctx.playerUuidOrNull();
        if (playerUuid == null) {
            return false;
        }

        GlobalReputationData data = GlobalReputationData.get(server);
        PlayerReputation rep = data.getOrCreate(playerUuid);
        boolean changed = rep.add(ctx.type, ctx.amount);
        data.cleanup(playerUuid);
        if (changed) {
            data.setDirty();
            if (ctx.actor instanceof ServerPlayer sp && sp.level() instanceof ServerLevel sl) {
                maybeAlertGolems(sl, sp, rep.weightedTotal());
            }
        }
        return changed;
    }

    // ------------------------------------------------------------------
    // 铁傀儡联动
    // ------------------------------------------------------------------

    /** 声望落入敌视阈值时立即警觉附近铁傀儡（若配置开启）。 */
    private static void maybeAlertGolems(ServerLevel level, ServerPlayer player, int reputation) {
        if (!Config.ALERT_GOLEMS_ON_DROP.get() || !Config.GOLEM_AGGRO_ENABLED.get()) {
            return;
        }
        if (reputation <= Config.GOLEM_AGGRO_THRESHOLD.get()) {
            alertGolems(level, player);
        }
    }

    /** 使范围内非玩家建造的铁傀儡对玩家愤怒（不覆盖已有的愤怒目标）。 */
    public static void alertGolems(ServerLevel level, Player player) {
        double range = Config.GOLEM_AGGRO_RANGE.get();
        for (IronGolem golem : level.getEntitiesOfClass(IronGolem.class,
                player.getBoundingBox().inflate(range, range / 2.0, range))) {
            angerAt(golem, player);
        }
    }

    public static void angerAt(IronGolem golem, Player player) {
        if (golem.level().isClientSide || golem.isPlayerCreated()) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        UUID current = golem.getPersistentAngerTarget();
        if (current != null && !current.equals(player.getUUID())) {
            return;
        }
        if (current == null) {
            golem.setPersistentAngerTarget(player.getUUID());
            golem.startPersistentAngerTimer();
            golem.level().playSound(null, golem.blockPosition(),
                    SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.HOSTILE, 2.0F, 0.8F);
        }
    }

    /**
     * 由 IronGolemMixin#aiStep 周期调用的低频扫描：让自然生成的铁傀儡
     * 对低于阈值的玩家保持敌视（原版铁傀儡与声望无关）。
     */
    public static void golemAggroScan(IronGolem golem) {
        if (!Config.GOLEM_AGGRO_ENABLED.get() || golem.level().isClientSide || golem.isPlayerCreated()) {
            return;
        }
        if ((golem.tickCount + golem.getId()) % 100 != 0) {
            return;
        }
        if (golem.getPersistentAngerTarget() != null) {
            return;
        }
        int threshold = Config.GOLEM_AGGRO_THRESHOLD.get();
        double range = Config.GOLEM_AGGRO_RANGE.get();
        AABB box = golem.getBoundingBox().inflate(range, range / 2.0, range);
        for (Player player : golem.level().getEntitiesOfClass(Player.class, box, p -> !p.isCreative() && !p.isSpectator())) {
            if (getReputation(player) <= threshold) {
                angerAt(golem, player);
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // 铁傀儡击杀/受伤惩罚（Forge 桥调用，原版从未触发的 golem_killed 补全）
    // ------------------------------------------------------------------

    public static void onGolemKilledByPlayer(ServerLevel level, IronGolem golem, @Nullable Player killer) {
        if (!Config.GOLEM_KILL_PENALTY_ENABLED.get() || killer == null) {
            return;
        }
        requestChange(level.getServer(), killer, null, ReputationChangeContext.Source.GOLEM_KILLED,
                ReputationType.MAJOR_NEGATIVE, Config.GOLEM_KILL_REP_AMOUNT.get(), false);
    }

    public static void onGolemHurtByPlayer(ServerLevel level, IronGolem golem, @Nullable Player attacker, float amount) {
        if (!Config.GOLEM_HURT_PENALTY_ENABLED.get() || attacker == null || amount <= 0) {
            return;
        }
        requestChange(level.getServer(), attacker, null, ReputationChangeContext.Source.GOLEM_HURT,
                ReputationType.MINOR_NEGATIVE, Config.GOLEM_HURT_REP_AMOUNT.get(), false);
    }

    // ------------------------------------------------------------------
    // 每日衰减（镜像原版 gossip 每日衰减）
    // ------------------------------------------------------------------

    private static long sweepCounter;

    public static void serverTick(MinecraftServer server) {
        if (!Config.DECAY_ENABLED.get()) {
            return;
        }
        if (++sweepCounter % DECAY_SWEEP_INTERVAL != 0) {
            return;
        }
        long now = server.overworld().getGameTime();
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        GlobalReputationData data = GlobalReputationData.get(server);
        boolean dirty = false;
        Iterator<Map.Entry<UUID, PlayerReputation>> it = data.all().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PlayerReputation> entry = it.next();
            PlayerReputation rep = entry.getValue();
            if (rep.lastDecayGameTime == 0L) {
                rep.lastDecayGameTime = now;
                dirty = true;
                continue;
            }
            if (now - rep.lastDecayGameTime < VANILLA_DECAY_PERIOD) {
                continue;
            }
            Map<ReputationType, Integer> changes = rep.decayOneDay();
            rep.lastDecayGameTime = now;
            dirty = true;
            if (!changes.isEmpty() && bridge != null) {
                Map<String, Integer> byId = new HashMap<>();
                changes.forEach((type, delta) -> byId.put(type.id, delta));
                bridge.reputationDecay(server, entry.getKey(), byId);
            }
        }
        if (dirty) {
            data.setDirty();
        }
    }

    // ------------------------------------------------------------------
    // 供 VillageAPI 使用的写操作
    // ------------------------------------------------------------------

    /** JS/模组主动添加声望，同样经过可取消的 KubeJS 事件。 */
    public static boolean apiAdd(MinecraftServer server, UUID playerId, ReputationType type, int amount) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        return requestChange(server, player, null, ReputationChangeContext.Source.CUSTOM, type, amount, false);
    }

    /** 直接覆写分量（绕过事件）。 */
    public static void apiSet(MinecraftServer server, UUID playerId, ReputationType type, int value) {
        GlobalReputationData data = GlobalReputationData.get(server);
        data.getOrCreate(playerId).setValue(type, value);
        data.cleanup(playerId);
        data.setDirty();
    }

    public static void apiClear(MinecraftServer server, UUID playerId) {
        GlobalReputationData data = GlobalReputationData.get(server);
        PlayerReputation rep = data.peek(playerId);
        if (rep != null) {
            for (ReputationType type : ReputationType.VALUES) {
                rep.remove(type);
            }
            data.cleanup(playerId);
            data.setDirty();
        }
    }

    public static int clampFor(ReputationType type, int value) {
        return Math.max(-type.max, Math.min(type.max, value));
    }

    @Nullable
    private static Player ctxPlayer(@Nullable Entity actor) {
        return actor instanceof Player p ? p : null;
    }

    /** 声望的可读描述（调试用）。 */
    public static Component describe(Player player) {
        int rep = getReputation(player);
        return Component.literal("村庄声望: " + rep).withStyle(
                rep < 0 ? ChatFormatting.RED : rep > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }
}
