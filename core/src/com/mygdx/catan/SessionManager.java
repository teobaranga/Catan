package com.mygdx.catan;

public class SessionManager {
	private Session aSession;
	
	
	//TODO: change this to fit design, so far this is only placeholder!
	public SessionManager(int numberOfPlayers)
	{
		aSession = new Session(7,0,0,numberOfPlayers,0);
	}
	
	public Player[] getPlayers() {
		return aSession.getPlayers();
	}
}
