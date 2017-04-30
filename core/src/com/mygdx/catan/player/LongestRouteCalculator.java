package com.mygdx.catan.player;

import com.mygdx.catan.CoordinatePair;
import com.mygdx.catan.gameboard.EdgeUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class LongestRouteCalculator {

    private static LongestRouteCalculator calculator = new LongestRouteCalculator();
    
    public static LongestRouteCalculator of() {
        return calculator;
    }
    
    public int calculateLongestRoute(ArrayList<EdgeUnit> edgeUnits) {
        List<ArrayList<EdgeUnit>> connectedComponents = new LinkedList<>();
        
        @SuppressWarnings("unchecked")
        ArrayList<EdgeUnit> edgeUnitsClone = (ArrayList<EdgeUnit>) edgeUnits.clone();
        HashMap<EdgeUnit, Boolean> visited = new HashMap<>();
        
        while (!edgeUnitsClone.isEmpty()) {
            ArrayList<EdgeUnit> connectedComponent = new ArrayList<>();
            EdgeUnit edge = edgeUnitsClone.remove(0);
            connectedComponent.add(edge);
            
            for (EdgeUnit other : edgeUnitsClone) {
                initVisitedMap(edgeUnitsClone, visited);
                if (reachable(edgeUnitsClone, visited, edge, other)) {
                    connectedComponent.add(other);
                }
            }
            
            for (EdgeUnit componentElement : connectedComponent) {
                edgeUnitsClone.remove(componentElement);
            }
            
            connectedComponents.add(connectedComponent);
        }
        
        
        int max = 0;
        for (ArrayList<EdgeUnit> connectedComponent : connectedComponents) {
            int longestRoad = findLongestRoadInComponent(connectedComponent);
            if (longestRoad > max) { max = longestRoad; }
        }
        
        return max;
    }
    
    int findLongestRoadInComponent(ArrayList<EdgeUnit> component) {
        ArrayList<EdgeUnit> endpoints = new ArrayList<>();
        ArrayList<EdgeUnit> cyclepoints = new ArrayList<>();
        for (EdgeUnit edge : component) {
            if (isEndpoint(edge, component)) {
                endpoints.add(edge);
            } else if (isInCycle(edge, component) && adjacentToEdgeNotInCycle(edge, component)) {
                cyclepoints.add(edge);
            }
        }
        
        ArrayList<EdgeUnit> allPoints = new ArrayList<>();
        allPoints.addAll(cyclepoints);
        allPoints.addAll(endpoints);
        
        int max = 0;
        
        for (EdgeUnit endpoint : endpoints) {
            boolean fromFirst;
            if (isEndpoint(endpoint.getAFirstCoordinate(), component)) {
                fromFirst = true;
            } else {
                fromFirst = false;
            }
            for (EdgeUnit point : allPoints) {
                int longestWalk = findLongestRoadValue(endpoint, point, component, fromFirst);
                if (longestWalk > max) { max = longestWalk; }
            }
        }
        for (EdgeUnit cyclepoint : cyclepoints) {
            boolean fromFirst;
            if (adjacentToEdgeNotInCycle(cyclepoint.getAFirstCoordinate(), component)) {
                fromFirst = true;
            } else {
                fromFirst = false;
            }
            for (EdgeUnit point : allPoints) {
                if (!cyclepoint.equals(point)){
                    int longestWalk = findLongestRoadValue(cyclepoint, point, component, fromFirst);
                    if (longestWalk > max) { max = longestWalk; }
                }
            }
        }
        
        return max;
    }
    
    boolean adjacentToEdgeNotInCycle(EdgeUnit unit, ArrayList<EdgeUnit> component) {
        for (EdgeUnit edge : component) {
            if (unit.isConnected(edge) && !isInCycle(edge, component)) {
                return true;
            }
        }
        return false;
    }
    
    boolean adjacentToEdgeNotInCycle(CoordinatePair intersection, ArrayList<EdgeUnit> component) {
        for (EdgeUnit edge : component) {
            if (edge.hasEndpoint(intersection) && !isInCycle(edge, component)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean reachable(List<EdgeUnit> component, HashMap<EdgeUnit, Boolean> visited, EdgeUnit start, EdgeUnit end) {
        if (start.equals(end)) {
            return true;
        } else {
            visited.put(start, true);
            for (EdgeUnit edge : getAdjacentEdges(start, component)) {
                if (!visited.get(edge)) {
                    if (reachable(component, visited, edge, end)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    public static void initVisitedMap(List<EdgeUnit> component, HashMap<EdgeUnit, Boolean> visited) {
        for (EdgeUnit edge : component) {
            visited.put(edge, false);
        }
    }
    
    boolean isInCycle(EdgeUnit edge, ArrayList<EdgeUnit> component) {
        @SuppressWarnings("unchecked")
        List<EdgeUnit> componentClone = (List<EdgeUnit>) component.clone();
        componentClone.remove(edge);
        List<EdgeUnit> leftAdjacentEdges = getAdjacentEdges(edge.getAFirstCoordinate(), componentClone);
        List<EdgeUnit> rightAdjacentEdges = getAdjacentEdges(edge.getASecondCoordinate(), componentClone);
        HashMap<EdgeUnit, Boolean> visited = new HashMap<>();
        
        for (EdgeUnit leftEdge : leftAdjacentEdges) {
            for (EdgeUnit rightEdge : rightAdjacentEdges) {
                initVisitedMap(component,visited);
                if (reachable(componentClone, visited, leftEdge, rightEdge)) {
                    return edge.isConnected(rightEdge) && edge.isConnected(leftEdge);
                }
            }
        }
        
        return false;
        
    } 
    
    /**
     * @return true iff the given edge is an open edge unit in the given component
     * */
    public static boolean isEndpoint(EdgeUnit edge, List<EdgeUnit> component) {
       
        if (edge.getAFirstCoordinate().isOccupied()) {
            if (!edge.getAFirstCoordinate().isOccupied(edge.getOwner())) {
                return true;
            }
        } else if (edge.getASecondCoordinate().isOccupied()) {
            if (!edge.getASecondCoordinate().isOccupied(edge.getOwner())) {
                return true;
            }
        }
        
        int adjacentLeftEdgesCount = 0;
        int adjacentRightEdgesCount = 0;
        for (EdgeUnit other : component) {
            if (!edge.equals(other) && other.hasEndpoint(edge.getAFirstCoordinate())) {
                adjacentLeftEdgesCount++;
            }
            if (!edge.equals(other) && other.hasEndpoint(edge.getASecondCoordinate())) {
                adjacentRightEdgesCount++;
            }
        }
        if (adjacentLeftEdgesCount == 0 || adjacentRightEdgesCount == 0) {
            return true;
        }
        
        return false;
    }
    
    /**
     * @return true iff the given coordinate is an open coordinate in the given component
     * */
    public static boolean isEndpoint(CoordinatePair end, List<EdgeUnit> component) {
        int adjacentEndCount = 0;
        for (EdgeUnit edge : component) {
            if (edge.hasEndpoint(end)) {
                adjacentEndCount++;
            }
        }
        return adjacentEndCount <= 1;
    }
    
    /**
     * @param start is the starting edge
     * @param end is the last edge
     * @param unvisited is a list of all the unvisited edges to choose from
     * @param fromFirst should be true if we are travelling in the direction firstCoordinate -> secondCoordinate (false other way around)
     * @return the length of the longest road from start to end in the list of edges unvisited
     * */
    public int findLongestRoadValue(EdgeUnit start, EdgeUnit end, ArrayList<EdgeUnit> unvisited, boolean fromFirst) {
        if (start.equals(end)) {
            return 1;
        } else {
            unvisited.remove(start);
            int max = Integer.MIN_VALUE;
            
            List<EdgeUnit> adjacentEdges;
            if (fromFirst) {
                List<EdgeUnit> adjacentToSecondEdges = getAdjacentEdges(start.getASecondCoordinate(), unvisited);
                adjacentEdges = adjacentToSecondEdges;
            } else {
                List<EdgeUnit> adjacentToFirstEdges = getAdjacentEdges(start.getAFirstCoordinate(), unvisited);
                adjacentEdges = adjacentToFirstEdges;
            }
            
            for (EdgeUnit adjacentEdge : adjacentEdges) {
                @SuppressWarnings("unchecked")
                ArrayList<EdgeUnit> unvisitedClone = (ArrayList<EdgeUnit>) unvisited.clone();
                
                int x;
                // determines which direction we are going
                if (start.hasEndpoint(adjacentEdge.getAFirstCoordinate())) {
                    x = findLongestRoadValue(adjacentEdge, end, unvisitedClone, true);
                } else {
                    x = findLongestRoadValue(adjacentEdge, end, unvisitedClone, false);
                }
                
                if (x > max) {
                    max = x;
                }
            }
            return 1 + max;
        }
    }
    
    /**
     * @param endpoint that we wish to find adjacent edges to
     * @param edges that we may find adjacent edges from
     * 
     * @return a list of all edges adjacent to endpoint
     * */
    private List<EdgeUnit> getAdjacentEdges(CoordinatePair endpoint, List<EdgeUnit> edges) {
        List<EdgeUnit> adjacentEdges = new ArrayList<>();
        
        for (EdgeUnit u : edges) {

            if (endpoint.isOccupied()) {
                if (!endpoint.isOccupied(u.getOwner())) {
                    continue;
                }
            }
            if (u.hasEndpoint(endpoint)) {
                adjacentEdges.add(u);
            }
        }
        
        return adjacentEdges;
    }
    
    /**
     * @param unit that we wish to find adjacent edges to
     * @param edges that we may find adjacent edges from
     * 
     * @return a list of all edges adjacent to unit
     * @precondition assumes edges does not contain unit
     * */
    private static List<EdgeUnit> getAdjacentEdges(EdgeUnit unit, List<EdgeUnit> edges) {
        List<EdgeUnit> adjacentEdges = new ArrayList<>();
        for (EdgeUnit u : edges) {
            if (!unit.equals(u) && u.isConnected(unit)) {
                adjacentEdges.add(u);
            }
        }
        
        return adjacentEdges;
    }
}
