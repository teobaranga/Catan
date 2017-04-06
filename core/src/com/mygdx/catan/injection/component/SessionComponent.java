package com.mygdx.catan.injection.component;

import com.mygdx.catan.injection.SessionScope;
import com.mygdx.catan.injection.module.SessionModule;
import com.mygdx.catan.session.KnightController;
import dagger.Subcomponent;

@SessionScope
@Subcomponent(modules = SessionModule.class)
public interface SessionComponent {

    void inject(KnightController knightController);
}
