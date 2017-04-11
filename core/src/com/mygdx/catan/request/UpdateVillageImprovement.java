package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.VillageKind;

public class UpdateVillageImprovement extends ForwardedRequest {
    
    private VillageKind newKind;
    private PlayerColor owner;
    private boolean isUpgrade;
    private Pair<Integer,Integer> pos;
    
    public static UpdateVillageImprovement newInstance(VillageKind newKind, PlayerColor owner, boolean isUpgrade, Pair<Integer,Integer> pos, String username) {
        UpdateVillageImprovement request = new UpdateVillageImprovement();
        
        request.newKind = newKind;
        request.owner = owner;
        request.isUpgrade = isUpgrade;
        request.username = username;
        request.universal = false;
        request.pos = pos;
        
        return request;
    }
    
    public VillageKind getNewKind() {
        return newKind;
    }
    
    public PlayerColor getOwner() {
        return owner;
    }
    
    public boolean isUpgrade() {
        return isUpgrade;
    }
    
    public Pair<Integer,Integer> getPos() {
        return pos;
    }
}
