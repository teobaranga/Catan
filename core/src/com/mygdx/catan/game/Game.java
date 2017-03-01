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
     */
    public Map<Account, Integer> peers;

    public Map<Account, Boolean> readyStatus;

    public Session session;

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
    public boolean inProgress() {
        return session != null;
    }

    private Account getAccount(String username) {
        Account playerAccount = null;
        for (Account account : readyStatus.keySet()) {
            if (account.getUsername().equals(username)) {
                playerAccount = account;
            }
        }
        return playerAccount;
    }
}
