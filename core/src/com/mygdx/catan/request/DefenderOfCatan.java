package com.mygdx.catan.request;

import com.mygdx.catan.enums.PlayerColor;

public class DefenderOfCatan extends ForwardedRequest {

    PlayerColor winner;
    
    public static DefenderOfCatan newInstance(PlayerColor winner, String username) {
        DefenderOfCatan request = new DefenderOfCatan();
        
        request.universal = true;
        request.username = username;
        request.winner = winner;
        
        return request;
    }
    
    public PlayerColor getWinner() {
        return winner;
    }
    
}
