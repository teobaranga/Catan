package com.mygdx.catan.injection.module;

import com.mygdx.catan.GameRules;
import com.mygdx.catan.TradeAndTransaction.TransactionManager;
import com.mygdx.catan.gameboard.GameBoard;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.injection.SessionScope;
import com.mygdx.catan.session.*;
import dagger.Module;
import dagger.Provides;

@Module
public class SessionModule {

    private final SessionScreen sessionScreen;
    private final Session session;
    private final GameBoard gameboard;

    public SessionModule(SessionScreen sessionScreen, Session session, GameBoard gameboard) {
        this.sessionScreen = sessionScreen;
        this.session = session;
        this.gameboard = gameboard;
    }

    @Provides
    @SessionScope
    SessionScreen provideSessionScreen() {
        return sessionScreen;
    }

    @Provides
    @SessionScope
    SessionManager provideSessionManager() {
        return SessionManager.getInstance(session);
    }

    @Provides
    @SessionScope
    TransactionManager provideTransactionManager(SessionManager sessionManager) {
        return TransactionManager.getInstance(sessionManager);
    }

    @Provides
    @SessionScope
    GameRules provideGameRules() {
        return GameRules.getGameRulesInstance();
    }

    @Provides
    @SessionScope
    GamePieces provideGamePieces() {
        return GamePieces.getInstance();
    }

    @Provides
    @SessionScope
    GameBoardManager provideGameBoardManager() {
        return GameBoardManager.getInstance(gameboard);
    }

    @Provides
    @SessionScope
    SessionController provideSessionController() {
        return new SessionController(sessionScreen);
    }

    @Provides
    @SessionScope
    KnightController provideKnightController(SessionController sessionController) {
        return new KnightController(sessionController);
    }
}
