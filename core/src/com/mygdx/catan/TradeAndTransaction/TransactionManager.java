package com.mygdx.catan.TradeAndTransaction;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.session.SessionController;

/**
 * The transactionManager.
 */
public class TransactionManager {
    private SessionController sessionController;

    /**
     * Adds resources to the player resourceMap.
     *
     * @param p    The Player who is involved in the trade.
     * @param cost The ResourceMap involved in the Trade
     */
    public void payPlayerToBank(Player p, ResourceMap cost) {
        p.removeResources(cost);
        sessionController.add(cost);
    }

    /**
     * Adds resources from the player resourceMap.
     *
     * @param p    The Player who is involved in the trade.
     * @param cost The ResourceMap involved in the Trade
     */
    public void payBankToPlayer(Player p, ResourceMap cost) {
        sessionController.remove(cost);
        p.addResources(cost);
    }
}
