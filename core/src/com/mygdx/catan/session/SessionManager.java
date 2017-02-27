package com.mygdx.catan.session;

import com.mygdx.catan.Account;
import com.mygdx.catan.Player;
import com.mygdx.catan.enums.PlayerColor;

import java.util.ArrayList;

public class SessionManager {
    private Session aSession;
    
    //FIXME SessionScreens must be in SessionController
    private ArrayList<SessionScreen> sessionScreens = new ArrayList<SessionScreen>();


    //TODO: change this to fit design, so far this is only placeholder!
    public SessionManager(int numberOfPlayers) {
        aSession = new Session(7, 0, 0, numberOfPlayers, 0);
    }

    public Player[] getPlayers() {
        return aSession.getPlayers();
    }

    public Player getCurrentPlayer() {
        return new Player(new Account("dummy", "dummy"), PlayerColor.ORANGE);
    }

    public void showDice() {
        for (SessionScreen sessionScreen : sessionScreens) {
            sessionScreen.showDice();
        }
    }

    //TODO FINISH
    public void updateResourceBar() {
        for (SessionScreen sessionScreen : sessionScreens) {

        }
    }

    public int getYellowDice() { return aSession.getYellowDice(); }

    public int getRedDice() { return aSession.getRedDice(); }
}
