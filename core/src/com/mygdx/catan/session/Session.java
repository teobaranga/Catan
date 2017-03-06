package com.mygdx.catan.session;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.EventKind;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Session {

    /** The current phase of the game/session */
    public GamePhase currentPhase;

    /** The last event rolled on the event die */
    public EventKind eventDie;

    /** The position of the barbarians */
    public int barbarianPosition;

    public int redDie;
    public int yellowDie;
    public int VPsToWin; //set

    /** Index of the current player */
    public int playerIndex;

    private Player[] players;
    private ResourceMap Bank;

    //TODO: change this to fit design, so far this is only placeholder!
    public Session() {
        this.Bank = new ResourceMap();
        currentPhase = GamePhase.SETUP_PHASE_ONE;
    }

    public static Session newInstance(Collection<Account> accounts, int VPsToWin) {
        final Session session = new Session();
        session.VPsToWin = VPsToWin;

        final Iterator<Account> accountIterator = accounts.iterator();
        session.players = new Player[accounts.size()];
        for (int i = 0; i < session.players.length; i++) {
            if (accountIterator.hasNext()) {
                final Account account = accountIterator.next();
                session.players[i] = Player.newInstance(account, PlayerColor.values()[i]);
                System.out.println("Created player: " + account.getUsername());
            }
        }

        return session;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void add(ResourceMap cost) {
        for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
            Bank.put(entry.getKey(), Bank.get(entry.getKey()) + entry.getValue());
        }
    }

    public void remove(ResourceMap cost) {
        for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
            Bank.put(entry.getKey(), Bank.get(entry.getKey()) - entry.getValue());
        }
    }

    public ResourceMap adjustcost(ResourceMap cost) {
        for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
            int diff = entry.getValue() - Bank.get(entry.getKey());
            cost.put(entry.getKey(), (diff > 0) ? diff : entry.getValue());
        }
        return cost;
    }
}
