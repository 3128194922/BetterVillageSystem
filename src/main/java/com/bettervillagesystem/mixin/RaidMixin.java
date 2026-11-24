package com.bettervillagesystem.mixin;

import com.bettervillagesystem.bridge.BetterVillageHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 袭击状态监控：在 {@code Raid#tick} 的每个返回点用公开的
 * isVictory/isLoss 检测状态跳变，回传袭击胜利 / 失败事件（原版无任何事件）。
 */
@Mixin(Raid.class)
public abstract class RaidMixin {
    @Unique
    private boolean bvs$victoryFired;

    @Unique
    private boolean bvs$lossFired;

    @Inject(method = "tick", at = @At("RETURN"))
    private void bvs$raidStatusTransition(CallbackInfo ci) {
        Raid raid = (Raid) (Object) this;
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (bridge == null || !(raid.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!bvs$victoryFired && raid.isVictory()) {
            bvs$victoryFired = true;
            bridge.raidVictory(level, raid);
        } else if (!bvs$lossFired && raid.isLoss()) {
            bvs$lossFired = true;
            bridge.raidDefeat(level, raid);
        }
    }
}
