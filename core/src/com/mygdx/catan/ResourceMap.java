package com.mygdx.catan;

import com.mygdx.catan.enums.ResourceKind;

import java.util.EnumMap;

/**
 * Map of resources & commodities to integer. This represents costs.
 */
public class ResourceMap extends EnumMap<ResourceKind, Integer> {

    public ResourceMap() {
        super(ResourceKind.class);
        // To avoid null pointer exceptions 
        for (ResourceKind kind: ResourceKind.values()) {
        	this.put(kind, 0);
        }
    }
}
