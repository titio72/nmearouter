package com.aboni.utils;

import com.google.inject.Injector;

public class ThingsFactory {
    private static Injector injector;

    public static void setInjector(Injector injector) {
        ThingsFactory.injector = injector;
    }

    public static <T> T getInstance(Class<T> aClass) {
        return injector.getInstance(aClass);
    }
}