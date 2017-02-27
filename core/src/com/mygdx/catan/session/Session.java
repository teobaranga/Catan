package com.mygdx.catan.session;

import com.mygdx.catan.Account;
import com.mygdx.catan.Player;
import com.mygdx.catan.ResourceMap;
import com.mygdx.catan.enums.EventKind;
import com.mygdx.catan.enums.GamePhase;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.ResourceKind;

import java.util.Map;

public class Session {
	
	GamePhase currentPhase;
	EventKind eventDice;
	private int barbarianPosition; //set      FIXME: currently this is in gameboard
	private int redDice; //setters needed?
	private int yellowDice; //setters needed?
	private int numberOfPlayers; //set
	private int VPsToWin; //set
	private Player[] players;
	private ResourceMap Bank;
	
	//TODO: change this to fit design, so far this is only placeholder!
	public Session(int barbarianPosition, int redDice, int yellowDice, int numberofPlayers, int VPsToWin) {
		this.barbarianPosition = barbarianPosition;
		this.redDice = redDice;
		this.yellowDice = yellowDice;
		this.numberOfPlayers = numberofPlayers;
		this.VPsToWin = VPsToWin;
		this.Bank = new ResourceMap();
		
		players = new Player[numberOfPlayers];
		for (int i = 0; i<numberOfPlayers; i++) {
			Account account = new Account(i+"",i+"");
			players[i] = new Player(account,PlayerColor.values()[i]);
		}
	}
	
	public GamePhase getCurrentPhase() {
		return this.currentPhase;
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
	
	public void setBarbarianPosition(int barbarianPosition) {
		this.barbarianPosition = barbarianPosition;
	}
	
	public int getBarbarianPosition() {
		return this.barbarianPosition;
	}
	
	public int getRedDice() {
		return this.redDice;
	}
	
	public int getYellowDice() {
		return this.yellowDice;
	}
	
	public int getNumberOfPlayers() {
		return this.numberOfPlayers;
	}
	
	public void setNumberOfPlayers(int numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}
	
	public Player[] getPlayers() {
		return players;
	}

	public void add(ResourceMap cost){
		for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
			Bank.put(entry.getKey(), Bank.get(entry.getKey()) + entry.getValue());
		}
	}

	public void remove(ResourceMap cost){
		for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
			Bank.put(entry.getKey(), Bank.get(entry.getKey()) - entry.getValue());
		}
	}

	public ResourceMap adjustcost(ResourceMap cost) {
		for (Map.Entry<ResourceKind, Integer> entry : cost.entrySet()) {
			int diff = entry.getValue() - Bank.get(entry.getKey());
			cost.put(entry.getKey(), (diff > 0) ? diff : entry.getValue() );
		}
		return cost;
	}
}
