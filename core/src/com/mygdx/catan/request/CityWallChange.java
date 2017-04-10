package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.enums.PlayerColor;

public class CityWallChange extends ForwardedRequest {

    private boolean newCityWallStatus;
    private Pair<Integer,Integer> position;
    private PlayerColor owner;
    
    public static CityWallChange newInstance(boolean newCityWallStatus, Pair<Integer,Integer> position, PlayerColor owner, String username) {
        CityWallChange request = new CityWallChange();
        
        request.newCityWallStatus = newCityWallStatus;
        request.position = position;
        request.owner = owner;
        request.universal = false;
        request.username = username;
        
        return request;
    } 
    
    public boolean getNewCityWallStatus() {
        return newCityWallStatus;
    }
    
    public Pair<Integer,Integer> getPosition() {
        return position;
    }
    
    public PlayerColor getOwner() {
        return owner;
    }
}
