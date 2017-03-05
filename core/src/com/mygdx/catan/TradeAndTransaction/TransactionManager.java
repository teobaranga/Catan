package com.mygdx.catan.TradeAndTransaction;

import com.mygdx.catan.CatanGame;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.session.Session;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * The transactionManager.
 */
public class TransactionManager {

    private static HashMap<Session, TransactionManager> transactionManagerInstances;

    static {
        transactionManagerInstances = new HashMap<>();
    }

    private Session aSession;

    private TransactionManager(Session session) {
        aSession = session;
    }

    public static TransactionManager getInstance(Session session) {
        if (!transactionManagerInstances.containsKey(session)) {
            if (session == null) {
                ArrayList<Account> accounts = new ArrayList<>();
                accounts.add(CatanGame.account);
                session = Session.newInstance(accounts, GameRules.getGameRulesInstance().getVpToWin());
            }
            transactionManagerInstances.put(session, new TransactionManager(session));
        }
        return transactionManagerInstances.get(session);
    }

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
