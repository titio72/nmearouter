package com.aboni.utils;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class ThingsFactory {

    private ThingsFactory() {
    }

    private static Injector injector;

    public static void setInjector(Injector injector) {
        ThingsFactory.injector = injector;
    }

    public static <T> T getInstance(Class<T> aClass) {
        return injector.getInstance(aClass);
    }

    public static <T> T getInstance(Class<T> aClass, String named) {
        Key<T> key = Key.get(aClass, Names.named(named));
        return injector.getInstance(key);
    }
}