package com.bettervillagesystem.mixin;

import com.bettervillagesystem.bridge.BetterVillageHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 村民向“村庄英雄”送礼行为：回传可取消事件（取消则不送礼）。
 */
@Mixin(GiveGiftToHero.class)
public abstract class GiveGiftToHeroMixin {

    @Inject(method = "throwGift", at = @At("HEAD"), cancellable = true)
    private void bvs$villagerGift(Villager villager, LivingEntity hero, CallbackInfo ci) {
        BetterVillageHooks.Bridge bridge = BetterVillageHooks.bridge;
        if (bridge != null && hero instanceof Player player
                && !villager.level().isClientSide
                && villager.level() instanceof ServerLevel level) {
            if (bridge.villagerGift(level, villager, player)) {
                ci.cancel();
            }
        }
    }
}
