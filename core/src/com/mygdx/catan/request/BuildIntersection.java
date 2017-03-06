package com.mygdx.catan.request;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.enums.PlayerColor;
import com.mygdx.catan.enums.VillageKind;

/**
 * request indicating that the sender player built a village on given intersection
 * */
public class BuildIntersection extends ForwardedRequest {
	
	private CoordinatePair position;
	private VillageKind kind;
	private PlayerColor owner;
	
	public static BuildIntersection newInstance(CoordinatePair position, VillageKind kind, PlayerColor owner, String username) {
		BuildIntersection request = new BuildIntersection();
		request.position = position;
		request.kind = kind;
		request.owner = owner;
		request.username = username;
		request.universal = false;
		return request;
	}
	
	public CoordinatePair getPosition() {
		return position;
	}
	
	public VillageKind getKind() {
		return kind;
	}
	
	public PlayerColor getOwner() {
		return owner;
	}
}
