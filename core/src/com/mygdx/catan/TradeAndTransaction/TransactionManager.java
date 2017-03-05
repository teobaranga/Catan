package com.mygdx.catan.TradeAndTransaction;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.session.Session;

/**
 * The transactionManager.
 */
public class TransactionManager {

    //session controller has a dependency on transaction manager aka other way around
    private Session aSession;

    private static TransactionManager instance = null;

    public static TransactionManager getInstance() {
        if (instance == null) {
            instance = new TransactionManager();
        }
        return instance;
    }

    //public TransactionManager(Session s) {
        //aSession = s;
    //}

    /**
     * Adds resources to the player resourceMap.
     *
     * @param p    The Player who is involved in the trade.
     * @param cost The ResourceMap involved in the Trade
     */
    public void payPlayerToBank(Player p, ResourceMap cost) {
        //check the logic of this
        p.removeResources(cost);
        aSession.add(cost);
    }

    /**
     * Adds resources from the player resourceMap.
     *
     * @param p    The Player who is involved in the trade.
     * @param cost The ResourceMap involved in the Trade
     */
    public void payBankToPlayer(Player p, ResourceMap cost) {
        //check the logic of this
        aSession.remove(cost);
        p.addResources(cost);
    }
}
