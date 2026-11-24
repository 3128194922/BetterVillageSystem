package com.bettervillagesystem.kubejs.event;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * BetterVillageEvents.trade —— 玩家完成一笔交易。
 */
public class TradeEventJS extends ServerEventJS {
    private final ServerPlayer player;
    private final AbstractVillager trader;
    private final MerchantOffer offer;

    public TradeEventJS(ServerPlayer player, AbstractVillager trader, MerchantOffer offer) {
        super(player.getServer());
        this.player = player;
        this.trader = trader;
        this.offer = offer;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public AbstractVillager getTrader() {
        return trader;
    }

    public MerchantOffer getOffer() {
        return offer;
    }

    public ItemStack getItemCostA() {
        return offer.getCostA();
    }

    public ItemStack getItemCostB() {
        return offer.getCostB();
    }

    public ItemStack getResult() {
        return offer.getResult();
    }
}
