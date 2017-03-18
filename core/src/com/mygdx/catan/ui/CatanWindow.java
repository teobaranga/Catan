package com.mygdx.catan.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class CatanWindow extends Window {

    private final float width, height;

    private TextButton closeButton;

    private WindowCloseListener windowCloseListener;

    /**
     * Create a new window
     *
     * @param title the title of the window - must not be null
     * @param skin  skin used for theming the window
     */
    public CatanWindow(String title, Skin skin) {
        super(title, skin, title.isEmpty() ? "no-title" : "default");
        width = 3f / 4f * Gdx.graphics.getWidth();
        height = 3f / 4f * Gdx.graphics.getHeight();
        init();
    }

    public CatanWindow(String title, Skin skin, float width, float height) {
        super(title, skin, title.isEmpty() ? "no-title" : "default");
        this.width = width;
        this.height = height;
        init();
    }

    private void init() {
        // Make the window around 3/4 of the screen
        setSize(width, height);

        // Position window in the middle
        setPosition(Gdx.graphics.getWidth() / 2 - getWidth() / 2, Gdx.graphics.getHeight() / 2 - getHeight() / 2);

        // Center title
        getTitleLabel().setAlignment(Align.center);

        // Create the close button
        if (getTitleLabel().getText().length != 0) {
            closeButton = new TextButton("X", getSkin());
            closeButton.setSize(20, 20);
            closeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    closeButton.setChecked(false);
                    close();
                }
            });
            getTitleTable().addActor(closeButton);
            closeButton.setPosition(getWidth() - closeButton.getWidth() - 5, 0);
        }

        getTitleTable().getCells().get(0).expandY().fillY();

        setMovable(false);
        setModal(true);

        row();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        // Correct the close button's position
        if (closeButton != null)
            closeButton.setPosition(getWidth() - closeButton.getWidth() - 5, 0);
    }

    /**
     * Close this window, remove it from its parent, and trigger the
     * close listener.
     */
    public void close() {
        remove();
        if (windowCloseListener != null)
            windowCloseListener.onWindowClosed();
    }

    public void setWindowCloseListener(WindowCloseListener windowCloseListener) {
        this.windowCloseListener = windowCloseListener;
    }

    public interface WindowCloseListener {
        void onWindowClosed();
    }
}
