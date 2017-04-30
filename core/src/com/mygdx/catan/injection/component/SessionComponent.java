package com.mygdx.catan.injection.component;

import com.mygdx.catan.injection.SessionScope;
import com.mygdx.catan.injection.module.SessionModule;
import com.mygdx.catan.session.KnightController;
import com.mygdx.catan.session.SessionController;
import com.mygdx.catan.session.SessionScreen;
import com.mygdx.catan.session.helper.KnightHelper;
import dagger.Subcomponent;

@SessionScope
@Subcomponent(modules = SessionModule.class)
public interface SessionComponent {

    void inject(SessionScreen sessionScreen);

    void inject(SessionController sessionController);

    void inject(KnightController knightController);

    void inject(KnightHelper knightHelper);
}
