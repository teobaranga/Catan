package com.mygdx.catan.TradeAndTransaction;

import com.mygdx.catan.SessionManager;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.session.Session;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.ResourceKind;

/**
 * Created by amandaivey on 2/26/17.
 */
public class TradeManager {

    private TransactionManager transactionManager;
    private SessionManager sessionManager;

    public TradeManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    //TODO
    public void maritimeTrade(ResourceKind desired, ResourceKind owned, Player currentP) {
        ResourceMap rmDesired = new ResourceMap();
        ResourceMap rmOwned = new ResourceMap();
        int tradeRatio = 0;
        
        switch (desired) {
            case WOOD: desired.equals(ResourceKind.WOOD);
                tradeRatio = currentP.getHighestHarbourLevel(ResourceKind.WOOD);
            case WOOL: desired.equals(ResourceKind.WOOL);
                tradeRatio = currentP.getHighestHarbourLevel(ResourceKind.WOOL);
            case ORE: desired.equals(ResourceKind.ORE);
                tradeRatio = currentP.getHighestHarbourLevel(ResourceKind.ORE);
            case CLOTH: desired.equals(ResourceKind.CLOTH);
                tradeRatio = currentP.getHighestHarbourLevel(ResourceKind.CLOTH);
            case GRAIN: desired.equals(ResourceKind.GRAIN);
                tradeRatio = currentP.getHighestHarbourLevel(ResourceKind.GRAIN);
            case BRICK: desired.equals(ResourceKind.BRICK);
                tradeRatio = currentP.getHighestHarbourLevel(ResourceKind.BRICK);
            case PAPER: desired.equals(ResourceKind.PAPER);
                tradeRatio = currentP.getHighestHarbourLevel(ResourceKind.PAPER);
        }

        rmDesired.put(desired, 1);
        rmOwned.put(owned, tradeRatio);
        
        transactionManager.payPlayerToBank(currentP, rmOwned);
        transactionManager.payBankToPlayer(currentP, rmDesired);
    }
}