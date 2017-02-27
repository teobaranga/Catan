package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class CatanWindow extends Window {

    private WindowCloseListener windowCloseListener;

    /**
     * Create a new window
     *
     * @param title the title of the window - must not be null
     * @param skin  skin used for theming the window
     */
    public CatanWindow(String title, Skin skin) {
        super(title, skin, title.isEmpty() ? "no-title" : "default");
        init();
    }

    private void init() {
        getTitleTable().getCells().get(0).expandY().fillY();
        Skin skin = getSkin();
        if (skin != null) {
            // Skin shouldn't be null, always create this window with a style!
            // Improve font rendering
            BitmapFont font = skin.getFont("default");
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            // Add the close button if the window has a title
            if (getTitleLabel().getText().length != 0) {
                // Add the close button
                final TextButton closeButton = new TextButton("X", skin);
                closeButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        closeButton.setChecked(false);
                        if (windowCloseListener != null)
                            windowCloseListener.onWindowClosed();
                    }
                });
                getTitleTable().add(closeButton).height(getPadTop()).width(25);
            }
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

    public void setWindowCloseListener(WindowCloseListener windowCloseListener) {
        this.windowCloseListener = windowCloseListener;
    }

    public interface WindowCloseListener {
        void onWindowClosed();
    }
}
