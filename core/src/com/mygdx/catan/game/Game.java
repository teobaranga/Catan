package com.mygdx.catan.game;

import com.mygdx.catan.Config;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.session.Session;

import java.util.HashMap;
import java.util.Map;

public class Game {

    /**
     * Map of game accounts to their latest connection (by ID).
     * Needs to be kept up-to-date because the connection might change.
     */
    public Map<Account, Integer> peers;

    public Session session;

    public Game() {
        peers = new HashMap<>(Config.MAX_PLAYERS);
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
}
