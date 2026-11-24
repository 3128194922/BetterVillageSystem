package com.bettervillagesystem.mixin;

import com.bettervillagesystem.reputation.ReputationManager;
import net.minecraft.world.entity.animal.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 铁傀儡声望联动：低声望玩家靠近村庄时，自然生成的铁傀儡会转为敌对。
 * （原版铁傀儡只对攻击过自己的玩家愤怒，与声望无关。）
 */
@Mixin(IronGolem.class)
public abstract class IronGolemMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void bvs$golemAggroScan(CallbackInfo ci) {
        ReputationManager.golemAggroScan((IronGolem) (Object) this);
    }
}
