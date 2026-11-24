package com.bettervillagesystem.mixin;

import com.bettervillagesystem.bridge.BetterVillageHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * 袭击创建监控：{@code Raids#getOrCreateRaid} 返回尚未入表的新袭击时，
 * 回传袭击开始事件。
 */
@Mixin(Raids.class)
public abstract class RaidsMixin {
    @Shadow
    @Final
    private Map<Integer, Raid> raidMap;

    @Inject(method = "getOrCreateRaid", at = @At("TAIL"))
    private void bvs$raidStart(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Raid> cir) {
        Raid raid = cir.getReturnValue();
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (bridge != null && raid != null && !this.raidMap.containsKey(raid.getId())) {
            bridge.raidStart(level, raid);
        }
    }
}
