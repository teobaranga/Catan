package com.mygdx.catan.game;

import com.esotericsoftware.kryonet.Connection;
import com.mygdx.catan.Account;
import com.mygdx.catan.session.Session;

import java.util.Map;

public class Game {

    /**
     * Map of game accounts to their latest connection.
     * Needs to be kept up-to-date because the connection might change.
     */
    public Map<Account, Connection> peers;

    public Session session;

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
