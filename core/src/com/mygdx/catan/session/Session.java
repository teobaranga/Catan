package com.mygdx.catan.session;

import com.mygdx.catan.player.Player;
import com.mygdx.catan.DiceRollPair;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.EventKind;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.enums.ProgressCardType;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


public class Session {

    /** The current phase of the game/session */
    public GamePhase currentPhase;

    /** The last event rolled on the event die */
    public EventKind eventDie;
    public DiceRollPair diceResult;

    /** The position of the barbarians */
    public int barbarianPosition;

    public int redDie;
    public int yellowDie;
    public int VPsToWin; //set

    /** Index of the first player */
    public int firstPlayerIndex;
    
    /** owners of the metropolises */
    public Player scienceMetropolisOwner;
    public Player tradeMetropolisOwner;
    public Player politicsMetropolisOwner;


    /** Index of the current player */
    public int playerIndex;

    /** Flag indicating whether the next player is determined in a clockwise fashion. */
    public boolean clockwise;

    /** Longest Road Owner */
    public Player longestRoadOwner;

    private Player[] players;
    private ResourceMap bank;

    public boolean firstBarbarianAttack;
    
    /** Describes the currently executing progress card. Null if no progress card is executing */
    private ProgressCardType currentlyExecutingProgressCard;

    //private HashMap<ProgressCardType, Integer> progressCardMap;

    //TODO: change this to fit design, so far this is only placeholder!
    public Session() {
        // Add the initial bank resources
        bank = new ResourceMap();
        bank.add(ResourceKind.WOOD, 19);
        bank.add(ResourceKind.WOOL, 19);
        bank.add(ResourceKind.GRAIN, 19);
        bank.add(ResourceKind.BRICK, 19);
        bank.add(ResourceKind.ORE, 19);
        bank.add(ResourceKind.COIN, 12);
        bank.add(ResourceKind.PAPER, 12);
        bank.add(ResourceKind.CLOTH, 12);
        currentPhase = GamePhase.SETUP_PHASE_ONE;
        clockwise = true;
        barbarianPosition = 7;

//        progressCardMap = new HashMap<>();
//        for (ProgressCardType p: ProgressCardType.values()) {
//            progressCardMap.put(p, 0);
//        }
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

   // public HashMap<ProgressCardType, Integer> getProgressCardMap () { return progressCardMap; }

    /**
     * Add resources to the bank.
     */
    public void addResources(ResourceMap cost) {
        bank.add(cost);
    }

    /**
     * Remove resources from the bank.
     */
    public void removeResources(ResourceMap cost) {
        bank.remove(cost);
    }

    public ResourceMap adjustcost(ResourceMap cost) {
        for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
            int diff = entry.getValue() - bank.get(entry.getKey());
            cost.put(entry.getKey(), (diff > 0) ? diff : entry.getValue());
        }
        return cost;
    }
    
    public ProgressCardType getCurrentlyExecutingProgressCard() {
        return currentlyExecutingProgressCard;
    }
    
    public void setCurrentlyExecutingProgressCard(ProgressCardType type) {
        currentlyExecutingProgressCard = type;
    }
}
