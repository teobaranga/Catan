package com.mygdx.catan.TradeAndTransaction;

import com.mygdx.catan.player.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.session.SessionManager;

/**
 * The transactionManager.
 */
public class TransactionManager {

    private SessionManager sessionManager;

    private static TransactionManager instance;

    private TransactionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public static TransactionManager getInstance(SessionManager sessionManager) {
        if (instance == null)
            instance = new TransactionManager(sessionManager);
        return instance;
    }

    /**
     * Adds resources to the player resourceMap.
     *
     * @param p    The Player who is involved in the trade.
     * @param cost The ResourceMap involved in the Trade
     */
    public void payPlayerToBank(Player p, ResourceMap cost) {
        p.removeResources(cost);
        sessionManager.addToBank(cost);
    }

    /**
     * Adds resources from the player resourceMap.
     *
     * @param p    The Player who is involved in the trade.
     * @param cost The ResourceMap involved in the Trade
     */
    public void payBankToPlayer(Player p, ResourceMap cost) {
        sessionManager.removeFromBank(cost);
        p.addResources(cost);
    }
}
