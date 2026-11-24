package com.bettervillagesystem.mixin;

import com.bettervillagesystem.bridge.BetterVillageHooks;
import com.bettervillagesystem.reputation.ReputationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 原版村民的声望完全接管：
 * - {@code getPlayerReputation} → 返回全局统一声望（价格折扣随之全村庄通用）；
 * - {@code onReputationEventFrom} → 原版 gossip 写入被整体替换，
 *   改走 KubeJS 可取消事件 + 全局存储；
 * - 召唤铁傀儡/升级/补货 → 回传 KubeJS 事件。
 */
@Mixin(Villager.class)
public abstract class VillagerMixin {

    @Inject(method = "getPlayerReputation", at = @At("HEAD"), cancellable = true)
    private void bvs$globalReputation(Player player, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(ReputationManager.getReputation(player));
    }

    @Inject(method = "onReputationEventFrom", at = @At("HEAD"), cancellable = true)
    private void bvs$takeOverReputationEvent(ReputationEventType eventType, Entity actor, CallbackInfo ci) {
        ReputationManager.handleVanillaReputationEvent((Villager) (Object) this, eventType, actor);
        ci.cancel();
    }

    @Inject(method = "spawnGolemIfNeeded", at = @At("HEAD"), cancellable = true)
    private void bvs$golemSummon(ServerLevel level, long gameTime, int minVillagerCount, CallbackInfo ci) {
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (bridge != null && bridge.golemSummon(level, (Villager) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "increaseMerchantCareer", at = @At("TAIL"))
    private void bvs$villagerLevelUp(CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (bridge != null && villager.level() instanceof ServerLevel level) {
            bridge.villagerLevelUp(level, villager, villager.getVillagerData().getLevel());
        }
    }

    @Inject(method = "restock", at = @At("TAIL"))
    private void bvs$villagerRestock(CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (bridge != null && villager.level() instanceof ServerLevel level) {
            bridge.villagerRestock(level, villager);
        }
    }
}
