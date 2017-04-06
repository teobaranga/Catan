package com.mygdx.catan.ui.window;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.ui.CatanWindow;
import com.mygdx.catan.ui.KnightActor;

import static com.mygdx.catan.gameboard.Knight.Strength.MIGHTY;

public class KnightActionsWindow extends CatanWindow {

    private KnightActivationListener knightActivationListener;
    private KnightUpgradeListener knightUpgradeListener;

    private final TextButton upgrade;

    private final KnightActor knightActor;

    public KnightActionsWindow(KnightActor knightActor) {
        super("Actions", CatanGame.skin);

        this.knightActor = knightActor;

        TextButton activate = new TextButton("Activate", CatanGame.skin);
        activate.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (knightActivationListener != null) {
                    if (knightActivationListener.onActivateClicked(knightActor)) {
                        knightActor.refresh();
                        close();
                    }
                }
            }
        });
        add(activate).padTop(10).padBottom(5).padLeft(30).padRight(30).row();

        upgrade = new TextButton("Upgrade", CatanGame.skin);
        upgrade.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (knightUpgradeListener != null) {
                    if (knightUpgradeListener.onUpgradeClicked(knightActor)) {
                        knightActor.refresh();
                        close();
                    }
                }
            }
        });
        add(upgrade).padTop(5).padBottom(10).padLeft(30).padRight(30).row();

        setModal(false);
        setMovable(true);
        pack();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        // Every time this window is displayed, do these checks...
        if (knightActor.getKnight().is(MIGHTY))
            upgrade.setDisabled(true);
    }

    public void setOnKnightActivateClick(KnightActivationListener knightActivationListener) {
        this.knightActivationListener = knightActivationListener;
    }

    public void setOnKnightUpgradeClick(KnightUpgradeListener knightUpgradeListener) {
        this.knightUpgradeListener = knightUpgradeListener;
    }

    public interface KnightActivationListener {
        boolean onActivateClicked(KnightActor knightActor);
    }

    public interface KnightUpgradeListener {
        boolean onUpgradeClicked(KnightActor knightActor);
    }
}
