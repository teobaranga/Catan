package com.mygdx.catan.session;

import com.mygdx.catan.GameRules;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ProgressCardType;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.player.Player;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Collection;
import java.util.HashMap;

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
        if (!sessionManagerInstances.containsKey(session))
            sessionManagerInstances.put(session, new SessionManager(session));
        return sessionManagerInstances.get(session);
    }

    /** Create a dummy session, for testing purposes */
    public static Session newPlaceholderSession(Collection<Account> accounts) {
        Session session = Session.newInstance(accounts, GameRules.getGameRulesInstance().getVpToWin());

        // Add lots of resources to the players to make it easy to test
        ResourceMap resourceMap = new ResourceMap();
        for (ResourceKind resourceKind : ResourceKind.values())
            resourceMap.add(resourceKind, 100);
        for (Player player : session.getPlayers())
            player.addResources(resourceMap);

        return session;
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
        if (aSession.firstPlayerIndex != session.firstPlayerIndex)
            aSession.firstPlayerIndex = session.firstPlayerIndex;
        if (aSession.playerIndex != session.playerIndex)
            aSession.playerIndex = session.playerIndex;
        if (aSession.clockwise != session.clockwise)
            aSession.clockwise = session.clockwise;
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
        aSession.addResources(cost);
    }

    /**
     * Remove resources from the bank.
     *
     * @param cost The resources to be removed from the bank
     */
    public void removeFromBank(ResourceMap cost) {
        aSession.removeResources(cost);
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

    /**
     * Sets the first player by their index.
     * Warning: this also sets the current player.
     */
    public void setFirstPlayer(int index) {
        aSession.firstPlayerIndex = index;
        aSession.playerIndex = index;
    }

    /** Move the index of the current player to point to the next player */
    void nextPlayer() {
        if (aSession.clockwise)
            aSession.playerIndex = (aSession.playerIndex + 1) % aSession.getPlayers().length;
        else {
            int nextIndex = aSession.playerIndex - 1;
            aSession.playerIndex = nextIndex < 0 ? aSession.getPlayers().length - 1 : nextIndex;
        }
    }

    /** Set the direction of the next player to be clockwise */
    public void setClockwise() {
        aSession.clockwise = true;
    }

    /** Set the direction of the next player to be counter-clockwise */
    public void setCounterClockwise() {
        aSession.clockwise = false;
    }

    /**
     * Check if a round was just completed, ie. if every player had a chance to play.
     */
    public boolean isRoundCompleted() {
        int lastPlayerIndex = aSession.firstPlayerIndex - 1;
        if (lastPlayerIndex < 0)
            lastPlayerIndex = aSession.getPlayers().length - 1;
        return aSession.playerIndex == (aSession.clockwise ? aSession.firstPlayerIndex : lastPlayerIndex);
    }

    public Player getlongestRoadOwner() {
        return aSession.longestRoadOwner;
    }

    public void setlongestRoadOwner(Player player) {
        aSession.longestRoadOwner = player;
    }

    public void setCurrentlyExecutingProgressCard(ProgressCardType type) {
        aSession.setCurrentlyExecutingProgressCard(type);
        
        //TODO: loop through players and remove card from players hand
    }
    
    public void finishCurrentlyExecutingProgressCard() {
        aSession.setCurrentlyExecutingProgressCard(null);
    }
    
    public ProgressCardType getCurrentlyExecutingProgressCard() {
        return aSession.getCurrentlyExecutingProgressCard();
    }
    
//    public void incrementProgressCardMap (ProgressCardType pType) {
//        Integer currentValue = aSession.getProgressCardMap().get(pType);
//        aSession.getProgressCardMap().put(pType, currentValue++);
//    }
//
//    public void decrementProgressCardMap (ProgressCardType pType) {
//        Integer currentValue = aSession.getProgressCardMap().get(pType);
//        aSession.getProgressCardMap().put(pType, currentValue--);
//    }
//
//    public HashMap<ProgressCardType, Integer> clearProgressCardsInPlay () {
//        for(Integer i: aSession.getProgressCardMap().values()) {
//            i = 0;
//        }
//        return aSession.getProgressCardMap();
//    }
    public void incrementTokenVP (Player currentP) {
        currentP.incrementTokenVictoryPoints();
    }
}
