package com.mygdx.catan.ui.window;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.gameboard.Knight;
import com.mygdx.catan.ui.CatanWindow;
import com.mygdx.catan.ui.KnightActor;

import static com.mygdx.catan.gameboard.Knight.Strength.MIGHTY;

public class KnightActionsWindow extends CatanWindow {

    private final TextButton activateButton, promoteButton, moveButton;
    private final KnightActor knightActor;
    private KnightActivationListener knightActivationListener;
    private KnightUpgradeListener knightUpgradeListener;
    private KnightMoveListener knightMoveListener;

    public KnightActionsWindow(KnightActor knightActor) {
        super("Actions", CatanGame.skin);

        this.knightActor = knightActor;

        activateButton = new TextButton("Activate", CatanGame.skin);
        activateButton.addListener(new ChangeListener() {
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
        add(activateButton).padTop(10).padBottom(5).padLeft(30).padRight(30).row();

        promoteButton = new TextButton("Promote", CatanGame.skin);
        promoteButton.addListener(new ChangeListener() {
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
        add(promoteButton).padTop(5).padBottom(5).padLeft(30).padRight(30).row();

        moveButton = new TextButton("Move", CatanGame.skin);
        moveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (knightMoveListener != null) {
                    if (knightMoveListener.onMoveClicked(knightActor)) {
                        knightActor.refresh();
                        close();
                    }
                }
            }
        });
        add(moveButton).padTop(5).padBottom(10).padLeft(30).padRight(30).row();

        setModal(false);
        setMovable(true);
        pack();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        // Every time this window is displayed, do these checks...
        Knight knight = knightActor.getKnight();
        promoteButton.setDisabled(knight.is(MIGHTY) || knight.isPromotedThisTurn());
        activateButton.setDisabled(knight.isActive() || knight.isActivatedThisTurn());
        moveButton.setDisabled(!knight.isActive() || knight.isActivatedThisTurn());
    }

    public void setOnKnightActivateClick(KnightActivationListener knightActivationListener) {
        this.knightActivationListener = knightActivationListener;
    }

    public void setOnKnightUpgradeClick(KnightUpgradeListener knightUpgradeListener) {
        this.knightUpgradeListener = knightUpgradeListener;
    }

    public void setOnKnightMoveClick(KnightMoveListener knightMoveListener) {
        this.knightMoveListener = knightMoveListener;
    }

    public interface KnightActivationListener {
        boolean onActivateClicked(KnightActor knightActor);
    }

    public interface KnightUpgradeListener {
        boolean onUpgradeClicked(KnightActor knightActor);
    }

    public interface KnightMoveListener {
        boolean onMoveClicked(KnightActor knightActor);
    }
}
