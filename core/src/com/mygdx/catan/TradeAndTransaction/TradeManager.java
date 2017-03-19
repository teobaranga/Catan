package com.mygdx.catan.TradeAndTransaction;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

public class TradeManager {

    private static TradeManager instance;

    private TransactionManager transactionManager;

    private TradeManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public static TradeManager getInstance(TransactionManager transactionManager) {
        if (instance == null)
            instance = new TradeManager(transactionManager);
        return instance;
    }

    /**
     * Perform domestic trade, which includes maritime trade.
     *
     * @param offer      type of resource that the player is offering
     * @param request    type of resource that the player is requesting
     * @param tradeRatio the number of units of the offered resource necessary to receive one requested resource
     * @param player     the player doing the trade
     */
    public void domesticTrade(ResourceKind offer, ResourceKind request, int tradeRatio, Player player) {
        // Compute the resources to be removed from the player
        final ResourceMap removeResourceMap = new ResourceMap();
        removeResourceMap.put(offer, tradeRatio);

        // Compute the resources to be added to the player
        final ResourceMap addResourceMap = new ResourceMap();
        addResourceMap.put(request, 1);

        transactionManager.payPlayerToBank(player, removeResourceMap);
        transactionManager.payBankToPlayer(player, addResourceMap);
    }

    public void trade(Player player, ResourceMap receive, ResourceMap give) {
        player.removeResources(give);
        player.addResources(receive);
    }
}
