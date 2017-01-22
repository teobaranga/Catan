package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class CatanWindow extends Window {

    private WindowListener windowListener;

    public CatanWindow(String title, Skin skin) {
        super(title, skin);
        init();
    }

    public CatanWindow(String title, Skin skin, String styleName) {
        super(title, skin, styleName);
        init();
    }

    public CatanWindow(String title, WindowStyle style) {
        super(title, style);
        init();
    }

    private void init() {
        Skin skin = getSkin();
        if (skin != null) {
            // Skin shouldn't be null, always create this window with a style!
            // Get the font
            BitmapFont font = skin.getFont("default");
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            // Add some padding to be able to see the title bar properly
            padTop(font.getCapHeight() + font.getAscent() - font.getDescent() + 2f);
            // Set the title bar background color (create a new color)
            getTitleTable().background(skin.newDrawable("background", Color.LIGHT_GRAY));

            // Add the close button
            final TextButton closeButton = new TextButton("X", skin);
            closeButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    closeButton.setChecked(false);
                    if (windowListener != null)
                        windowListener.onWindowClosed();
                }
            });
            getTitleTable().add(closeButton).height(getPadTop()).width(20f);
        }
        setMovable(false);
        setModal(true);
        // Center title
        getTitleLabel().setAlignment(Align.center);

        // Make the window around 3/4 of the screen
        setWidth(3f / 4f * Gdx.graphics.getWidth());
        setHeight(3f / 4f * Gdx.graphics.getHeight());

        // Position window in the middle
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);
    }

    public void setWindowListener(WindowListener windowListener) {
        this.windowListener = windowListener;
    }

    public interface WindowListener {
        void onWindowClosed();
    }
}
