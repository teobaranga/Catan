package com.mygdx.catan.injection.module;

import com.mygdx.catan.GameRules;
import com.mygdx.catan.TradeAndTransaction.TransactionManager;
import com.mygdx.catan.gameboard.GameBoardManager;
import com.mygdx.catan.injection.SessionScope;
import com.mygdx.catan.session.GamePieces;
import com.mygdx.catan.session.Session;
import com.mygdx.catan.session.SessionManager;
import dagger.Module;
import dagger.Provides;

@Module
public class SessionModule {

    private final Session session;

    public SessionModule(Session session) {
        this.session = session;
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
        return GameBoardManager.getInstance();
    }
}
