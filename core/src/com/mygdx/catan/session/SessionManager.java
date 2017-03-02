package com.mygdx.catan.session;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;

import java.util.ArrayList;

public class SessionManager {
    private Session aSession;
    
    //FIXME SessionScreens must be in SessionController
    private ArrayList<SessionScreen> sessionScreens = new ArrayList<SessionScreen>();

    public Session getSession() { return this.aSession; }

    //TODO: change this to fit design, so far this is only placeholder!
    public SessionManager(int numberOfPlayers) {
        aSession = new Session();
    }

    public Player[] getPlayers() {
        return aSession.getPlayers();
    }


    /**
     *  Returns the current value of the yellow dice.
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

    /**
     * Adds resources to the Bank
     *
     * @param cost resources to be added to the bank
     */
    public void addToBank(ResourceMap cost){
        aSession.add(cost);
    }

    /**
     *
     * Remove resources from the bank.
     *
     * @param cost The resources to be removed from the bank
     */
    public void removeFromBank(ResourceMap cost){
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
