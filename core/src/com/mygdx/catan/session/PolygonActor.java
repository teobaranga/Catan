package com.mygdx.catan.session;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PolygonActor extends Actor {

    private final PolygonRegion polygonRegion;

    public PolygonActor(PolygonRegion polygonRegion) {
        this.polygonRegion = polygonRegion;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!(batch instanceof PolygonSpriteBatch))
            return;
        final PolygonSpriteBatch polygonSpriteBatch = (PolygonSpriteBatch) batch;
        polygonSpriteBatch.draw(polygonRegion, getX(), getY());
    }
}
