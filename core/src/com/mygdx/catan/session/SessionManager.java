package com.mygdx.catan.session;

import com.mygdx.catan.CatanGame;
import com.mygdx.catan.GameRules;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.PlayerColor;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SessionManager {

    private static HashMap<Session, SessionManager> sessionManagerInstances;

    static {
        sessionManagerInstances = new HashMap<>();
    }

    /**
     * The session associated with this session manager.
     * Must be private at all times.
     */
    private final Session aSession;

    /**
     * Pair of player index to dice roll.
     * Used in the initialization phase when we need to keep track of the
     * player with the highest roll.
     * Used by the server only.
     */
    private MutablePair<Integer, Integer> diceRolls;

    /** Index of the last player to roll the dice */
    private int diceRollPlayerIndex;

    private SessionManager(Session session) {
        aSession = session;
        diceRolls = new MutablePair<>(0, 0);
        resetDiceRoll();
        resetBarbarianPosition();
    }

    /**
     * Get the session manager instance associated with a specific session.
     * If the session is null, then a dummy session will be created.
     */
    public static SessionManager getInstance(Session session) {
        if (!sessionManagerInstances.containsKey(session)) {
            if (session == null) {
                // Create a dummy session, for testing purposes
                List<Account> accounts = new ArrayList<>();
                accounts.add(CatanGame.account);
                session = Session.newInstance(accounts, GameRules.getGameRulesInstance().getVpToWin());
            }
            sessionManagerInstances.put(session, new SessionManager(session));
        }
        return sessionManagerInstances.get(session);
    }

    /** Get the session associated with this session manager */
    public Session getSession() {
        return aSession;
    }

    /** Update the session with new values */
    public void updateSession(Session session) {
        if (aSession.currentPhase != session.currentPhase)
            aSession.currentPhase = session.currentPhase;
        if (aSession.barbarianPosition != session.barbarianPosition)
            aSession.barbarianPosition = session.barbarianPosition;
        if (aSession.redDie != session.redDie)
            aSession.redDie = session.redDie;
        if (aSession.yellowDie != session.yellowDie)
            aSession.yellowDie = session.yellowDie;
        if (aSession.playerIndex != session.playerIndex)
            aSession.playerIndex = session.playerIndex;
    }

    public Player[] getPlayers() {
        return aSession.getPlayers();
    }

    /** Get the current player */
    public Player getCurrentPlayer() {
        return aSession.getPlayers()[aSession.playerIndex];
    }

    GamePhase getCurrentPhase() {
        return aSession.currentPhase;
    }

    void setCurrentPhase(GamePhase phase) {
        aSession.currentPhase = phase;
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
    public int getYellowDie() {
        return aSession.yellowDie;
    }

    public void setYellowDie(int yellowDie) {
        aSession.yellowDie = yellowDie;
    }

    /**
     * Returns the current value of the red dice.
     *
     * @return red dice value
     */
    public int getRedDie() {
        return aSession.redDie;
    }

    public void setRedDie(int redDie) {
        aSession.redDie = redDie;
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

    /**
     * Decreases the barbarian position by 1.
     */
    public void decreaseBarbarianPosition() {
        aSession.barbarianPosition--;
        // TODO needs to be reset back to the original position if below 0
    }

    /**
     * Resets the barbarian position to 7
     */
    public void resetBarbarianPosition() {
        aSession.barbarianPosition = 7;
    }

    /** Get a player's index by their username */
    public int getPlayerIndex(String username) {
        Player[] players = aSession.getPlayers();
        for (int i = 0; i < players.length; i++) {
            if (players[i].getUsername().equals(username))
                return i;
        }
        // Should never happen
        return -1;
    }

    /** Get the highest dice roll at the time of the call */
    public int getHighestDiceRoll() {
        return diceRolls.getRight();
    }

    /** Get the index of the player with the highest dice roll */
    public int getHighestDiceRollPlayerIndex() {
        return diceRolls.getLeft();
    }

    public void setHighestDiceRoll(int playerIndex, int diceRoll) {
        diceRolls.setLeft(playerIndex);
        diceRolls.setRight(diceRoll);
    }

    /** Update the number of players who rolled the dice */
    public void updateDiceRollPlayersCount() {
        diceRollPlayerIndex++;
    }

    /** Check if every player is done rolling the dice */
    public boolean isRollDiceDone() {
        return diceRollPlayerIndex >= getPlayers().length;
    }

    /** Reset the index of the last player that rolled the dice */
    public void resetDiceRoll() {
        diceRollPlayerIndex = 0;
    }

    /** Set the current player by their index */
    public void setCurrentPlayerIndex(int index) {
        aSession.playerIndex = index;
    }

    /** Move the index of the current player to point to the next player */
    public void nextPlayer() {
        aSession.playerIndex = (aSession.playerIndex + 1) % aSession.getPlayers().length;
    }
}
