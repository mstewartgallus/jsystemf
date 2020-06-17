package com.sstewartgallus.plato.runtime;

public final class HaltBehaviour extends Behaviour {
    public static final Behaviour SINGLETON = new HaltBehaviour();

    private HaltBehaviour() {
    }

    @Override
    public Behaviour step() throws BehaviourThrowable {
        throw HaltThrowable.SINGLETON;
    }
}
