package com.mygdx.catan.game;

import com.mygdx.catan.Config;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.session.Session;

import java.util.LinkedHashMap;
import java.util.Map;

public class Game {

    /**
     * Map of game accounts to their latest connection (by ID).
     * Needs to be kept up-to-date because the connection might change.
     * Contains all players of a game.
     */
    public final Map<Account, Integer> peers;

    /**
     * Map of game accounts to their ready status. The accounts must be
     * the same ones as in {@link #peers}.
     */
    private final Map<Account, Boolean> readyStatus;

    public String name;

    public Session session;

    /** The admin of the game, ie. the player who created this game */
    private Account admin;

    public Game() {
        peers = new LinkedHashMap<>(Config.MAX_PLAYERS);
        readyStatus = new LinkedHashMap<>(Config.MAX_PLAYERS);
    }

    /**
     * Add a new player to the game.
     *
     * @param account      account of the new player
     * @param connectionId connection ID of the new player
     */
    public void addPlayer(Account account, Integer connectionId) {
        peers.put(account, connectionId);
        readyStatus.put(account, false);
    }

    /**
     * Remove a player from the game.
     *
     * @param account account of the player being removed
     */
    public void removePlayer(Account account) {
        peers.remove(account);
        readyStatus.remove(account);
        // Update the admin in case the admin was removed
        if (account == admin) {
            admin = null;
            getAdmin();
        }
    }

    public void markAsReady(String username) {
        final Account account = getAccount(username);
        readyStatus.put(account, true);
    }

    /**
     * Get the number of players participating in this game.
     */
    public int getPlayerCount() {
        return peers.keySet().size();
    }

    /**
     * Check if this game is in progress or not (this is the case only
     * when the game is at the looking for players stage)
     */
    public boolean isInProgress() {
        return session != null;
    }

    /**
     * Check if a player of this game is ready.
     *
     * @param username username of the player
     */
    public boolean isReady(String username) {
        for (Map.Entry<Account, Boolean> entry : readyStatus.entrySet()) {
            if (entry.getKey().getUsername().equals(username))
                return entry.getValue();
        }
        return false;
    }

    /**
     * Check if the game is ready to start, ie. if there are at least a few
     * players in the game and they're all marked as ready.
     *
     * @return true if the game is ready to begin, false otherwise
     */
    public boolean isReadyToStart() {
        int readyCount = 0;
        for (Boolean ready : readyStatus.values()) {
            System.out.println(ready);
            if (ready) {
                readyCount++;
            } else {
                return false;
            }
        }
        return readyCount >= Config.MIN_PLAYERS;
    }

    /**
     * Get the admin of this game.
     *
     * @return the admin, or null if the game has no players
     */
    public Account getAdmin() {
        // Get the admin if already cached
        if (admin != null)
            return admin;
        // No player, return null
        if (peers.isEmpty())
            return null;
        // Update the admin to be the first account
        final Account[] accounts = peers.keySet().toArray(new Account[Config.MAX_PLAYERS]);
        admin = accounts[0];
        return admin;
    }

    /**
     * Get the account of a game member
     *
     * @param username username of the player
     * @return the account of the player
     */
    private Account getAccount(String username) {
        Account playerAccount = null;
        for (Account account : peers.keySet()) {
            if (account.getUsername().equals(username)) {
                playerAccount = account;
            }
        }
        return playerAccount;
    }

    @Override
    public String toString() {
        return (name == null ? "Unnamed" : name) + " ID:" + Integer.toHexString(hashCode());
    }
}
