package com.mygdx.catan.request;

import org.apache.commons.lang3.tuple.Pair;

import com.mygdx.catan.enums.EdgeUnitKind;
import com.mygdx.catan.enums.PlayerColor;

/**
 * request indicating that the sender player built an edge piece between given intersections
 * */
public class BuildEdge extends ForwardedRequest {
	
	private Pair<Integer,Integer> leftPosition;
	private Pair<Integer,Integer> rightPosition;
	private EdgeUnitKind kind;
	private PlayerColor owner;
	
	public static BuildEdge newInstance(Pair<Integer,Integer> leftPosition, Pair<Integer,Integer> rightPosition, EdgeUnitKind kind, PlayerColor owner, String username) {
		BuildEdge request = new BuildEdge();
		request.leftPosition = leftPosition;
		request.rightPosition = rightPosition;
		request.kind = kind;
		request.owner = owner;
		request.username = username;
		request.universal = false;
		return request;
	}
	
	public Pair<Integer,Integer> getLeftPosition() {
		return leftPosition;
	}
	
	public Pair<Integer,Integer> getRightPosition() {
		return rightPosition;
	}
	
	public EdgeUnitKind getKind() {
		return kind;
	}
	
	public PlayerColor getOwner() {
		return owner;
	}

}
