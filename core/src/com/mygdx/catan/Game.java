package com.mygdx.catan;

import com.mygdx.catan.enums.EventKind;

public class Game {
	
	GamePhase currentPhase;
	EventKind eventDice;
	private int barbarianPosition; //set
	private int redDice; //setters needed?
	private int yellowDice; //setters needed?
	private int numberOfPlayers; //set
	private int VPsToWin; //set
	
	public Game(int barbarianPosition, int redDice, int yellowDice, int numberofPlayers, int VPsToWin) {
		this.barbarianPosition = barbarianPosition;
		this.redDice = redDice;
		this.yellowDice = yellowDice;
		this.numberOfPlayers = numberofPlayers;
		this.VPsToWin = VPsToWin;
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
	
	
	
}
