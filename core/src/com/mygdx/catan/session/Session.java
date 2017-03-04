package com.mygdx.catan.session;

import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.EventKind;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;

import java.util.Collection;
import java.util.Map;

public class Session {

	private GamePhase currentPhase;
	private EventKind eventDice;
	private int barbarianPosition = 7; //set      FIXME: currently this is in gameboard
	private int redDice; //setters needed?
	private int yellowDice; //setters needed?
	private int VPsToWin; //set
    /** Index of the current player */
    private int playerIndex;
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

		session.players = new Player[accounts.size()];
		for (int i = 0; i < session.players.length; i++) {
			final Account account = accounts.iterator().next();
			session.players[i] = Player.newInstance(account, PlayerColor.values()[i]);
			System.out.println("Created player: " + account.getUsername());
		}

		return session;
	}

	/** Get the index of the current player */
    public int getPlayerIndex() {
        return playerIndex;
    }

    public GamePhase getCurrentPhase() {
		return currentPhase;
	}

	public EventKind getEventKind() {
		return this.eventDice;
	}

	public void setVPsToWin(int victoryPoints) {
		this.VPsToWin = victoryPoints;
	}

	public int getVpsToWin() {
		return this.VPsToWin;
	}

	public int getBarbarianPosition() {
		return this.barbarianPosition;
	}

	public void setBarbarianPosition(int barbarianPosition) {
		this.barbarianPosition = barbarianPosition;
	}

	public int getRedDice() {
		return this.redDice;
	}

	public int getYellowDice() {
		return this.yellowDice;
	}

    public void setRedDice(int redDice) {
        this.redDice = redDice;
    }

    public void setYellowDice(int yellowDice) {
        this.yellowDice = yellowDice;
    }

	public int getNumberOfPlayers() {
		return players.length;
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