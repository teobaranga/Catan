package com.mygdx.catan.injection.component;

import com.mygdx.catan.injection.module.SessionModule;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component
public interface AppComponent {

    SessionComponent plus(SessionModule sessionModule);
}
