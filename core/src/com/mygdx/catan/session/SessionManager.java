package com.mygdx.catan.session;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.PlayerColor;

import java.util.ArrayList;

public class SessionManager {

    private static SessionManager instance;
    private Session aSession;

    // FIXME SessionScreens must be in SessionController
    private ArrayList<SessionScreen> sessionScreens = new ArrayList<>();

    public Session getSession() { return this.aSession; }

    // TODO: change this to fit design, so far this is only placeholder!
    private SessionManager() {
        aSession = new Session();
    }

    public static SessionManager getInstance() {
        if (instance == null)
            instance = new SessionManager();
        return instance;
    }
    
    public Player getCurrentPlayer() {
        return Player.newInstance(new Account("dummy", "dummy"), PlayerColor.ORANGE);
    }

    public Player[] getPlayers() {
        return aSession.getPlayers();
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
