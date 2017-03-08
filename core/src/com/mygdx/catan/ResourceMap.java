package com.mygdx.catan;

import com.mygdx.catan.enums.ResourceKind;

import java.util.EnumMap;
import java.util.Map;

/**
 * Map of resources & commodities to integer. This represents costs.
 */
public class ResourceMap extends EnumMap<ResourceKind, Integer> {

    public ResourceMap() {
        super(ResourceKind.class);
        // To avoid null pointer exceptions 
        for (ResourceKind kind : ResourceKind.values()) {
            put(kind, 0);
        }
    }

    /**
     * Add a number of resources to the specified resource kind. Can be negative
     * in order to remove resources.
     *
     * @param resourceKind the resource type to which the number is added
     * @param count        the number of resources to be added to the current
     */
    public void add(ResourceKind resourceKind, int count) {
        put(resourceKind, get(resourceKind) + count);
    }

    /**
     * Add the values of the provided ResourceMap to this ResourceMap.
     */
    public void add(ResourceMap resourceMap) {
        for (Map.Entry<ResourceKind, Integer> entry : resourceMap.entrySet()) {
            put(entry.getKey(), get(entry.getKey()) + entry.getValue());
        }
    }

    /**
     * Remove the values of the provided ResourceMap from this ResourceMap.
     * This operation assumes that the values are legal, and that the values
     * of this ResourceMap will not decrease below zero after this operation.
     */
    public void remove(ResourceMap resourceMap) {
        for (Map.Entry<ResourceKind, Integer> entry : resourceMap.entrySet()) {
            put(entry.getKey(), get(entry.getKey()) - entry.getValue());
        }
    }
}
