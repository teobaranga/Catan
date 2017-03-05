package com.mygdx.catan.session;

import com.mygdx.catan.CatanGame;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.game.Game;
import com.mygdx.catan.game.GameManager;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static SessionManager instance;
    private Session aSession;

    //TODO: change this to fit design, so far this is only placeholder!
    private SessionManager(Session session) {
        aSession = session;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            final Game currentGame = GameManager.getInstance().getCurrentGame();
            Session session;
            if (currentGame == null || currentGame.session == null) {
                // Create a dummy session, for testing purposes
                List<Account> accounts = new ArrayList<>();
                accounts.add(CatanGame.account);
                session = Session.newInstance(accounts, GameRules.getGameRulesInstance().getVpToWin());
            } else {
                session = currentGame.session;
            }
            instance = new SessionManager(session);
        }
        return instance;
    }

    public Session getSession() { return this.aSession; }

    public Player[] getPlayers() {
        return aSession.getPlayers();
    }

    /** Get the current player */
    public Player getCurrentPlayer() {
        return aSession.getPlayers()[aSession.getPlayerIndex()];
    }

    GamePhase getCurrentPhase() {
        return aSession.getCurrentPhase();
    }

    public Player getCurrentPlayerFromColor(PlayerColor c) {
        Player currentP = null;
        for (Player p : getPlayers()) {
            if (p.getColor().equals(c)) {
                currentP = p;
            }
        }
        return currentP;
    }

    /**
     * Returns the current value of the yellow dice.
     *
     * @return yellow dice value
     */
    public int getYellowDice() { return aSession.getYellowDice(); }

    /**
     * Returns the current value of the red dice.
     *
     * @return red dice value
     */
    public int getRedDice() { return aSession.getRedDice(); }


    public void setRedDice(int redDice) {
        aSession.setRedDice(redDice);
    }

    public void setYellowDice(int yellowDice) {
        aSession.setYellowDice(yellowDice);
    }


    /**
     * Adds resources to the Bank
     *
     * @param cost resources to be added to the bank
     */
    public void addToBank(ResourceMap cost) {
        aSession.add(cost);
    }

    /**
     *
     * Remove resources from the bank.
     *
     * @param cost The resources to be removed from the bank
     */
    public void removeFromBank(ResourceMap cost) {
        aSession.remove(cost);
    }

    /**
     * Changes the values in the cost map in case the Bank does not hold enough resources.
     *
     * @param cost initial removal from bank
     * @return updated removal from Bank
     */
    public ResourceMap checkMaxCostForBank(ResourceMap cost) {
        cost = aSession.adjustcost(cost);
        return cost;
    }
}
