package com.sstewartgallus.plato.runtime;

public final class HaltThrowable extends BehaviourThrowable {
    public static final HaltThrowable SINGLETON = new HaltThrowable();

    private HaltThrowable() {
    }
}
